package uk.co.brightec.kbarcode

import androidx.lifecycle.LiveData
import androidx.test.filters.SmallTest
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import uk.co.brightec.kbarcode.camera.OnCameraErrorListener
import uk.co.brightec.kbarcode.processor.OnBarcodeListener
import uk.co.brightec.kbarcode.processor.OnBarcodesListener

@SmallTest
internal class KBarcodeScannerTest {

    private lateinit var barcodeScanner: KBarcode.Scanner

    @Before
    fun before() {
        barcodeScanner = spy(BarcodeScanner())
    }

    @Test
    fun options__setOptions__callsMethods() {
        // GIVEN
        val options = mock<Options> {
            on { cameraFacing } doReturn -1
            on { barcodeFormats } doReturn arrayOf(1, 2, 3)
            on { minBarcodeWidth } doReturn -2
            on { barcodesSort } doReturn mock()
            on { scaleType } doReturn -3
        }

        // WHEN
        barcodeScanner.setOptions(options)

        // THEN
        verify(barcodeScanner).setCameraFacing(options.cameraFacing)
        verify(barcodeScanner).setBarcodeFormats(options.barcodeFormats)
        verify(barcodeScanner).setMinBarcodeWidth(options.minBarcodeWidth)
        verify(barcodeScanner).setBarcodesSort(options.barcodesSort)
        verify(barcodeScanner).setScaleType(options.scaleType)
    }

    class BarcodeScanner : KBarcode.Scanner {

        override var onBarcodesListener: OnBarcodesListener? = null
        override var onBarcodeListener: OnBarcodeListener? = null
        override val barcodes: LiveData<List<Barcode>>
            get() = mock()
        override val barcode: LiveData<Barcode>
            get() = mock()
        override var onCameraErrorListener: OnCameraErrorListener? = null

        override fun start() {}

        override fun resume() {}

        override fun pause() {}

        override fun release() {}

        override fun setCameraFacing(facing: Int) {}

        override fun setBarcodeFormats(formats: Array<Int>) {}

        override fun setMinBarcodeWidth(minBarcodeWidth: Int?) {}

        override fun setBarcodesSort(comparator: Comparator<Barcode>?) {}

        override fun setScaleType(scaleType: Int) {}
    }
}
