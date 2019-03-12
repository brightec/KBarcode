package uk.co.brightec.kbarcode

import android.content.Context
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.util.Size
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import uk.co.brightec.kbarcode.processor.sort.CentralBarcodeComparator

@MediumTest
internal class BarcodeViewTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var barcodeScanner: BarcodeScanner

    private lateinit var barcodeView: BarcodeView

    @Before
    fun before() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        context = target
        barcodeScanner = mock<BarcodeScanner>().apply {
            doNothing().whenever(this).addSurface(any())
        }

        barcodeView = spy(BarcodeView(context = context, barcodeScanner = barcodeScanner))
    }

    @Test
    fun nothing__resume__callsBarcodeScanner() {
        // GIVEN
        // nothing

        // WHEN
        barcodeView.resume()

        // THEN
        verify(barcodeScanner).resume()
    }

    @Test
    fun nothing__pause__callsBarcodeScanner() {
        // GIVEN
        // nothing

        // WHEN
        barcodeView.pause()

        // THEN
        verify(barcodeScanner).pause()
    }

    @Test
    fun nothing__release__callsBarcodeScanner() {
        // GIVEN
        // nothing

        // WHEN
        barcodeView.release()

        // THEN
        verify(barcodeScanner).release()
    }

    @Test
    fun facing_setCameraFacing_callsBarcodeScanner() {
        // GIVEN
        val facing = -1

        // WHEN
        barcodeView.setCameraFacing(facing)

        // THEN
        verify(barcodeScanner).setCameraFacing(facing)
    }

    @Test
    fun formats_setBarcodeFormats_callsBarcodeScanner() {
        // GIVEN
        val formats = arrayOf(-1, -2)

        // WHEN
        barcodeView.setBarcodeFormats(formats)

        // THEN
        verify(barcodeScanner).setBarcodeFormats(formats)
    }

    @Test
    fun minWidth_setMinBarcodeWidth_callsBarcodeScanner() {
        // GIVEN
        val minWidth = 1

        // WHEN
        barcodeView.setMinBarcodeWidth(minWidth)

        // THEN
        verify(barcodeScanner).setMinBarcodeWidth(minWidth)
    }

    @Test
    fun comparator_setBarcodesSort_callsBarcodeScanner() {
        // GIVEN
        val comparator = mock<Comparator<Barcode>>()

        // WHEN
        barcodeView.setBarcodesSort(comparator)

        // THEN
        verify(barcodeScanner).setBarcodesSort(comparator)
    }

    @Test
    fun attr0__cameraFacingAttrConvert__front() {
        // GIVEN
        val attr = 0

        // WHEN
        val result = barcodeView.cameraFacingAttrConvert(attr)

        // THEN
        assertEquals(CameraCharacteristics.LENS_FACING_FRONT, result)
    }

    @Test
    fun attr1__cameraFacingAttrConvert__back() {
        // GIVEN
        val attr = 1

        // WHEN
        val result = barcodeView.cameraFacingAttrConvert(attr)

        // THEN
        assertEquals(CameraCharacteristics.LENS_FACING_BACK, result)
    }

    @Test
    fun attr2__cameraFacingAttrConvert__externalOrBack() {
        // GIVEN
        val attr = 2

        // WHEN
        val result = barcodeView.cameraFacingAttrConvert(attr)

        // THEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assertEquals(CameraCharacteristics.LENS_FACING_EXTERNAL, result)
        } else {
            assertEquals(CameraCharacteristics.LENS_FACING_BACK, result)
        }
    }

    @Test
    fun attr0__formatsAttrConvert__formatsAll() {
        // GIVEN
        val attr = 0

        // WHEN
        val result = barcodeView.formatsAttrConvert(attr)

        // THEN
        assertEquals(1, result.size)
        assertTrue(result.contains(Barcode.FORMAT_ALL_FORMATS))
    }

    @Test
    fun attr1__formatsAttrConvert__formatsNumeric() {
        // GIVEN
        val attr = 1

        // WHEN
        val result = barcodeView.formatsAttrConvert(attr)

        // THEN
        assertEquals(6, result.size)
        assertTrue(result.contains(Barcode.FORMAT_CODABAR))
        assertTrue(result.contains(Barcode.FORMAT_EAN_13))
        assertTrue(result.contains(Barcode.FORMAT_EAN_8))
        assertTrue(result.contains(Barcode.FORMAT_ITF))
        assertTrue(result.contains(Barcode.FORMAT_UPC_A))
        assertTrue(result.contains(Barcode.FORMAT_UPC_E))
    }

    @Test
    fun attr2__formatsAttrConvert__formatsAlphanumeric() {
        // GIVEN
        val attr = 2

        // WHEN
        val result = barcodeView.formatsAttrConvert(attr)

        // THEN
        assertEquals(3, result.size)
        assertTrue(result.contains(Barcode.FORMAT_CODE_128))
        assertTrue(result.contains(Barcode.FORMAT_CODE_39))
        assertTrue(result.contains(Barcode.FORMAT_CODE_93))
    }

    @Test
    fun attr3__formatsAttrConvert__formatsData() {
        // GIVEN
        val attr = 3

        // WHEN
        val result = barcodeView.formatsAttrConvert(attr)

        // THEN
        assertEquals(4, result.size)
        assertTrue(result.contains(Barcode.FORMAT_DATA_MATRIX))
        assertTrue(result.contains(Barcode.FORMAT_QR_CODE))
        assertTrue(result.contains(Barcode.FORMAT_PDF417))
        assertTrue(result.contains(Barcode.FORMAT_AZTEC))
    }

    @Test
    fun attr4__formatsAttrConvert__formatsEan() {
        // GIVEN
        val attr = 4

        // WHEN
        val result = barcodeView.formatsAttrConvert(attr)

        // THEN
        assertEquals(2, result.size)
        assertTrue(result.contains(Barcode.FORMAT_EAN_13))
        assertTrue(result.contains(Barcode.FORMAT_EAN_8))
    }

    @Test
    fun attr0__sortAttrConvert__null() {
        // GIVEN
        val attr = 0

        // WHEN
        val result = barcodeView.sortAttrConvert(attr)

        // THEN
        assertNull(result)
    }

    @Test
    fun attr1__sortAttrConvert__central() {
        // GIVEN
        val attr = 1

        // WHEN
        val result = barcodeView.sortAttrConvert(attr)

        // THEN
        assertTrue(result is CentralBarcodeComparator)
    }

    @Test
    fun centerInside_viewEqualWidthTaller_portrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_INSIDE
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 100, 250)
        doReturn(true).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(0, result.left)
        assertEquals(25, result.top)
        assertEquals(100, result.right)
        assertEquals(225, result.bottom)
    }

    @Test
    fun centerInside_viewEqualWidthShorter_portrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_INSIDE
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 100, 150)
        doReturn(true).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(12, result.left)
        assertEquals(0, result.top)
        assertEquals(88, result.right)
        assertEquals(150, result.bottom)
    }

    @Test
    fun centerInside_viewEqualHeightWider_portrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_INSIDE
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 150, 200)
        doReturn(true).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(25, result.left)
        assertEquals(0, result.top)
        assertEquals(125, result.right)
        assertEquals(200, result.bottom)
    }

    @Test
    fun centerInside_viewEqualHeightNarrower_portrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_INSIDE
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 50, 200)
        doReturn(true).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(0, result.left)
        assertEquals(50, result.top)
        assertEquals(50, result.right)
        assertEquals(150, result.bottom)
    }

    @Test
    fun centerInside_viewEqualWidthTaller_notPortrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_INSIDE
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 250, 100)
        doReturn(false).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(25, result.left)
        assertEquals(0, result.top)
        assertEquals(225, result.right)
        assertEquals(100, result.bottom)
    }

    @Test
    fun centerInside_viewEqualWidthShorter_notPortrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_INSIDE
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 150, 100)
        doReturn(false).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(0, result.left)
        assertEquals(12, result.top)
        assertEquals(150, result.right)
        assertEquals(88, result.bottom)
    }

    @Test
    fun centerInside_viewEqualHeightWider_notPortrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_INSIDE
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 200, 150)
        doReturn(false).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(0, result.left)
        assertEquals(25, result.top)
        assertEquals(200, result.right)
        assertEquals(125, result.bottom)
    }

    @Test
    fun centerInside_viewEqualHeightNarrower_notPortrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_INSIDE
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 200, 50)
        doReturn(false).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(50, result.left)
        assertEquals(0, result.top)
        assertEquals(150, result.right)
        assertEquals(50, result.bottom)
    }

    @Test
    fun centerCrop_viewEqualWidthTaller_portrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_CROP
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 100, 250)
        doReturn(true).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(-12, result.left)
        assertEquals(0, result.top)
        assertEquals(112, result.right)
        assertEquals(250, result.bottom)
    }

    @Test
    fun centerCrop_viewEqualWidthShorter_portrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_CROP
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 100, 150)
        doReturn(true).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(0, result.left)
        assertEquals(-25, result.top)
        assertEquals(100, result.right)
        assertEquals(175, result.bottom)
    }

    @Test
    fun centerCrop_viewEqualHeightWider_portrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_CROP
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 150, 200)
        doReturn(true).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(0, result.left)
        assertEquals(-50, result.top)
        assertEquals(150, result.right)
        assertEquals(250, result.bottom)
    }

    @Test
    fun centerCrop_viewEqualHeightNarrower_portrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_CROP
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 50, 200)
        doReturn(true).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(-25, result.left)
        assertEquals(0, result.top)
        assertEquals(75, result.right)
        assertEquals(200, result.bottom)
    }

    @Test
    fun centerCrop_viewEqualWidthTaller_notPortrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_CROP
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 250, 100)
        doReturn(false).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(0, result.left)
        assertEquals(-12, result.top)
        assertEquals(250, result.right)
        assertEquals(112, result.bottom)
    }

    @Test
    fun centerCrop_viewEqualWidthShorter_notPortrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_CROP
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 150, 100)
        doReturn(false).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(-25, result.left)
        assertEquals(0, result.top)
        assertEquals(175, result.right)
        assertEquals(100, result.bottom)
    }

    @Test
    fun centerCrop_viewEqualHeightWider_notPortrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_CROP
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 200, 150)
        doReturn(false).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(-50, result.left)
        assertEquals(0, result.top)
        assertEquals(250, result.right)
        assertEquals(150, result.bottom)
    }

    @Test
    fun centerCrop_viewEqualHeightNarrower_notPortrait__calculateRectForChild__isCorrect() {
        // GIVEN
        val scaleType = BarcodeView.CENTER_CROP
        whenever(barcodeScanner.getOutputSize()).thenReturn(Size(200, 100))
        val viewRect = Rect(0, 0, 200, 50)
        doReturn(false).whenever(barcodeView).isPortraitMode()

        // WHEN
        val result = barcodeView.calculateRectForChild(
            scaleType = scaleType, left = viewRect.left, top = viewRect.top, right = viewRect.right,
            bottom = viewRect.bottom
        )

        // THEN
        assertEquals(0, result.left)
        assertEquals(-25, result.top)
        assertEquals(200, result.right)
        assertEquals(75, result.bottom)
    }
}
