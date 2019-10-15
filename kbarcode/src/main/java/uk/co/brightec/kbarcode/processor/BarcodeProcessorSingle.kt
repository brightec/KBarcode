package uk.co.brightec.kbarcode.processor

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import timber.log.Timber
import uk.co.brightec.kbarcode.Barcode
import uk.co.brightec.kbarcode.camera.FrameMetadata
import uk.co.brightec.kbarcode.processor.base.VisionImageProcessorSingleBase
import uk.co.brightec.kbarcode.processor.sort.BarcodeComparator

internal class BarcodeProcessorSingle(
    private val firebaseVision: FirebaseVision = FirebaseVision.getInstance(),
    formats: IntArray = intArrayOf(
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8
    ),
    var barcodesSort: Comparator<Barcode>? = null
) : VisionImageProcessorSingleBase<List<FirebaseVisionBarcode>>() {

    var formats: IntArray = formats
        set(value) {
            field = value
            detector = createDetector()
        }
    @VisibleForTesting
    internal var detector: FirebaseVisionBarcodeDetector = createDetector()
        set(value) {
            field.close()
            field = value
        }

    private val _barcodes = MutableLiveData<List<Barcode>>()
    val barcodes: LiveData<List<Barcode>>
        get() = _barcodes

    override fun stop() {
        super.stop()
        detector.close()
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionBarcode>> =
        detector.detectInImage(image)

    override fun onSuccess(
        results: List<FirebaseVisionBarcode>,
        frameMetadata: FrameMetadata
    ) {
        val barcodes = results.map { Barcode(it) }
        val resultSort = this.barcodesSort
        if (resultSort != null) {
            if (resultSort is BarcodeComparator) {
                resultSort.frameMetadata = frameMetadata
            }
            barcodes.sortedWith(resultSort)
        }
        _barcodes.value = barcodes
    }

    override fun onFailure(e: Exception) {
        Timber.e(e, "Barcode processing error")
    }

    @VisibleForTesting
    internal fun createDetector(): FirebaseVisionBarcodeDetector {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        if (formats.size > 1) {
            @Suppress("SpreadOperator") // Required by Firebase API
            options.setBarcodeFormats(
                formats[0], *formats.slice(IntRange(1, formats.size - 1)).toIntArray()
            )
        } else if (formats.size == 1) {
            options.setBarcodeFormats(formats[0])
        }
        return firebaseVision.getVisionBarcodeDetector(options.build())
    }
}
