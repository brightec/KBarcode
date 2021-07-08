package uk.co.brightec.kbarcode

import androidx.lifecycle.LiveData
import androidx.test.filters.SmallTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
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
            on { cameraFlashMode } doReturn -1
            on { barcodeFormats } doReturn intArrayOf(1, 2, 3)
            on { minBarcodeWidth } doReturn -2
            on { barcodesSort } doReturn mock()
            on { scaleType } doReturn -3
            on { clearFocusDelay } doReturn 1234L
        }

        // WHEN
        barcodeScanner.setOptions(options)

        // THEN
        verify(barcodeScanner).setCameraFacing(options.cameraFacing)
        verify(barcodeScanner).setCameraFlashMode(options.cameraFlashMode)
        verify(barcodeScanner).setBarcodeFormats(options.barcodeFormats)
        verify(barcodeScanner).setMinBarcodeWidth(options.minBarcodeWidth)
        verify(barcodeScanner).setBarcodesSort(options.barcodesSort)
        verify(barcodeScanner).setScaleType(options.scaleType)
        verify(barcodeScanner).setClearFocusDelay(options.clearFocusDelay)
    }

    private class BarcodeScanner : KBarcode.Scanner {

        override var onBarcodesListener: OnBarcodesListener? = null
        override var onBarcodeListener: OnBarcodeListener? = null
        override val barcodes: LiveData<List<Barcode>>
            get() = mock()
        override val barcode: LiveData<Barcode>
            get() = mock()
        override var onCameraErrorListener: OnCameraErrorListener? = null

        override fun start() {
            // no-op
        }

        override fun resume() {
            // no-op
        }

        override fun pause() {
            // no-op
        }

        override fun release() {
            // no-op
        }

        override fun setCameraFacing(facing: Int) {
            // no-op
        }

        override fun setCameraFlashMode(flashMode: Int) {
            // no-op
        }

        override fun setBarcodeFormats(formats: IntArray) {
            // no-op
        }

        override fun setMinBarcodeWidth(minBarcodeWidth: Int?) {
            // no-op
        }

        override fun setBarcodesSort(comparator: Comparator<Barcode>?) {
            // no-op
        }

        override fun setScaleType(scaleType: Int) {
            // no-op
        }

        override fun setClearFocusDelay(delay: Long) {
            // no-op
        }
    }
}
