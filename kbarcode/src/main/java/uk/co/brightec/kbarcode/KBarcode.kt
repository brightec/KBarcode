package uk.co.brightec.kbarcode

import androidx.annotation.Px
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.OnLifecycleEvent
import timber.log.Timber
import uk.co.brightec.kbarcode.camera.OnCameraErrorListener
import uk.co.brightec.kbarcode.processor.OnBarcodeListener
import uk.co.brightec.kbarcode.processor.OnBarcodesListener

class KBarcode {

    @Suppress("TooManyFunctions") // This class does still feels single responsibility
    interface Scanner : LifecycleObserver {

        var onBarcodesListener: OnBarcodesListener?
        var onBarcodeListener: OnBarcodeListener?
        val barcodes: LiveData<List<Barcode>>
        val barcode: LiveData<Barcode>
        var onCameraErrorListener: OnCameraErrorListener?

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun start()

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun resume()

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun pause()

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun release()

        fun setCameraFacing(facing: Int)

        fun setCameraFlashMode(flashMode: Int)

        fun setBarcodeFormats(formats: IntArray)

        fun setMinBarcodeWidth(@Px minBarcodeWidth: Int?)

        fun setBarcodesSort(comparator: Comparator<Barcode>?)

        fun setScaleType(@BarcodeView.ScaleType scaleType: Int)

        fun setClearFocusDelay(delay: Long)

        fun setOptions(options: Options) {
            setCameraFacing(options.cameraFacing)
            setCameraFlashMode(options.cameraFlashMode)
            setBarcodeFormats(options.barcodeFormats)
            setMinBarcodeWidth(options.minBarcodeWidth)
            setBarcodesSort(options.barcodesSort)
            setScaleType(options.scaleType)
            setClearFocusDelay(options.clearFocusDelay)
        }
    }

    companion object {

        @JvmStatic
        fun setDebugging(debugging: Boolean) {
            if (debugging) {
                Timber.plant(Timber.DebugTree())
            }
        }
    }
}
