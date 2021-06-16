package uk.co.brightec.kbarcode.camera

import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import timber.log.Timber
import uk.co.brightec.kbarcode.util.OpenForTesting

@OpenForTesting
@Suppress("TooManyFunctions") // This class does still feels single responsibility
internal class Camera2Source(
    private val cameraManager: CameraManager
) {

    @VisibleForTesting
    internal var cameraDevice: CameraDevice? = null

    @VisibleForTesting
    internal var currentSession: CameraCaptureSession? = null

    @VisibleForTesting
    internal var currentSurfaces: List<Surface>? = null

    @VisibleForTesting
    internal var cameraOpening = false
    var requestedFacing = CameraCharacteristics.LENS_FACING_BACK
    var requestedFlashMode = CameraMetadata.FLASH_MODE_OFF

    constructor(context: Context) : this(
        cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
    )

    fun isStarted() = cameraDevice != null

    fun isOpening() = cameraOpening

    fun release() {
        currentSession?.close()
        currentSession = null
        cameraDevice?.close()
        cameraDevice = null
        cameraOpening = false
        currentSurfaces = null
    }

    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun start(
        surfaces: List<Surface>,
        listener: OnCameraReadyListener?
    ) {
        if (cameraDevice != null || cameraOpening) return

        val cameraId = selectCamera()
        if (cameraId == null) {
            val message = "Error opening camera - No cameraId available"
            val exception = CameraServiceException(message)
            releaseCameraAndReportException(
                listener = listener, message = message, exception = exception
            )
            return
        }
        cameraOpening = true
        try {
            cameraManager.openCamera(
                cameraId,
                object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraOpening = false
                        cameraDevice = camera

                        if (surfaces.any { !it.isValid }) {
                            val message = "Surfaces no longer valid"
                            val exception = CameraException(message)
                            releaseCameraAndReportException(
                                listener = listener, message = message, exception = exception
                            )
                            return
                        }

                        listener?.onCameraReady()
                        createCaptureSession(
                            surfaces = surfaces,
                            listener = listener
                        )
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        release()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        val exception = createExceptionFromCameraDeviceError(error)
                        releaseCameraAndReportException(
                            listener = listener,
                            message = "Error opening camera",
                            exception = exception
                        )
                    }
                },
                null
            )
        } catch (e: android.hardware.camera2.CameraAccessException) {
            releaseCameraAndReportException(
                listener = listener,
                message = "Camera is disabled by device policy, has been disconnected, or is " +
                    "being used by a higher-priority camera API client.",
                cause = e
            )
        } catch (e: IllegalArgumentException) {
            releaseCameraAndReportException(
                listener = listener,
                message = "CameraId does not match any camera device",
                cause = e
            )
        } catch (e: SecurityException) {
            releaseCameraAndReportException(
                listener = listener,
                message = "Application does not have permission to access the camera",
                cause = e
            )
        }
    }

    fun getOutputSize(minWidth: Int): Size? {
        val cameraDevice = this.cameraDevice
        // If we already have a camera started use that, otherwise use the camera id of the
        // camera we expect to start
        val characteristics = if (cameraDevice != null) {
            cameraManager.getCameraCharacteristics(cameraDevice.id)
        } else {
            val cameraId = selectCamera() ?: return null
            cameraManager.getCameraCharacteristics(cameraId)
        }
        val configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?: throw IllegalStateException() // This key is available on all devices
        val sizes = configs.getOutputSizes(IMAGE_FORMAT)
        return chooseOutputSize(sizes, minWidth)
    }

    fun getCameraFacing(): Int? {
        val cameraDevice = this.cameraDevice ?: return null
        val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
        return characteristics.get(CameraCharacteristics.LENS_FACING)
    }

    fun getCameraSensorOrientation(): Int? {
        val cameraDevice = this.cameraDevice ?: return null
        val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
        return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
    }

    fun getCameraSensorInfoActiveArraySize(): Rect? {
        val cameraDevice = this.cameraDevice ?: return null
        val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
        return characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
    }

    /**
     * Request focus on a particular region
     *
     * Sources:
     * https://gist.github.com/royshil/8c760c2485257c85a11cafd958548482
     * https://medium.com/androiddevelopers/whats-new-in-camerax-fb8568d6ddc
     * https://07687375219170485808.googlegroups.com/attach/b86ef247362e5/Touch-to-focus%20API%20Draft.pdf
     */
    fun requestFocus(regions: Array<MeteringRectangle>) {
        // Some safety checks
        val session = this.currentSession ?: return
        if (getCameraAvailableAfModes()?.contains(CameraMetadata.CONTROL_AF_MODE_AUTO) != true) {
            return
        }
        val maxRegionsAf = getCameraAfMaxRegions()
        if (maxRegionsAf == null || maxRegionsAf < 1) return

        // Stop repeating preview and cancel any existing focus requests
        session.stopRepeating()
        val cancelBuilder = createDefaultCaptureRequestBuilder() ?: return
        cancelBuilder.set(
            CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
        )
        session.capture(cancelBuilder.build(), null, null)

        // Start new focus request
        val builder = createDefaultCaptureRequestBuilder(regions = regions) ?: return
        builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO)
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
        val callback = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                createRepeatingRequest(session = session, regions = regions)
            }

            override fun onCaptureFailed(
                session: CameraCaptureSession,
                request: CaptureRequest,
                failure: CaptureFailure
            ) {
                Timber.e("requestAutoFocus() failed: $failure")
                createRepeatingRequest(session = session)
            }
        }
        session.capture(builder.build(), callback, null)
    }

    /**
     * Clear the focus from a particular region
     */
    fun clearFocusRegions() {
        // Some safety checks
        val session = this.currentSession ?: return

        // Stop repeating preview and cancel any existing focus requests
        session.stopRepeating()
        val cancelBuilder = createDefaultCaptureRequestBuilder() ?: return
        cancelBuilder.set(
            CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
        )
        session.capture(cancelBuilder.build(), null, null)

        // Restart repeating
        createRepeatingRequest(session = session)
    }

    @Suppress("ReturnCount") // Better readability
    @VisibleForTesting
    internal fun selectCamera(): String? {
        if (cameraManager.cameraIdList.isEmpty()) return null

        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == requestedFacing) {
                return cameraId
            }
        }
        return cameraManager.cameraIdList[0]
    }

    @VisibleForTesting
    internal fun createCaptureSession(
        surfaces: List<Surface>,
        listener: OnCameraReadyListener?
    ) {
        try {
            cameraDevice?.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        if (cameraDevice == null) return
                        currentSession = session
                        currentSurfaces = surfaces

                        createRepeatingRequest(session = session, listener = listener)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        releaseCameraAndReportException(
                            listener = listener, message = "Error creating camera session",
                            exception = CameraSessionException()
                        )
                    }
                },
                null
            )
        } catch (e: android.hardware.camera2.CameraAccessException) {
            releaseCameraAndReportException(
                listener = listener, message = "Error creating camera session", cause = e
            )
        } catch (e: IllegalArgumentException) {
            releaseCameraAndReportException(
                listener = listener, message = "Surfaces do not meet the requirements", cause = e
            )
        } catch (e: IllegalStateException) {
            releaseCameraAndReportException(
                listener = listener, message = "Camera device has been closed", cause = e
            )
        }
    }

    @VisibleForTesting
    internal fun createRepeatingRequest(
        session: CameraCaptureSession,
        listener: OnCameraReadyListener? = null,
        regions: Array<MeteringRectangle>? = null
    ) {
        try {
            val builder = createDefaultCaptureRequestBuilder(listener, regions) ?: return
            builder.set(CaptureRequest.CONTROL_AF_MODE, selectBestContinuousAfMode())
            builder.set(CaptureRequest.CONTROL_AE_MODE, selectBestContinuousAeMode())
            builder.set(CaptureRequest.CONTROL_AWB_MODE, selectBestContinuousAwbMode())
            session.setRepeatingRequest(builder.build(), null, null)
        } catch (e: android.hardware.camera2.CameraAccessException) {
            releaseCameraAndReportException(
                listener = listener, message = "Error creating capture request", cause = e
            )
        } catch (e: IllegalStateException) {
            releaseCameraAndReportException(
                listener = listener, message = "Camera device has been closed", cause = e
            )
        } catch (e: IllegalArgumentException) {
            releaseCameraAndReportException(
                listener = listener, message = "Surfaces do not meet the requirements", cause = e
            )
        }
    }

    /**
     * Select the smallest pixel width size which is still larger than minWidth
     *
     * Fall back: Largest size (if none large enough), or just first element
     */
    @Suppress("MagicNumber")
    @VisibleForTesting
    internal fun chooseOutputSize(sizes: Array<Size>, minWidth: Int): Size {
        val largeEnoughSizes = sizes.filter { it.width > minWidth }
        return if (largeEnoughSizes.isNotEmpty()) {
            largeEnoughSizes.minByOrNull { it.width }
        } else {
            sizes.maxByOrNull { it.width }
        } ?: sizes[0]
    }

    @VisibleForTesting
    internal fun createExceptionFromCameraDeviceError(error: Int) = when (error) {
        CameraDevice.StateCallback.ERROR_CAMERA_IN_USE -> CameraInUseException()
        CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE -> MaxCamerasInUseException()
        CameraDevice.StateCallback.ERROR_CAMERA_DISABLED -> CameraDisabledException()
        CameraDevice.StateCallback.ERROR_CAMERA_DEVICE -> CameraDeviceException()
        CameraDevice.StateCallback.ERROR_CAMERA_SERVICE -> CameraServiceException()
        else -> CameraException()
    }

    @VisibleForTesting
    internal fun getCameraAvailableAfModes(): IntArray? {
        val cameraDevice = this.cameraDevice ?: return null
        val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
        return characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
    }

    @VisibleForTesting
    internal fun getCameraAvailableAeModes(): IntArray? {
        val cameraDevice = this.cameraDevice ?: return null
        val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
        return characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES)
    }

    @VisibleForTesting
    internal fun getCameraAvailableAwbModes(): IntArray? {
        val cameraDevice = this.cameraDevice ?: return null
        val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
        return characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
    }

    @VisibleForTesting
    internal fun getCameraAfMaxRegions(): Int? {
        val cameraDevice = this.cameraDevice ?: return null
        val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
        return characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)
    }

    @VisibleForTesting
    internal fun getCameraAeMaxRegions(): Int? {
        val cameraDevice = this.cameraDevice ?: return null
        val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
        return characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)
    }

    @VisibleForTesting
    internal fun getCameraAwbMaxRegions(): Int? {
        val cameraDevice = this.cameraDevice ?: return null
        val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
        return characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB)
    }

    @VisibleForTesting
    internal fun selectBestContinuousAfMode(): Int {
        val available = getCameraAvailableAfModes() ?: return CameraMetadata.CONTROL_AF_MODE_OFF
        return when {
            available.contains(CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE) ->
                CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            available.contains(CameraMetadata.CONTROL_AF_MODE_AUTO) ->
                CameraMetadata.CONTROL_AF_MODE_AUTO
            else ->
                CameraMetadata.CONTROL_AF_MODE_OFF
        }
    }

    @VisibleForTesting
    internal fun selectBestContinuousAeMode(): Int {
        val available = getCameraAvailableAeModes() ?: return CameraMetadata.CONTROL_AE_MODE_OFF
        return when {
            available.contains(CameraMetadata.CONTROL_AE_MODE_ON) ->
                CameraMetadata.CONTROL_AE_MODE_ON
            else ->
                CameraMetadata.CONTROL_AE_MODE_OFF
        }
    }

    @VisibleForTesting
    internal fun selectBestContinuousAwbMode(): Int {
        val available = getCameraAvailableAwbModes() ?: return CameraMetadata.CONTROL_AWB_MODE_OFF
        return when {
            available.contains(CameraMetadata.CONTROL_AWB_MODE_AUTO) ->
                CameraMetadata.CONTROL_AWB_MODE_AUTO
            else ->
                CameraMetadata.CONTROL_AWB_MODE_OFF
        }
    }

    @VisibleForTesting
    internal fun createDefaultCaptureRequestBuilder(
        listener: OnCameraReadyListener? = null,
        regions: Array<MeteringRectangle>? = null
    ): CaptureRequest.Builder? {
        val cameraDevice = this.cameraDevice
        if (cameraDevice == null) {
            releaseCameraAndReportException(
                listener = listener, message = "Camera device not available.",
                cause = NullPointerException()
            )
            return null
        }
        val surfaces = this.currentSurfaces
        if (surfaces == null) {
            releaseCameraAndReportException(
                listener = listener, message = "Surfaces not available.",
                cause = NullPointerException()
            )
            return null
        }
        val builder = try {
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        } catch (e: IllegalArgumentException) {
            releaseCameraAndReportException(
                listener = listener, message = "TemplateType is not supported by this device.",
                cause = e
            )
            return null
        }
        builder.set(CaptureRequest.FLASH_MODE, requestedFlashMode)

        if (regions != null) {
            val maxRegionsAf = getCameraAfMaxRegions()
            if (maxRegionsAf != null && maxRegionsAf > 0) {
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, regions)
            }
            val maxRegionsAe = getCameraAeMaxRegions()
            if (maxRegionsAe != null && maxRegionsAe > 0) {
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, regions)
            }
            val maxRegionsAwb = getCameraAwbMaxRegions()
            if (maxRegionsAwb != null && maxRegionsAwb > 0) {
                builder.set(CaptureRequest.CONTROL_AWB_REGIONS, regions)
            }
        }

        for (surface in surfaces) {
            builder.addTarget(surface)
        }
        return builder
    }

    private fun releaseCameraAndReportException(
        listener: OnCameraReadyListener?,
        message: String? = null,
        cause: Throwable? = null
    ) {
        release()
        val exception = when (cause) {
            is android.hardware.camera2.CameraAccessException ->
                CameraAccessException(message, cause)
            is CameraSessionException ->
                CameraSessionException(message, cause)
            else ->
                CameraException(message, cause)
        }
        Timber.e(exception, message)
        listener?.onCameraFailure(exception)
    }

    private fun releaseCameraAndReportException(
        listener: OnCameraReadyListener?,
        message: String? = null,
        exception: CameraException
    ) {
        release()
        Timber.e(exception, message)
        listener?.onCameraFailure(exception)
    }

    companion object {

        const val IMAGE_FORMAT = ImageFormat.YUV_420_888
    }
}
