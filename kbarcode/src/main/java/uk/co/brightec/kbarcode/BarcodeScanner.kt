package uk.co.brightec.kbarcode

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.media.ImageReader
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.Px
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import timber.log.Timber
import uk.co.brightec.kbarcode.camera.Camera2Source
import uk.co.brightec.kbarcode.camera.CameraException
import uk.co.brightec.kbarcode.camera.FrameMetadata
import uk.co.brightec.kbarcode.camera.OnCameraErrorListener
import uk.co.brightec.kbarcode.camera.OnCameraReadyListener
import uk.co.brightec.kbarcode.extension.BARCODE_FORMAT_ALL_MIN_WIDTH
import uk.co.brightec.kbarcode.extension.getMinWidth
import uk.co.brightec.kbarcode.processor.BarcodeProcessorSingle
import uk.co.brightec.kbarcode.processor.OnBarcodeListener
import uk.co.brightec.kbarcode.processor.OnBarcodesListener
import uk.co.brightec.kbarcode.util.OpenForTesting

@OpenForTesting
class BarcodeScanner internal constructor(
    private val cameraSource: Camera2Source,
    private val windowManager: WindowManager,
    private val frameProcessor: BarcodeProcessorSingle = BarcodeProcessorSingle()
) : KBarcode.Scanner {

    override var onBarcodesListener: OnBarcodesListener? = null
    override var onBarcodeListener: OnBarcodeListener? = null
    private val _barcodes = MutableLiveData<List<Barcode>>()
    override val barcodes: LiveData<List<Barcode>>
        get() = _barcodes
    private val _barcode = MutableLiveData<Barcode>()
    override val barcode: LiveData<Barcode>
        get() = _barcode
    override var onCameraErrorListener: OnCameraErrorListener? = null

    @VisibleForTesting
    internal var pauseProcessing = false

    @VisibleForTesting
    internal var imageReader: ImageReader? = null

    @VisibleForTesting
    internal val customSurfaces = arrayListOf<Surface>()
    private val barcodesObserver = Observer<List<Barcode>> { barcodes ->
        if (barcodes.isNotEmpty()) {
            onBarcodesListener?.onBarcodes(barcodes)
            onBarcodeListener?.onBarcode(barcodes.first())
            _barcodes.value = barcodes
            _barcode.value = barcodes.first()

            val builder = StringBuilder()
            for (barcode in barcodes) {
                builder.append(barcode.displayValue).append("; ")
            }
            Timber.v("Barcodes - $builder")
        } else {
            Timber.v("No barcodes found")
        }
    }

    @VisibleForTesting
    internal var customMinBarcodeWidth: Int? = null

    constructor(context: Context) : this(
        cameraSource = Camera2Source(context),
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    )

    init {
        frameProcessor.onImageProcessed = { it.close() }
    }

    fun addSurface(surface: Surface) {
        val cameraWasStarted = cameraSource.isStarted()
        if (cameraWasStarted) {
            release()
        }
        customSurfaces.add(surface)
        if (cameraWasStarted) {
            @Suppress("MissingPermission") // Already been started manually
            start()
        }
    }

    @RequiresPermission(android.Manifest.permission.CAMERA)
    override fun start() {
        pauseProcessing = false
        if (cameraSource.isStarted()) {
            Timber.d("Attempted Camera2Source.start(), which has already been started")
            return
        }
        if (cameraSource.isOpening()) {
            Timber.d("Attempted Camera2Source.start(), which is currently opening")
            return
        }

        frameProcessor.barcodes.observeForever(barcodesObserver)
        startCameraSource()
    }

    override fun resume() {
        pauseProcessing = false
    }

    override fun pause() {
        pauseProcessing = true
    }

    override fun release() {
        frameProcessor.barcodes.removeObserver(barcodesObserver)
        frameProcessor.stop()
        imageReader?.close()
        cameraSource.release()
    }

    override fun setCameraFacing(facing: Int) {
        val cameraWasStarted = cameraSource.isStarted()
        if (cameraWasStarted) {
            release()
        }
        cameraSource.requestedFacing = facing
        if (cameraWasStarted) {
            @Suppress("MissingPermission") // Already been started manually
            start()
        }
    }

    override fun setBarcodeFormats(formats: IntArray) {
        frameProcessor.formats = formats
    }

    override fun setMinBarcodeWidth(minBarcodeWidth: Int?) {
        customMinBarcodeWidth = if (minBarcodeWidth ?: -1 < 0) {
            null
        } else {
            minBarcodeWidth
        }
    }

    override fun setBarcodesSort(comparator: Comparator<Barcode>?) {
        frameProcessor.barcodesSort = comparator
    }

    // TODO : alistairsykes 08/03/2019 : Discuss with nickholcombe - Should this be here
    override fun setScaleType(@BarcodeView.ScaleType scaleType: Int) {
        Timber.v("ScaleType has no affect on ${BarcodeScanner::class.java.simpleName}")
    }

    fun getOutputSize(): Size? = cameraSource.getOutputSize(minWidthForBarcodes())

    @VisibleForTesting
    @RequiresPermission(android.Manifest.permission.CAMERA)
    internal fun startCameraSource() {
        val surface = createProcessorSurface()
        val surfaces = arrayListOf(surface).apply {
            this.addAll(customSurfaces)
        }

        cameraSource.start(
            surfaces = surfaces,
            listener = object : OnCameraReadyListener {
                override fun onCameraReady() {
                    // no-op
                }

                override fun onCameraFailure(e: CameraException) {
                    onCameraErrorListener?.onCameraError(e)
                }
            })
    }

    @Suppress("ReturnCount") // For readability
    @VisibleForTesting
    internal fun createProcessorSurface(): Surface {
        val size =
            cameraSource.getOutputSize(minWidthForBarcodes()) ?: throw IllegalStateException()
        val reader = createProcessorImageReader(size)
        reader.setOnImageAvailableListener({
            val image = it.acquireLatestImage() ?: return@setOnImageAvailableListener

            if (frameProcessor.isProcessing() || pauseProcessing) {
                image.close()
                return@setOnImageAvailableListener
            }

            val frameMetadata = FrameMetadata(
                width = it.width,
                height = it.height,
                rotation = getRotationCompensation(),
                cameraFacing = cameraSource.getCameraFacing()
            )
            frameProcessor.process(image, frameMetadata)
        }, null)
        imageReader = reader
        return reader.surface
    }

    @VisibleForTesting
    internal fun createProcessorImageReader(size: Size) = ImageReader.newInstance(
        size.width, size.height, Camera2Source.IMAGE_FORMAT, MAX_IMAGES_IN_READER
    )

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     *
     * Source: https://developers.google.com/ml-kit/vision/barcode-scanning/android
     */
    @Suppress("MagicNumber", "ReturnCount") // Intentional use of numbers. Returns for readability
    @VisibleForTesting
    internal fun getRotationCompensation(): Int {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        val deviceRotation = windowManager.defaultDisplay.rotation
        val rotationCompensation = ORIENTATIONS[deviceRotation]!!

        // Get the device's sensor orientation.
        val sensorOrientation = cameraSource.getCameraSensorOrientation() ?: 0

        val isFrontFacing =
            cameraSource.getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT
        return if (isFrontFacing) {
            (sensorOrientation + rotationCompensation) % 360
        } else { // back-facing
            (sensorOrientation - rotationCompensation + 360) % 360
        }
    }

    /**
     * Find the minimum pixel width required for your barcode formats
     *
     * We use BARCODE_SCREEN_PROPORTION as a rough estimate for how much of the screen will be
     * occupied by the barcode
     */
    @VisibleForTesting
    @Px
    internal fun minWidthForBarcodes(): Int {
        var minWidth = customMinBarcodeWidth
        if (minWidth == null) {
            val maxSizedFormat = frameProcessor.formats.maxByOrNull { it.getMinWidth() }
            minWidth = maxSizedFormat?.getMinWidth() ?: BARCODE_FORMAT_ALL_MIN_WIDTH
        }
        val minWidthForBarcodes = (minWidth / BARCODE_SCREEN_PROPORTION).toInt()
        Timber.v("minWidthForBarcodes() = $minWidthForBarcodes")
        return minWidthForBarcodes
    }

    companion object {

        @VisibleForTesting
        internal const val BARCODE_SCREEN_PROPORTION = 0.3
        private const val MAX_IMAGES_IN_READER = 3
        private val ORIENTATIONS = HashMap<Int, Int>().apply {
            this[Surface.ROTATION_0] = 0
            this[Surface.ROTATION_90] = 90
            this[Surface.ROTATION_180] = 180
            this[Surface.ROTATION_270] = 270
        }
    }
}
