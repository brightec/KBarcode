package uk.co.brightec.kbarcode.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Handler
import android.util.Size
import android.view.Surface
import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@SmallTest
internal class Camera2SourceTest {

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice

    private lateinit var cameraSource: Camera2Source

    @Before
    fun before() {
        cameraManager = mock()
        cameraDevice = mock {
            on { id } doReturn "some id"
        }

        cameraSource = spy(Camera2Source(cameraManager))
    }

    @Test
    fun nothing__isStarted__false() {
        // GIVEN
        // nothing

        // WHEN
        val result = cameraSource.isStarted()

        // THEN
        assertFalse(result)
    }

    @Test
    fun cameraDevice__isStarted__true() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice

        // WHEN
        val result = cameraSource.isStarted()

        // THEN
        assertTrue(result)
    }

    @Test
    fun nothing__isOpening__false() {
        // GIVEN
        // nothing

        // WHEN
        val result = cameraSource.isOpening()

        // THEN
        assertFalse(result)
    }

    @Test
    fun cameraOpening__isOpening__true() {
        // GIVEN
        cameraSource.cameraOpening = true

        // WHEN
        val result = cameraSource.isOpening()

        // THEN
        assertTrue(result)
    }

    @Test
    fun cameraDevice__release__closesCameraDevice() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice

        // WHEN
        cameraSource.release()

        // THEN
        verify(cameraDevice).close()
        assertNull(cameraSource.cameraDevice)
        assertFalse(cameraSource.cameraOpening)
    }

    @Test
    fun cameraDevice__start__doesNothing() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice

        // WHEN
        cameraSource.start(mock(), mock())

        // THEN
        verify(cameraManager, never()).openCamera(
            any(), any(), anyOrNull<Handler>()
        )
    }

    @Test
    fun cameraOpening__start__doesNothing() {
        // GIVEN
        cameraSource.cameraOpening = true

        // WHEN
        cameraSource.start(mock(), mock())

        // THEN
        verify(cameraManager, never()).openCamera(
            any(), any(), anyOrNull<Handler>()
        )
    }

    @Test
    fun noCameraId__start__error() {
        // GIVEN
        doReturn(null).whenever(cameraSource).selectCamera()

        // WHEN
        val listener = mock<OnCameraReadyListener>()
        cameraSource.start(mock(), listener)

        // THEN
        val captor = argumentCaptor<CameraException>()
        verify(listener).onCameraFailure(captor.capture())
        assertTrue(captor.firstValue is CameraServiceException)
    }

    @Test
    fun cameraId__start__setsOpening_opensCamera() {
        // GIVEN
        val cameraId = "some id"
        doReturn(cameraId).whenever(cameraSource).selectCamera()

        // WHEN
        cameraSource.start(mock(), mock())

        // THEN
        assertTrue(cameraSource.cameraOpening)
        verify(cameraManager).openCamera(
            eq(cameraId), any(), anyOrNull<Handler>()
        )
    }

    @Test
    fun cameraId_cameraOpened_sufacesInvalid__start__setsNotOpening_setCameraDevice_error() {
        doNothing().whenever(cameraSource).createCaptureSession(any(), any())

        // GIVEN
        val cameraId = "some id"
        doReturn(cameraId).whenever(cameraSource).selectCamera()
        whenever(cameraManager.openCamera(any(), any(), anyOrNull<Handler>())).then {
            val stateCallback = it.getArgument<CameraDevice.StateCallback>(1)
            stateCallback.onOpened(cameraDevice)
        }

        // THEN
        val surface = mock<Surface> {
            on { isValid } doReturn false
        }
        val surfaces = listOf(surface)
        val listener = mock<OnCameraReadyListener>()
        cameraSource.start(surfaces, listener)

        // THEN
        assertFalse(cameraSource.cameraOpening)
        verify(cameraSource).cameraDevice = cameraDevice
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun cameraId_cameraOpened__start__setsNotOpening_setCameraDevice_callListener_createSession() {
        doNothing().whenever(cameraSource).createCaptureSession(any(), any())

        // GIVEN
        val cameraId = "some id"
        doReturn(cameraId).whenever(cameraSource).selectCamera()
        whenever(cameraManager.openCamera(any(), any(), anyOrNull<Handler>())).then {
            val stateCallback = it.getArgument<CameraDevice.StateCallback>(1)
            stateCallback.onOpened(cameraDevice)
        }

        // THEN
        val surface = mock<Surface> {
            on { isValid } doReturn true
        }
        val surfaces = listOf(surface)
        val listener = mock<OnCameraReadyListener>()
        cameraSource.start(surfaces, listener)

        // THEN
        assertFalse(cameraSource.cameraOpening)
        verify(cameraSource).cameraDevice = cameraDevice
        verify(listener).onCameraReady()
        verify(cameraSource).createCaptureSession(surfaces, listener)
    }

    @Test
    fun cameraId_cameraDisconnected__start__release() {
        // GIVEN
        val cameraId = "some id"
        doReturn(cameraId).whenever(cameraSource).selectCamera()
        whenever(cameraManager.openCamera(any(), any(), anyOrNull<Handler>())).then {
            val stateCallback = it.getArgument<CameraDevice.StateCallback>(1)
            stateCallback.onDisconnected(cameraDevice)
        }

        // THEN
        cameraSource.start(mock(), mock())

        // THEN
        verify(cameraSource).release()
    }

    @Test
    fun cameraId_cameraError__start__release_callListener() {
        // GIVEN
        val cameraId = "some id"
        doReturn(cameraId).whenever(cameraSource).selectCamera()
        whenever(cameraManager.openCamera(any(), any(), anyOrNull<Handler>())).then {
            val stateCallback = it.getArgument<CameraDevice.StateCallback>(1)
            stateCallback.onError(cameraDevice, -1)
        }

        // THEN
        val listener = mock<OnCameraReadyListener>()
        cameraSource.start(mock(), listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun cameraDevice_sizes__getOutputSize__callsChooseOutputSize() {
        doReturn(Size(-1, -1)).whenever(cameraSource).chooseOutputSize(any(), any())

        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val sizes = arrayOf(Size(-1, -1), Size(-2, -2))
        val sizeMap = mock<StreamConfigurationMap> {
            on { getOutputSizes(any()) } doReturn sizes
        }
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) } doReturn sizeMap
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val minWidth = -1
        cameraSource.getOutputSize(minWidth)

        // THEN
        verify(cameraSource).chooseOutputSize(sizes, minWidth)
    }

    @Test
    fun characteristics__getCameraFacing__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val facing = -1
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.LENS_FACING) } doReturn facing
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraFacing()

        // THEN
        assertEquals(facing, result)
    }

    @Test
    fun characteristics__getCameraSensorOrientation__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val orientation = -1
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.SENSOR_ORIENTATION) } doReturn orientation
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraSensorOrientation()

        // THEN
        assertEquals(orientation, result)
    }

    @Test
    fun listInclFacing__selectCamera__isCorrect() {
        // GIVEN
        val cameraId1 = "1"
        val cameraCharacteristics1 = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.LENS_FACING) } doReturn 1
        }
        whenever(cameraManager.getCameraCharacteristics(cameraId1))
            .thenReturn(cameraCharacteristics1)
        val cameraId2 = "2"
        val cameraCharacteristics2 = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.LENS_FACING) } doReturn 2
        }
        whenever(cameraManager.getCameraCharacteristics(cameraId2))
            .thenReturn(cameraCharacteristics2)
        val ids = arrayOf(cameraId1, cameraId2)
        whenever(cameraManager.cameraIdList).thenReturn(ids)

        // WHEN
        cameraSource.requestedFacing = 2
        val result = cameraSource.selectCamera()

        // THEN
        assertEquals(cameraId2, result)
    }

    @Test
    fun listExclFacing__selectCamera__isCorrect() {
        // GIVEN
        val cameraId1 = "1"
        val cameraCharacteristics1 = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.LENS_FACING) } doReturn 1
        }
        whenever(cameraManager.getCameraCharacteristics(cameraId1))
            .thenReturn(cameraCharacteristics1)
        val cameraId2 = "2"
        val cameraCharacteristics2 = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.LENS_FACING) } doReturn 2
        }
        whenever(cameraManager.getCameraCharacteristics(cameraId2))
            .thenReturn(cameraCharacteristics2)
        val ids = arrayOf(cameraId1, cameraId2)
        whenever(cameraManager.cameraIdList).thenReturn(ids)

        // WHEN
        cameraSource.requestedFacing = 3
        val result = cameraSource.selectCamera()

        // THEN
        assertEquals(cameraId1, result)
    }

    @Test
    fun cameraDevice__createCaptureSession__createsCaptureSession() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        cameraSource.createCaptureSession(surfaces, mock())

        // THEN
        verify(cameraDevice).createCaptureSession(eq(surfaces), any(), anyOrNull())
    }

    @Test
    fun cameraDevice_configured__createCaptureSession__createCaptureRequest() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val session = mock<CameraCaptureSession>()
        whenever(cameraDevice.createCaptureSession(any(), any(), anyOrNull())).then {
            val stateCallback = it.getArgument<CameraCaptureSession.StateCallback>(1)
            stateCallback.onConfigured(session)
        }

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureSession(surfaces, listener)

        // THEN
        verify(cameraSource).createCaptureRequest(surfaces, listener, session)
    }

    @Test
    fun cameraDevice_configurationFailed__createCaptureSession__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val session = mock<CameraCaptureSession>()
        whenever(cameraDevice.createCaptureSession(any(), any(), anyOrNull())).then {
            val stateCallback = it.getArgument<CameraCaptureSession.StateCallback>(1)
            stateCallback.onConfigureFailed(session)
        }

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureSession(surfaces, listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any<CameraSessionException>())
    }

    @Test
    fun cameraDevice_cameraAccessException__createCaptureSession__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        whenever(cameraDevice.createCaptureSession(any(), any(), anyOrNull()))
            .doThrow(mock<android.hardware.camera2.CameraAccessException>())

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureSession(surfaces, listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any<CameraAccessException>())
    }

    @Test
    fun cameraDevice_illegalArgumentException__createCaptureSession__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        whenever(cameraDevice.createCaptureSession(any(), any(), anyOrNull()))
            .doThrow(mock<IllegalArgumentException>())

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureSession(surfaces, listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun cameraDevice_illegalStateException__createCaptureSession__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        whenever(cameraDevice.createCaptureSession(any(), any(), anyOrNull()))
            .doThrow(mock<IllegalStateException>())

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureSession(surfaces, listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun cameraDevice_session_autoFocus__createCaptureRequest__setsRepeatingRequest() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val session = mock<CameraCaptureSession>()
        val captureRequest = mock<CaptureRequest>()
        val captureRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn captureRequest
        }
        whenever(cameraDevice.createCaptureRequest(any())).thenReturn(captureRequestBuilder)
        val autoFocus = 1
        doReturn(autoFocus).whenever(cameraSource).selectBestAutoFocus()

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        cameraSource.createCaptureRequest(
            surfaces = surfaces, listener = mock(), session = session
        )

        // THEN
        verify(captureRequestBuilder).set(CaptureRequest.CONTROL_AF_MODE, autoFocus)
        verify(captureRequestBuilder).addTarget(surfaces[0])
        verify(captureRequestBuilder).addTarget(surfaces[1])
        verify(session).setRepeatingRequest(captureRequest, null, null)
    }

    @Test
    fun cameraDevice_session_captureIllegalArgExc__createCaptureRequest__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val session = mock<CameraCaptureSession>()
        whenever(cameraDevice.createCaptureRequest(any()))
            .doThrow(mock<IllegalArgumentException>())

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureRequest(
            surfaces = surfaces, listener = listener, session = session
        )

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun cameraDevice_session_captureCameraAccessExc__createCaptureRequest__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val session = mock<CameraCaptureSession>()
        whenever(cameraDevice.createCaptureRequest(any()))
            .doThrow(mock<android.hardware.camera2.CameraAccessException>())

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureRequest(
            surfaces = surfaces, listener = listener, session = session
        )

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any<CameraAccessException>())
    }

    @Test
    fun cameraDevice_session_captureIllegalStateExc__createCaptureRequest__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val session = mock<CameraCaptureSession>()
        whenever(cameraDevice.createCaptureRequest(any()))
            .doThrow(mock<IllegalStateException>())

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureRequest(
            surfaces = surfaces, listener = listener, session = session
        )

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun cameraDevice_session_requestCameraAccessExc__createCaptureRequest__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val autoFocus = 1
        doReturn(autoFocus).whenever(cameraSource).selectBestAutoFocus()
        val session = mock<CameraCaptureSession> {
            on {
                setRepeatingRequest(any(), anyOrNull(), anyOrNull())
            } doThrow mock<android.hardware.camera2.CameraAccessException>()
        }
        val captureRequest = mock<CaptureRequest>()
        val captureRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn captureRequest
        }
        whenever(cameraDevice.createCaptureRequest(any())).thenReturn(captureRequestBuilder)

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureRequest(
            surfaces = surfaces, listener = listener, session = session
        )

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any<CameraAccessException>())
    }

    @Test
    fun cameraDevice_session_requestIllegalArgExc__createCaptureRequest__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val autoFocus = 1
        doReturn(autoFocus).whenever(cameraSource).selectBestAutoFocus()
        val session = mock<CameraCaptureSession> {
            on {
                setRepeatingRequest(any(), anyOrNull(), anyOrNull())
            } doThrow mock<IllegalArgumentException>()
        }
        val captureRequest = mock<CaptureRequest>()
        val captureRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn captureRequest
        }
        whenever(cameraDevice.createCaptureRequest(any())).thenReturn(captureRequestBuilder)

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureRequest(
            surfaces = surfaces, listener = listener, session = session
        )

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun cameraDevice_session_requestIllegalStateExc__createCaptureRequest__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val autoFocus = 1
        doReturn(autoFocus).whenever(cameraSource).selectBestAutoFocus()
        val session = mock<CameraCaptureSession> {
            on {
                setRepeatingRequest(any(), anyOrNull(), anyOrNull())
            } doThrow mock<IllegalStateException>()
        }
        val captureRequest = mock<CaptureRequest>()
        val captureRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn captureRequest
        }
        whenever(cameraDevice.createCaptureRequest(any())).thenReturn(captureRequestBuilder)

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureRequest(
            surfaces = surfaces, listener = listener, session = session
        )

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun sizesWithAboveAndBelow_minWidth__chooseOutputSize__lowestAboveMin() {
        // GIVEN
        val size1 = mock<Size> {
            on { width } doReturn 1
        }
        val size2 = mock<Size> {
            on { width } doReturn 2
        }
        val size3 = mock<Size> {
            on { width } doReturn 4
        }
        val size4 = mock<Size> {
            on { width } doReturn 5
        }
        val sizes = arrayOf(size1, size2, size3, size4)
        val minWidth = 3

        // WHEN
        val result = cameraSource.chooseOutputSize(sizes, minWidth)

        // THEN
        assertEquals(size3, result)
    }

    @Test
    fun sizesWithAboveOnly_minWidth__chooseOutputSize__lowest() {
        // GIVEN
        val size1 = mock<Size> {
            on { width } doReturn 2
        }
        val size2 = mock<Size> {
            on { width } doReturn 3
        }
        val sizes = arrayOf(size1, size2)
        val minWidth = 1

        // WHEN
        val result = cameraSource.chooseOutputSize(sizes, minWidth)

        // THEN
        assertEquals(size1, result)
    }

    @Test
    fun sizesWithBelowOnly_minWidth__chooseOutputSize__highest() {
        // GIVEN
        val size1 = mock<Size> {
            on { width } doReturn 1
        }
        val size2 = mock<Size> {
            on { width } doReturn 2
        }
        val sizes = arrayOf(size1, size2)
        val minWidth = 3

        // WHEN
        val result = cameraSource.chooseOutputSize(sizes, minWidth)

        // THEN
        assertEquals(size2, result)
    }

    @Test
    fun cameraInUseError__createExceptionFromCameraDeviceError__cameraInUseException() {
        // GIVEN
        val cameraDeviceError = CameraDevice.StateCallback.ERROR_CAMERA_IN_USE

        // WHEN
        val result = cameraSource.createExceptionFromCameraDeviceError(cameraDeviceError)

        // THEN
        assertTrue(result is CameraInUseException)
    }

    @Test
    fun maxCamerasInUseError__createExceptionFromCameraDeviceError__maxCamerasInUseException() {
        // GIVEN
        val cameraDeviceError = CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE

        // WHEN
        val result = cameraSource.createExceptionFromCameraDeviceError(cameraDeviceError)

        // THEN
        assertTrue(result is MaxCamerasInUseException)
    }

    @Test
    fun cameraDisabledError__createExceptionFromCameraDeviceError__cameraDisabledException() {
        // GIVEN
        val cameraDeviceError = CameraDevice.StateCallback.ERROR_CAMERA_DISABLED

        // WHEN
        val result = cameraSource.createExceptionFromCameraDeviceError(cameraDeviceError)

        // THEN
        assertTrue(result is CameraDisabledException)
    }

    @Test
    fun cameraDeviceError__createExceptionFromCameraDeviceError__cameraDeviceException() {
        // GIVEN
        val cameraDeviceError = CameraDevice.StateCallback.ERROR_CAMERA_DEVICE

        // WHEN
        val result = cameraSource.createExceptionFromCameraDeviceError(cameraDeviceError)

        // THEN
        assertTrue(result is CameraDeviceException)
    }

    @Test
    fun cameraServiceError__createExceptionFromCameraDeviceError__cameraServiceException() {
        // GIVEN
        val cameraDeviceError = CameraDevice.StateCallback.ERROR_CAMERA_SERVICE

        // WHEN
        val result = cameraSource.createExceptionFromCameraDeviceError(cameraDeviceError)

        // THEN
        assertTrue(result is CameraServiceException)
    }

    @Test
    fun continuousAvailable__selectBestAutoFocus__continuous() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val available = IntArray(1).apply {
            this[0] = CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        }
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES) } doReturn available
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.selectBestAutoFocus()

        // THEN
        assertEquals(CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE, result)
    }

    @Test
    fun autoAvailable__selectBestAutoFocus__auto() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val available = IntArray(1).apply {
            this[0] = CameraMetadata.CONTROL_AF_MODE_AUTO
        }
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES) } doReturn available
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.selectBestAutoFocus()

        // THEN
        assertEquals(CameraMetadata.CONTROL_AF_MODE_AUTO, result)
    }

    @Test
    fun available__selectBestAutoFocus__off() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val available = IntArray(1)
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES) } doReturn available
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.selectBestAutoFocus()

        // THEN
        assertEquals(CameraMetadata.CONTROL_AF_MODE_OFF, result)
    }
}
