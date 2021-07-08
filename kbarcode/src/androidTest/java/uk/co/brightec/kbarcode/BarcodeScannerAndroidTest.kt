package uk.co.brightec.kbarcode

import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.params.MeteringRectangle
import android.view.WindowManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.co.brightec.kbarcode.camera.Camera2Source
import uk.co.brightec.kbarcode.processor.BarcodeImageProcessor

@MediumTest
internal class BarcodeScannerAndroidTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var cameraSource: Camera2Source
    private lateinit var windowManager: WindowManager
    private lateinit var frameProcessor: BarcodeImageProcessor

    private lateinit var barcodeScanner: BarcodeScanner

    @Before
    fun before() {
        cameraSource = mock()
        windowManager = mock()
        frameProcessor = mock {
            on { barcodes } doReturn mock()
        }

        barcodeScanner = spy(
            BarcodeScanner(
                cameraSource = cameraSource, windowManager = windowManager,
                frameProcessor = frameProcessor
            )
        )
    }

    @Test
    fun params_cameraBack_rotComp0__calculateFocusRegions__asExpected() {
        // GIVEN
        val viewWidth = 1000
        val viewHeight = 1000
        val touchX = 10F
        val touchY = 20F
        val rect = Rect(0, 0, 100, 100)
        whenever(cameraSource.getCameraSensorInfoActiveArraySize()).doReturn(rect)
        whenever(cameraSource.getCameraFacing()).doReturn(CameraCharacteristics.LENS_FACING_BACK)
        doReturn(0).whenever(barcodeScanner).getRotationCompensation()

        // WHEN
        val result = barcodeScanner.calculateFocusRegions(
            viewWidth = viewWidth, viewHeight = viewHeight, touchX = touchX, touchY = touchY
        )

        // THEN
        assertEquals(1, result!!.size)
        assertEquals(1, result[0].x)
        assertEquals(2, result[0].y)
        assertEquals(5, result[0].width)
        assertEquals(5, result[0].height)
        assertEquals(MeteringRectangle.METERING_WEIGHT_MAX, result[0].meteringWeight)
    }

    @Test
    fun params_cameraBack_rotComp90__calculateFocusRegions__asExpected() {
        // GIVEN
        val viewWidth = 1000
        val viewHeight = 1000
        val touchX = 10F
        val touchY = 20F
        val rect = Rect(0, 0, 100, 100)
        whenever(cameraSource.getCameraSensorInfoActiveArraySize()).doReturn(rect)
        whenever(cameraSource.getCameraFacing()).doReturn(CameraCharacteristics.LENS_FACING_BACK)
        doReturn(90).whenever(barcodeScanner).getRotationCompensation()

        // WHEN
        val result = barcodeScanner.calculateFocusRegions(
            viewWidth = viewWidth, viewHeight = viewHeight, touchX = touchX, touchY = touchY
        )

        // THEN
        assertEquals(1, result!!.size)
        assertEquals(2, result[0].x)
        assertEquals(99, result[0].y)
        assertEquals(5, result[0].width)
        assertEquals(5, result[0].height)
        assertEquals(MeteringRectangle.METERING_WEIGHT_MAX, result[0].meteringWeight)
    }

    @Test
    fun params_cameraBack_rotComp180__calculateFocusRegions__asExpected() {
        // GIVEN
        val viewWidth = 1000
        val viewHeight = 1000
        val touchX = 10F
        val touchY = 20F
        val rect = Rect(0, 0, 100, 100)
        whenever(cameraSource.getCameraSensorInfoActiveArraySize()).doReturn(rect)
        whenever(cameraSource.getCameraFacing()).doReturn(CameraCharacteristics.LENS_FACING_BACK)
        doReturn(180).whenever(barcodeScanner).getRotationCompensation()

        // WHEN
        val result = barcodeScanner.calculateFocusRegions(
            viewWidth = viewWidth, viewHeight = viewHeight, touchX = touchX, touchY = touchY
        )

        // THEN
        assertEquals(1, result!!.size)
        assertEquals(99, result[0].x)
        assertEquals(98, result[0].y)
        assertEquals(5, result[0].width)
        assertEquals(5, result[0].height)
        assertEquals(MeteringRectangle.METERING_WEIGHT_MAX, result[0].meteringWeight)
    }

    @Test
    fun params_cameraBack_rotComp270__calculateFocusRegions__asExpected() {
        // GIVEN
        val viewWidth = 1000
        val viewHeight = 1000
        val touchX = 10F
        val touchY = 20F
        val rect = Rect(0, 0, 100, 100)
        whenever(cameraSource.getCameraSensorInfoActiveArraySize()).doReturn(rect)
        whenever(cameraSource.getCameraFacing()).doReturn(CameraCharacteristics.LENS_FACING_BACK)
        doReturn(270).whenever(barcodeScanner).getRotationCompensation()

        // WHEN
        val result = barcodeScanner.calculateFocusRegions(
            viewWidth = viewWidth, viewHeight = viewHeight, touchX = touchX, touchY = touchY
        )

        // THEN
        assertEquals(1, result!!.size)
        assertEquals(98, result[0].x)
        assertEquals(1, result[0].y)
        assertEquals(5, result[0].width)
        assertEquals(5, result[0].height)
        assertEquals(MeteringRectangle.METERING_WEIGHT_MAX, result[0].meteringWeight)
    }

    @Test
    fun params_cameraFront_rotComp0__calculateFocusRegions__asExpected() {
        // GIVEN
        val viewWidth = 1000
        val viewHeight = 1000
        val touchX = 10F
        val touchY = 20F
        val rect = Rect(0, 0, 100, 100)
        whenever(cameraSource.getCameraSensorInfoActiveArraySize()).doReturn(rect)
        whenever(cameraSource.getCameraFacing()).doReturn(CameraCharacteristics.LENS_FACING_FRONT)
        doReturn(0).whenever(barcodeScanner).getRotationCompensation()

        // WHEN
        val result = barcodeScanner.calculateFocusRegions(
            viewWidth = viewWidth, viewHeight = viewHeight, touchX = touchX, touchY = touchY
        )

        // THEN
        assertEquals(1, result!!.size)
        assertEquals(99, result[0].x)
        assertEquals(2, result[0].y)
        assertEquals(5, result[0].width)
        assertEquals(5, result[0].height)
        assertEquals(MeteringRectangle.METERING_WEIGHT_MAX, result[0].meteringWeight)
    }

    @Test
    fun params_cameraFront_rotComp90__calculateFocusRegions__asExpected() {
        // GIVEN
        val viewWidth = 1000
        val viewHeight = 1000
        val touchX = 10F
        val touchY = 20F
        val rect = Rect(0, 0, 100, 100)
        whenever(cameraSource.getCameraSensorInfoActiveArraySize()).doReturn(rect)
        whenever(cameraSource.getCameraFacing()).doReturn(CameraCharacteristics.LENS_FACING_BACK)
        doReturn(90).whenever(barcodeScanner).getRotationCompensation()

        // WHEN
        val result = barcodeScanner.calculateFocusRegions(
            viewWidth = viewWidth, viewHeight = viewHeight, touchX = touchX, touchY = touchY
        )

        // THEN
        assertEquals(1, result!!.size)
        assertEquals(2, result[0].x)
        assertEquals(99, result[0].y)
        assertEquals(5, result[0].width)
        assertEquals(5, result[0].height)
        assertEquals(MeteringRectangle.METERING_WEIGHT_MAX, result[0].meteringWeight)
    }

    @Test
    fun params_cameraFront_rotComp180__calculateFocusRegions__asExpected() {
        // GIVEN
        val viewWidth = 1000
        val viewHeight = 1000
        val touchX = 10F
        val touchY = 20F
        val rect = Rect(0, 0, 100, 100)
        whenever(cameraSource.getCameraSensorInfoActiveArraySize()).doReturn(rect)
        whenever(cameraSource.getCameraFacing()).doReturn(CameraCharacteristics.LENS_FACING_BACK)
        doReturn(180).whenever(barcodeScanner).getRotationCompensation()

        // WHEN
        val result = barcodeScanner.calculateFocusRegions(
            viewWidth = viewWidth, viewHeight = viewHeight, touchX = touchX, touchY = touchY
        )

        // THEN
        assertEquals(1, result!!.size)
        assertEquals(99, result[0].x)
        assertEquals(98, result[0].y)
        assertEquals(5, result[0].width)
        assertEquals(5, result[0].height)
        assertEquals(MeteringRectangle.METERING_WEIGHT_MAX, result[0].meteringWeight)
    }

    @Test
    fun params_cameraFront_rotComp270__calculateFocusRegions__asExpected() {
        // GIVEN
        val viewWidth = 1000
        val viewHeight = 1000
        val touchX = 10F
        val touchY = 20F
        val rect = Rect(0, 0, 100, 100)
        whenever(cameraSource.getCameraSensorInfoActiveArraySize()).doReturn(rect)
        whenever(cameraSource.getCameraFacing()).doReturn(CameraCharacteristics.LENS_FACING_BACK)
        doReturn(270).whenever(barcodeScanner).getRotationCompensation()

        // WHEN
        val result = barcodeScanner.calculateFocusRegions(
            viewWidth = viewWidth, viewHeight = viewHeight, touchX = touchX, touchY = touchY
        )

        // THEN
        assertEquals(1, result!!.size)
        assertEquals(98, result[0].x)
        assertEquals(1, result[0].y)
        assertEquals(5, result[0].width)
        assertEquals(5, result[0].height)
        assertEquals(MeteringRectangle.METERING_WEIGHT_MAX, result[0].meteringWeight)
    }

    @Test
    fun delayNever__scheduleClearFocusRegions__nothing() {
        // GIVEN
        val delay = BarcodeView.CLEAR_FOCUS_DELAY_NEVER

        // WHEN
        barcodeScanner.scheduleClearFocusRegions(delay)

        // THEN
        verify(cameraSource, never()).clearFocusRegions()
    }

    @Test
    fun delay_cameraNotStarted__scheduleClearFocusRegions__clears() {
        // GIVEN
        val delay = 0L
        whenever(cameraSource.isStarted()).doReturn(false)

        // WHEN
        barcodeScanner.scheduleClearFocusRegions(delay)

        // THEN
        verify(cameraSource, timeout(50).times(0)).clearFocusRegions()
    }

    @Test
    fun delay_cameraStarted__scheduleClearFocusRegions__clears() {
        // GIVEN
        val delay = 0L
        whenever(cameraSource.isStarted()).doReturn(true)

        // WHEN
        barcodeScanner.scheduleClearFocusRegions(delay)

        // THEN
        verify(cameraSource, timeout(50)).clearFocusRegions()
    }
}
