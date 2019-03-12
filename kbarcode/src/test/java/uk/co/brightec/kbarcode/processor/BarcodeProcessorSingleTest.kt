package uk.co.brightec.kbarcode.processor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.filters.SmallTest
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import uk.co.brightec.kbarcode.Barcode

@SmallTest
internal class BarcodeProcessorSingleTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var detector: FirebaseVisionBarcodeDetector
    private lateinit var firebaseVision: FirebaseVision

    private lateinit var processor: BarcodeProcessorSingle

    @Before
    fun before() {
        detector = mock()
        firebaseVision = mock {
            on { getVisionBarcodeDetector(any()) } doReturn detector
        }

        processor = spy(BarcodeProcessorSingle(firebaseVision = firebaseVision))
    }

    @Test
    fun nothing__setFormats__setsDetector() {
        // GIVEN
        // nothing

        // WHEN
        processor.formats = arrayOf(1, 2)

        // THEN
        verify(processor).detector = any()
    }

    @Test
    fun nothing__setDetector__closesPrevious() {
        // GIVEN
        // nothing

        // WHEN
        processor.detector = mock()

        // THEN
        verify(detector).close()
    }

    @Test
    fun nothing__stop__closesDetector() {
        // GIVEN
        // nothing

        // WHEN
        processor.stop()

        // THEN
        verify(detector).close()
    }

    @Test
    fun image__detectInImage__callsDetector() {
        doReturn(mock<Task<List<FirebaseVisionBarcode>>>()).whenever(detector).detectInImage(any())

        // GIVEN
        val image = mock<FirebaseVisionImage>()

        // WHEN
        processor.detectInImage(image)

        // THEN
        verify(detector).detectInImage(image)
    }

    @Test
    fun nothing__onSuccess__publishValue() {
        // GIVEN
        // nothing

        // WHEN
        val observer = mock<Observer<List<Barcode>>>()
        processor.barcodes.observeForever(observer)
        val result1 = mock<FirebaseVisionBarcode>()
        val result2 = mock<FirebaseVisionBarcode>()
        val results = listOf(result1, result2)
        processor.onSuccess(results, mock())

        // THEN
        verify(observer).onChanged(listOf(Barcode(result1), Barcode(result2)))
    }

    @Test
    fun sort__onSuccess__usesSort_publishValue() {
        // GIVEN
        val comparator = mock<Comparator<Barcode>>()
        processor.barcodesSort = comparator

        // WHEN
        val observer = mock<Observer<List<Barcode>>>()
        processor.barcodes.observeForever(observer)
        val result1 = mock<FirebaseVisionBarcode>()
        val result2 = mock<FirebaseVisionBarcode>()
        val results = listOf(result1, result2)
        processor.onSuccess(results, mock())

        // THEN
        verify(comparator).compare(any(), any())
        verify(observer).onChanged(listOf(Barcode(result1), Barcode(result2)))
    }

    @Test
    fun formats__createDetector__creates() {
        reset(firebaseVision) // createDetector called once in init
        doReturn(detector).whenever(firebaseVision).getVisionBarcodeDetector(any())

        // GIVEN
        // nothing

        // WHEN
        processor.createDetector()

        // THEN
        verify(firebaseVision).getVisionBarcodeDetector(any())
    }
}
