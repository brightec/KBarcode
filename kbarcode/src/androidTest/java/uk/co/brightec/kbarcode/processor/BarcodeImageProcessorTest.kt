package uk.co.brightec.kbarcode.processor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.filters.MediumTest
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.co.brightec.kbarcode.Barcode
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

@MediumTest
internal class BarcodeImageProcessorTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var scanner: BarcodeScanner

    private lateinit var processor: BarcodeImageProcessor

    @Before
    fun before() {
        scanner = mock()
        processor = spy(BarcodeImageProcessor())
        processor.scanner = scanner
        doReturn(scanner).whenever(processor).createScanner()

        reset(processor)
    }

    @Test
    fun nothing__setFormats__setsScanner() {
        // GIVEN
        // nothing

        // WHEN
        processor.formats = intArrayOf(1, 2)

        // THEN
        verify(processor).scanner = any()
    }

    @Test
    fun nothing__setScanner__closesPrevious() {
        // GIVEN
        // nothing

        // WHEN
        processor.scanner = mock()

        // THEN
        verify(scanner).close()
    }

    @Test
    fun nothing__stop__closesScanner() {
        // GIVEN
        // nothing

        // WHEN
        processor.stop()

        // THEN
        verify(scanner).close()
    }

    @Test
    fun image__detectInImage__callsScanner() {
        // STUB
        doReturn(mock<Task<List<MlBarcode>>>()).whenever(scanner).process(any())

        // GIVEN
        val image = mock<InputImage>()

        // WHEN
        processor.detectInImage(image)

        // THEN
        verify(scanner).process(image)
    }

    @Test
    fun nothing__onSuccess__publishValue() {
        // GIVEN
        // nothing

        // WHEN
        val observer = mock<Observer<List<Barcode>>>()
        processor.barcodes.observeForever(observer)
        val result1 = mock<MlBarcode>()
        val result2 = mock<MlBarcode>()
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
        val result1 = mock<MlBarcode>()
        val result2 = mock<MlBarcode>()
        val results = listOf(result1, result2)
        processor.onSuccess(results, mock())

        // THEN
        verify(comparator).compare(any(), any())
        verify(observer).onChanged(listOf(Barcode(result1), Barcode(result2)))
    }
}
