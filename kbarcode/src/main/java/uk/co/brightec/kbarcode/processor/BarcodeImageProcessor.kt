package uk.co.brightec.kbarcode.processor

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import timber.log.Timber
import uk.co.brightec.kbarcode.Barcode
import uk.co.brightec.kbarcode.camera.FrameMetadata
import uk.co.brightec.kbarcode.processor.base.ImageProcessorBase
import uk.co.brightec.kbarcode.processor.sort.BarcodeComparator
import uk.co.brightec.kbarcode.util.OpenForTesting
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

@OpenForTesting
internal class BarcodeImageProcessor(
    formats: IntArray = intArrayOf(
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8
    ),
    var barcodesSort: Comparator<Barcode>? = null
) : ImageProcessorBase<List<MlBarcode>>() {

    var formats: IntArray = formats
        set(value) {
            field = value
            scanner = createScanner()
        }

    @VisibleForTesting
    internal var scanner: BarcodeScanner = createScanner()
        set(value) {
            field.close()
            field = value
        }

    private val _barcodes = MutableLiveData<List<Barcode>>()
    val barcodes: LiveData<List<Barcode>>
        get() = _barcodes

    override fun stop() {
        super.stop()
        scanner.close()
        _barcodes.value = emptyList()
    }

    override fun detectInImage(image: InputImage): Task<List<MlBarcode>> =
        scanner.process(image)

    override fun onSuccess(
        results: List<MlBarcode>,
        frameMetadata: FrameMetadata
    ) {
        val barcodes = results.map { Barcode(it) }
        val resultSort = this.barcodesSort
        if (resultSort != null) {
            if (resultSort is BarcodeComparator) {
                resultSort.frameMetadata = frameMetadata
            }
            _barcodes.value = barcodes.sortedWith(resultSort)
        } else {
            _barcodes.value = barcodes
        }
    }

    override fun onFailure(e: Exception) {
        Timber.e(e, "Barcode processing error")
    }

    @VisibleForTesting
    internal fun createScanner(): BarcodeScanner {
        val options = BarcodeScannerOptions.Builder()
        if (formats.size > 1) {
            @Suppress("SpreadOperator") // Required by Google API
            options.setBarcodeFormats(
                formats[0], *formats.slice(IntRange(1, formats.size - 1)).toIntArray()
            )
        } else if (formats.size == 1) {
            options.setBarcodeFormats(formats[0])
        }
        return BarcodeScanning.getClient(options.build())
    }
}
