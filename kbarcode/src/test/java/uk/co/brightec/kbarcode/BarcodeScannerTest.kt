package uk.co.brightec.kbarcode

import android.media.Image
import android.media.ImageReader
import android.util.Size
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import uk.co.brightec.kbarcode.BarcodeScanner.Companion.BARCODE_SCREEN_PROPORTION
import uk.co.brightec.kbarcode.camera.Camera2Source
import uk.co.brightec.kbarcode.camera.CameraException
import uk.co.brightec.kbarcode.camera.OnCameraErrorListener
import uk.co.brightec.kbarcode.camera.OnCameraReadyListener
import uk.co.brightec.kbarcode.extension.getMinWidth
import uk.co.brightec.kbarcode.processor.BarcodeProcessorSingle

@SmallTest
internal class BarcodeScannerTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var cameraSource: Camera2Source
    private lateinit var windowManager: WindowManager
    private lateinit var frameProcessor: BarcodeProcessorSingle

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
    fun nothing__init__imageProcessedListenerSet() {
        // GIVEN
        // nothing

        // WHEN
        // init

        // THEN
        verify(frameProcessor).onImageProcessed = any()
    }

    @Test
    fun imageProcessedListenerSet__onImageProcessed__close() {
        // GIVEN
        val captor = argumentCaptor<((Image) -> Unit)>()
        verify(frameProcessor).onImageProcessed = captor.capture()

        // WHEN
        val image = mock<Image>()
        captor.firstValue.invoke(image)

        // THEN
        verify(image).close()
    }

    @Test
    fun cameraNotStarted__addSurface__addsSurface() {
        // GIVEN
        whenever(cameraSource.isStarted()).thenReturn(false)

        // WHEN
        val surface = mock<Surface>()
        barcodeScanner.addSurface(surface)

        // THEN
        verify(barcodeScanner, never()).release()
        assertTrue(barcodeScanner.customSurfaces.contains(surface))
        verify(barcodeScanner, never()).start()
    }

    @Test
    fun cameraStarted__addSurface__releases_addsSurface_starts() {
        doNothing().whenever(barcodeScanner).release()
        doNothing().whenever(barcodeScanner).start()

        // GIVEN
        whenever(cameraSource.isStarted()).thenReturn(true)

        // WHEN
        val surface = mock<Surface>()
        barcodeScanner.addSurface(surface)

        // THEN
        verify(barcodeScanner).release()
        assertTrue(barcodeScanner.customSurfaces.contains(surface))
        verify(barcodeScanner).start()
    }

    @Test
    fun nothing__start__resumeProcessing_addsObserver_startCameraSource() {
        doNothing().whenever(barcodeScanner).startCameraSource()

        // GIVEN
        // nothing

        // WHEN
        barcodeScanner.start()

        // THEN
        assertFalse(barcodeScanner.pauseProcessing)
        verify(frameProcessor.barcodes).observeForever(any())
        verify(barcodeScanner).startCameraSource()
    }

    @Test
    fun processingPaused__start__resumeProcessing_addsObserver_startCameraSource() {
        doNothing().whenever(barcodeScanner).startCameraSource()

        // GIVEN
        barcodeScanner.pauseProcessing = true

        // WHEN
        barcodeScanner.start()

        // THEN
        assertFalse(barcodeScanner.pauseProcessing)
        verify(frameProcessor.barcodes).observeForever(any())
        verify(barcodeScanner).startCameraSource()
    }

    @Test
    fun cameraIsStarted__start__resumeProcessing_doesntAddObserver_doesntStartCameraSource() {
        doNothing().whenever(barcodeScanner).startCameraSource()

        // GIVEN
        whenever(cameraSource.isStarted()).thenReturn(true)

        // WHEN
        barcodeScanner.start()

        // THEN
        assertFalse(barcodeScanner.pauseProcessing)
        verify(frameProcessor.barcodes, never()).observeForever(any())
        verify(barcodeScanner, never()).startCameraSource()
    }

    @Test
    fun cameraIsOpening__start__resumeProcessing_doesntAddObserver_doesntStartCameraSource() {
        doNothing().whenever(barcodeScanner).startCameraSource()

        // GIVEN
        whenever(cameraSource.isOpening()).thenReturn(true)

        // WHEN
        barcodeScanner.start()

        // THEN
        assertFalse(barcodeScanner.pauseProcessing)
        verify(frameProcessor.barcodes, never()).observeForever(any())
        verify(barcodeScanner, never()).startCameraSource()
    }

    @Test
    fun processingResumed__resume__resumeProcessing() {
        // GIVEN
        barcodeScanner.pauseProcessing = false

        // WHEN
        barcodeScanner.resume()

        // THEN
        assertFalse(barcodeScanner.pauseProcessing)
    }

    @Test
    fun processingPaused__resume__resumeProcessing() {
        // GIVEN
        barcodeScanner.pauseProcessing = true

        // WHEN
        barcodeScanner.resume()

        // THEN
        assertFalse(barcodeScanner.pauseProcessing)
    }

    @Test
    fun processingResumed__pause__pauseProcessing() {
        // GIVEN
        barcodeScanner.pauseProcessing = false

        // WHEN
        barcodeScanner.pause()

        // THEN
        assertTrue(barcodeScanner.pauseProcessing)
    }

    @Test
    fun processingPaused__pause__pauseProcessing() {
        // GIVEN
        barcodeScanner.pauseProcessing = true

        // WHEN
        barcodeScanner.pause()

        // THEN
        assertTrue(barcodeScanner.pauseProcessing)
    }

    @Test
    fun imageReader__release__releasesResources() {
        // GIVEN
        barcodeScanner.imageReader = mock()

        // WHEN
        barcodeScanner.release()

        // THEN
        verify(frameProcessor.barcodes).removeObserver(any())
        verify(frameProcessor).stop()
        verify(cameraSource).release()
        verify(barcodeScanner.imageReader)!!.close()
    }

    @Test
    fun cameraNotStarted__setCameraFacing__setsOnCamera() {
        // GIVEN
        whenever(cameraSource.isStarted()).thenReturn(false)

        // WHEN
        val facing = -1
        barcodeScanner.setCameraFacing(facing)

        // THEN
        verify(barcodeScanner, never()).release()
        verify(cameraSource).requestedFacing = facing
        verify(barcodeScanner, never()).start()
    }

    @Test
    fun cameraStarted__setCameraFacing__releases_setsOnCamera_starts() {
        doNothing().whenever(barcodeScanner).release()
        doNothing().whenever(barcodeScanner).start()

        // GIVEN
        whenever(cameraSource.isStarted()).thenReturn(true)

        // WHEN
        val facing = -1
        barcodeScanner.setCameraFacing(facing)

        // THEN
        verify(barcodeScanner).release()
        verify(cameraSource).requestedFacing = facing
        verify(barcodeScanner).start()
    }

    @Test
    fun nothing__setBarcodeFormats__setsOnProcessor() {
        // GIVEN
        // nothing

        // WHEN
        val formats = arrayOf(-1, -2)
        barcodeScanner.setBarcodeFormats(formats)

        // THEN
        verify(frameProcessor).formats = formats
    }

    @Test
    fun minWidthPositive__setMinBarcodeWidth__setsVar() {
        // GIVEN
        val minWidth = 1

        // WHEN
        barcodeScanner.setMinBarcodeWidth(minWidth)

        // THEN
        verify(barcodeScanner).customMinBarcodeWidth = minWidth
    }

    @Test
    fun minWidthNegative__setMinBarcodeWidth__setsVar() {
        // GIVEN
        val minWidth = -1

        // WHEN
        barcodeScanner.setMinBarcodeWidth(minWidth)

        // THEN
        verify(barcodeScanner).customMinBarcodeWidth = null
    }

    @Test
    fun nothing__setBarcodesSort__setsOnProcessor() {
        // GIVEN
        // nothing

        // WHEN
        val comparator = mock<Comparator<Barcode>>()
        barcodeScanner.setBarcodesSort(comparator)

        // THEN
        verify(frameProcessor).barcodesSort = comparator
    }

    @Test
    fun minWidthForBarcodes__getOutputSize__callsCameraWithMinWidth() {
        // GIVEN
        doReturn(-1).whenever(barcodeScanner).minWidthForBarcodes()
        val size = mock<Size>()
        whenever(cameraSource.getOutputSize(any())).thenReturn(size)

        // WHEN
        val result = barcodeScanner.getOutputSize()

        // THEN
        verify(barcodeScanner).minWidthForBarcodes()
        assertEquals(size, result)
    }

    @Test
    fun processorSurface_customSurfaces__startCameraSource__startsCameraWithSurfaces() {
        // GIVEN
        val processorSurface = mock<Surface>()
        doReturn(processorSurface).whenever(barcodeScanner).createProcessorSurface()
        val surface1 = mock<Surface>()
        barcodeScanner.customSurfaces.add(surface1)
        val surface2 = mock<Surface>()
        barcodeScanner.customSurfaces.add(surface2)

        // WHEN
        barcodeScanner.startCameraSource()

        // THEN
        val captor = argumentCaptor<List<Surface>>()
        verify(cameraSource).start(captor.capture(), any())
        assertTrue(captor.firstValue.contains(processorSurface))
        assertTrue(captor.firstValue.contains(surface1))
        assertTrue(captor.firstValue.contains(surface2))
    }

    @Test
    fun processorSurface_customSurfaces_cameraFailure__startCameraSource__callsErrorListener() {
        // GIVEN
        val processorSurface = mock<Surface>()
        doReturn(processorSurface).whenever(barcodeScanner).createProcessorSurface()
        val surface1 = mock<Surface>()
        barcodeScanner.customSurfaces.add(surface1)
        val surface2 = mock<Surface>()
        barcodeScanner.customSurfaces.add(surface2)
        val errorListener = mock<OnCameraErrorListener>()
        barcodeScanner.onCameraErrorListener = errorListener
        val error = CameraException()
        whenever(cameraSource.start(any(), any())).then {
            val listener = it.getArgument<OnCameraReadyListener>(1)
            listener.onCameraFailure(error)
        }

        // WHEN
        barcodeScanner.startCameraSource()

        // THEN
        verify(errorListener).onCameraError(error)
    }

    @Test
    fun size__createProcessorSurface__setsReader() {
        val imageReader = mock<ImageReader> {
            on { surface } doReturn mock()
        }
        doReturn(imageReader).whenever(barcodeScanner).createProcessorImageReader(any())
        doReturn(-1).whenever(barcodeScanner).minWidthForBarcodes()

        // GIVEN
        whenever(cameraSource.getOutputSize(any())).thenReturn(mock())

        // WHEN
        barcodeScanner.createProcessorSurface()

        // THEN
        verify(barcodeScanner).imageReader = imageReader
    }

    @Test
    fun createProcessorSurface_processing__imageAvailable__imageGetsClosed_notProcessed() {
        val image = mock<Image>()
        val imageReader = mock<ImageReader> {
            on { surface } doReturn mock()
            on { acquireLatestImage() } doReturn image
        }
        doReturn(imageReader).whenever(barcodeScanner).createProcessorImageReader(any())
        doReturn(-1).whenever(barcodeScanner).minWidthForBarcodes()

        // GIVEN
        whenever(cameraSource.getOutputSize(any())).thenReturn(mock())
        barcodeScanner.createProcessorSurface()
        whenever(frameProcessor.isProcessing()).thenReturn(true)

        // WHEN
        val captor = argumentCaptor<ImageReader.OnImageAvailableListener>()
        verify(imageReader).setOnImageAvailableListener(captor.capture(), anyOrNull())
        captor.firstValue.onImageAvailable(imageReader)

        // THEN
        verify(image).close()
        verify(frameProcessor, never()).process(any<Image>(), any())
    }

    @Test
    fun createProcessorSurface_paused__imageAvailable__imageGetsClosed_notProcessed() {
        val image = mock<Image>()
        val imageReader = mock<ImageReader> {
            on { surface } doReturn mock()
            on { acquireLatestImage() } doReturn image
        }
        doReturn(imageReader).whenever(barcodeScanner).createProcessorImageReader(any())
        doReturn(-1).whenever(barcodeScanner).minWidthForBarcodes()

        // GIVEN
        whenever(cameraSource.getOutputSize(any())).thenReturn(mock())
        barcodeScanner.createProcessorSurface()
        barcodeScanner.pauseProcessing = true

        // WHEN
        val captor = argumentCaptor<ImageReader.OnImageAvailableListener>()
        verify(imageReader).setOnImageAvailableListener(captor.capture(), anyOrNull())
        captor.firstValue.onImageAvailable(imageReader)

        // THEN
        verify(image).close()
        verify(frameProcessor, never()).process(any<Image>(), any())
    }

    @Test
    fun createProcessorSurface__imageAvailable__processes() {
        val image = mock<Image>()
        val imageReader = mock<ImageReader> {
            on { surface } doReturn mock()
            on { acquireLatestImage() } doReturn image
        }
        doReturn(imageReader).whenever(barcodeScanner).createProcessorImageReader(any())
        doReturn(-1).whenever(barcodeScanner).minWidthForBarcodes()
        doReturn(-1).whenever(barcodeScanner).getRotationCompensation()

        // GIVEN
        whenever(cameraSource.getOutputSize(any())).thenReturn(mock())
        barcodeScanner.createProcessorSurface()

        // WHEN
        val captor = argumentCaptor<ImageReader.OnImageAvailableListener>()
        verify(imageReader).setOnImageAvailableListener(captor.capture(), anyOrNull())
        captor.firstValue.onImageAvailable(imageReader)

        // THEN
        verify(frameProcessor).process(eq(image), any())
    }

    @Test
    fun device0_sensor0__getRotationCompensation__0() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_0
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(0)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_0, result)
    }

    @Test
    fun device0_sensor90__getRotationCompensation__90() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_0
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(90)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_90, result)
    }

    @Test
    fun device0_sensor180__getRotationCompensation__180() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_0
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(180)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_180, result)
    }

    @Test
    fun device0_sensor270__getRotationCompensation__270() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_0
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(270)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_270, result)
    }

    @Test
    fun device90_sensor0__getRotationCompensation__270() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_90
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(0)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_270, result)
    }

    @Test
    fun device90_sensor90__getRotationCompensation__0() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_90
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(90)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_0, result)
    }

    @Test
    fun device90_sensor180__getRotationCompensation__90() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_90
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(180)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_90, result)
    }

    @Test
    fun device90_sensor270__getRotationCompensation__180() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_90
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(270)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_180, result)
    }

    @Test
    fun device180_sensor0__getRotationCompensation__180() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_180
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(0)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_180, result)
    }

    @Test
    fun device180_sensor90__getRotationCompensation__270() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_180
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(90)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_270, result)
    }

    @Test
    fun device180_sensor180__getRotationCompensation__0() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_180
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(180)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_0, result)
    }

    @Test
    fun device180_sensor270__getRotationCompensation__90() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_180
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(270)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_90, result)
    }

    @Test
    fun device270_sensor0__getRotationCompensation__90() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_270
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(0)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_90, result)
    }

    @Test
    fun device270_sensor90__getRotationCompensation__180() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_270
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(90)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_180, result)
    }

    @Test
    fun device270_sensor180__getRotationCompensation__270() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_270
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(180)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_270, result)
    }

    @Test
    fun device270_sensor270__getRotationCompensation__0() {
        // GIVEN
        val display = mock<Display> {
            on { rotation } doReturn Surface.ROTATION_270
        }
        whenever(windowManager.defaultDisplay).thenReturn(display)
        whenever(cameraSource.getCameraSensorOrientation()).thenReturn(270)

        // WHEN
        val result = barcodeScanner.getRotationCompensation()

        // THEN
        assertEquals(FirebaseVisionImageMetadata.ROTATION_0, result)
    }

    @Test
    fun oneFormat__minWidthForBarcodes__isCorrect() {
        // GIVEN
        val formats = arrayOf(Barcode.FORMAT_PDF417)
        whenever(frameProcessor.formats).thenReturn(formats)

        // WHEN
        val result = barcodeScanner.minWidthForBarcodes()

        // THEN
        val expected = Barcode.FORMAT_PDF417.getMinWidth() / BARCODE_SCREEN_PROPORTION
        assertEquals(expected.toInt(), result)
    }

    @Test
    fun formats__minWidthForBarcodes__usesMaxFormat() {
        // GIVEN
        val formats = arrayOf(Barcode.FORMAT_PDF417, Barcode.FORMAT_EAN_8)
        whenever(frameProcessor.formats).thenReturn(formats)

        // WHEN
        val result = barcodeScanner.minWidthForBarcodes()

        // THEN
        val expected = Barcode.FORMAT_PDF417.getMinWidth() / BARCODE_SCREEN_PROPORTION
        assertEquals(expected.toInt(), result)
    }
}
