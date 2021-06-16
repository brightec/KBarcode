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

public class KBarcode {

    @Suppress("TooManyFunctions") // This class does still feels single responsibility
    public interface Scanner : LifecycleObserver {

        public var onBarcodesListener: OnBarcodesListener?
        public var onBarcodeListener: OnBarcodeListener?
        public val barcodes: LiveData<List<Barcode>>
        public val barcode: LiveData<Barcode>
        public var onCameraErrorListener: OnCameraErrorListener?

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public fun start()

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        public fun resume()

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        public fun pause()

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public fun release()

        public fun setCameraFacing(facing: Int)

        public fun setCameraFlashMode(flashMode: Int)

        public fun setBarcodeFormats(formats: IntArray)

        public fun setMinBarcodeWidth(@Px minBarcodeWidth: Int?)

        public fun setBarcodesSort(comparator: Comparator<Barcode>?)

        public fun setScaleType(@BarcodeView.ScaleType scaleType: Int)

        public fun setClearFocusDelay(delay: Long)

        public fun setOptions(options: Options) {
            setCameraFacing(options.cameraFacing)
            setCameraFlashMode(options.cameraFlashMode)
            setBarcodeFormats(options.barcodeFormats)
            setMinBarcodeWidth(options.minBarcodeWidth)
            setBarcodesSort(options.barcodesSort)
            setScaleType(options.scaleType)
            setClearFocusDelay(options.clearFocusDelay)
        }
    }

    public companion object {

        @JvmStatic
        public fun setDebugging(debugging: Boolean) {
            if (debugging) {
                Timber.plant(Timber.DebugTree())
            }
        }
    }
}
