package uk.co.brightec.kbarcode

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import timber.log.Timber
import uk.co.brightec.kbarcode.camera.OnCameraErrorListener
import uk.co.brightec.kbarcode.processor.OnBarcodeListener
import uk.co.brightec.kbarcode.processor.OnBarcodesListener
import uk.co.brightec.kbarcode.processor.sort.CentralBarcodeComparator
import uk.co.brightec.kbarcode.util.OpenForTesting

@OpenForTesting
public class BarcodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val barcodeScanner: BarcodeScanner = BarcodeScanner(context)
) : ViewGroup(context, attrs, defStyleAttr), KBarcode.Scanner {

    override var onBarcodesListener: OnBarcodesListener?
        get() = barcodeScanner.onBarcodesListener
        set(value) {
            barcodeScanner.onBarcodesListener = value
        }
    override var onBarcodeListener: OnBarcodeListener?
        get() = barcodeScanner.onBarcodeListener
        set(value) {
            barcodeScanner.onBarcodeListener = value
        }
    override val barcodes: LiveData<List<Barcode>>
        get() = barcodeScanner.barcodes
    override val barcode: LiveData<Barcode>
        get() = barcodeScanner.barcode
    override var onCameraErrorListener: OnCameraErrorListener?
        get() = barcodeScanner.onCameraErrorListener
        set(value) {
            barcodeScanner.onCameraErrorListener = value
        }

    private val surfaceView = SurfaceView(context)

    @ScaleType
    private var _previewScaleType: Int = CENTER_INSIDE
        set(value) {
            field = value
            requestLayout()
        }
    private val surfaceAvailable = MutableLiveData<Boolean>()
    private val surfaceAvailableObserver = object : Observer<Boolean> {
        override fun onChanged(available: Boolean) {
            if (!available) return

            surfaceAvailable.removeObserver(this)
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                @Suppress("MissingPermission") // Lint mistake
                barcodeScanner.start()
            } else {
                Timber.e("Attempted BarcodeScanner.start() without camera permission")
            }
        }
    }

    init {
        @Suppress("MagicNumber") // Defined in attrs.xml
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BarcodeView,
            defStyleAttr,
            0
        ).apply {
            try {
                val attrFacing = getInteger(R.styleable.BarcodeView_cameraFacing, 1)
                setCameraFacing(cameraFacingAttrConvert(attrFacing))
                val attrFlashMode = getInteger(R.styleable.BarcodeView_cameraFlashMode, 0)
                setCameraFlashMode(cameraFlashModeAttrConvert(attrFlashMode))
                val attrFormats = getInteger(R.styleable.BarcodeView_formats, 0)
                setBarcodeFormats(formatsAttrConvert(attrFormats))
                val attrMinBarcodeWidth = getInteger(R.styleable.BarcodeView_minBarcodeWidth, -1)
                setMinBarcodeWidth(attrMinBarcodeWidth)
                val attrSort = getInteger(R.styleable.BarcodeView_sort, 0)
                setBarcodesSort(sortAttrConvert(attrSort))
                val attrScaleType = getInteger(R.styleable.BarcodeView_previewScaleType, 0)
                setPreviewScaleType(previewScaleTypeAttrConvert(attrScaleType))
                val attrClearFocusDelay = getInteger(
                    R.styleable.BarcodeView_clearFocusDelay,
                    CLEAR_FOCUS_DELAY_DEFAULT.toInt()
                )
                setClearFocusDelay(attrClearFocusDelay.toLong())
            } finally {
                recycle()
            }
        }

        val surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                surfaceAvailable.value = true
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                // no-op
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                surfaceAvailable.value = false
                release()
            }
        })
        barcodeScanner.addSurface(surfaceHolder.surface)
        @Suppress("LeakingThis") // Intentional invocation
        addView(surfaceView)

        // Add touch listener to request focus on touch
        surfaceView.setOnTouchListener { _, motionEvent ->
            val actionMasked = motionEvent.actionMasked
            if (actionMasked != MotionEvent.ACTION_DOWN) {
                return@setOnTouchListener false
            }

            barcodeScanner.requestCameraFocus(
                viewWidth = surfaceView.width, viewHeight = surfaceView.height,
                touchX = motionEvent.x, touchY = motionEvent.y
            )
            true
        }
    }

    override fun start() {
        // We remove and add here to ensure the observer gets called even if the value is the same
        surfaceAvailable.removeObserver(surfaceAvailableObserver)
        surfaceAvailable.observeForever(surfaceAvailableObserver)
    }

    override fun resume() {
        barcodeScanner.resume()
    }

    override fun pause() {
        barcodeScanner.pause()
    }

    override fun release() {
        barcodeScanner.release()
        surfaceAvailable.removeObserver(surfaceAvailableObserver)
    }

    override fun setCameraFacing(facing: Int) {
        barcodeScanner.setCameraFacing(facing)
    }

    override fun setCameraFlashMode(flashMode: Int) {
        barcodeScanner.setCameraFlashMode(flashMode)
    }

    override fun setBarcodeFormats(formats: IntArray) {
        barcodeScanner.setBarcodeFormats(formats)
    }

    override fun setMinBarcodeWidth(minBarcodeWidth: Int?) {
        barcodeScanner.setMinBarcodeWidth(minBarcodeWidth)
    }

    override fun setBarcodesSort(comparator: Comparator<Barcode>?) {
        barcodeScanner.setBarcodesSort(comparator)
    }

    override fun setPreviewScaleType(scaleType: Int) {
        _previewScaleType = scaleType
    }

    override fun setClearFocusDelay(delay: Long) {
        barcodeScanner.setClearFocusDelay(delay)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val childRect = calculateRectForChild(
            scaleType = _previewScaleType, left = left, top = top, right = right, bottom = bottom
        )

        for (i in 0 until childCount) {
            getChildAt(i).layout(childRect.left, childRect.top, childRect.right, childRect.bottom)
        }
    }

    @VisibleForTesting
    internal fun cameraFacingAttrConvert(attr: Int) = when (attr) {
        0 -> CameraCharacteristics.LENS_FACING_FRONT
        1 -> CameraCharacteristics.LENS_FACING_BACK
        2 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraCharacteristics.LENS_FACING_EXTERNAL
        } else {
            CameraCharacteristics.LENS_FACING_BACK
        }
        else -> CameraCharacteristics.LENS_FACING_BACK
    }

    @VisibleForTesting
    internal fun cameraFlashModeAttrConvert(attr: Int) = when (attr) {
        0 -> CameraMetadata.FLASH_MODE_OFF
        1 -> CameraMetadata.FLASH_MODE_SINGLE
        2 -> CameraMetadata.FLASH_MODE_TORCH
        else -> CameraMetadata.FLASH_MODE_OFF
    }

    @Suppress("MagicNumber") // Intended magic number, purpose of method
    @VisibleForTesting
    internal fun formatsAttrConvert(attr: Int) = when (attr) {
        0 -> intArrayOf(Barcode.FORMAT_ALL_FORMATS)
        1 -> intArrayOf(
            Barcode.FORMAT_CODABAR, Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E
        )
        2 -> intArrayOf(
            Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_93
        )
        3 -> intArrayOf(
            Barcode.FORMAT_DATA_MATRIX, Barcode.FORMAT_QR_CODE, Barcode.FORMAT_PDF417,
            Barcode.FORMAT_AZTEC
        )
        4 -> intArrayOf(
            Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8
        )
        else -> intArrayOf(Barcode.FORMAT_ALL_FORMATS)
    }

    @VisibleForTesting
    internal fun sortAttrConvert(attr: Int) = when (attr) {
        0 -> null
        1 -> CentralBarcodeComparator()
        else -> null
    }

    @VisibleForTesting
    internal fun previewScaleTypeAttrConvert(attr: Int) = when (attr) {
        0 -> CENTER_INSIDE
        1 -> CENTER_CROP
        else -> CENTER_INSIDE
    }

    @VisibleForTesting
    internal fun calculateRectForChild(
        @ScaleType scaleType: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): Rect {
        @Suppress("MagicNumber") // Make assumption if unknown
        var width = 320

        @Suppress("MagicNumber") // Make assumption if unknown
        var height = 240
        val size = barcodeScanner.getOutputSize()
        if (size != null) {
            width = size.width
            height = size.height
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            val tmp = width
            width = height
            height = tmp
        }

        val layoutWidth = right - left
        val layoutHeight = bottom - top

        // Computes height and width for potentially doing fit width.
        var childWidth = layoutWidth
        var childHeight = (layoutWidth.toFloat() / width.toFloat() * height).toInt()

        if (scaleType == CENTER_INSIDE) {
            // If height is too tall using fit width, do fit height instead.
            if (childHeight > layoutHeight) {
                childHeight = layoutHeight
                childWidth = (layoutHeight.toFloat() / height.toFloat() * width).toInt()
            }
        } else if (scaleType == CENTER_CROP) {
            // If height is too short using fit width, do fit height instead.
            if (childHeight < layoutHeight) {
                childHeight = layoutHeight
                childWidth = (layoutHeight.toFloat() / height.toFloat() * width).toInt()
            }
        }

        val horizontalMargin = (layoutWidth - childWidth) / 2

        @Suppress("UnnecessaryVariable") // For clarity
        val childLeft = horizontalMargin
        val childRight = layoutWidth - horizontalMargin
        val verticalMargin = (layoutHeight - childHeight) / 2

        @Suppress("UnnecessaryVariable") // For clarity
        val childTop = verticalMargin
        val childBottom = layoutHeight - verticalMargin

        return Rect(childLeft, childTop, childRight, childBottom)
    }

    @VisibleForTesting
    internal fun isPortraitMode(): Boolean {
        return when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> false
            Configuration.ORIENTATION_PORTRAIT -> true
            else -> false
        }
    }

    public companion object {

        /**
         * Scale the surface uniformly (maintain it's aspect ratio) so that both dimensions
         * (width and height) of the surface will be equal to or less than the corresponding
         * dimension of the parent BarcodeView.
         */
        public const val CENTER_INSIDE: Int = 0

        /**
         * Scale the surface uniformly (maintain it's aspect ratio) so that both dimensions
         * (width and height) of the surface will be equal to or larger than the corresponding
         * dimension of the parent BarcodeView.
         */
        public const val CENTER_CROP: Int = 1

        public const val CLEAR_FOCUS_DELAY_DEFAULT: Long = 5000L // Millis
        public const val CLEAR_FOCUS_DELAY_NEVER: Long = -1L
    }

    @IntDef(
        CENTER_INSIDE,
        CENTER_CROP
    )
    @Retention(AnnotationRetention.SOURCE)
    public annotation class ScaleType
}
