package uk.co.brightec.kbarcode.camera

import android.graphics.Rect
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Handler
import android.util.Size
import android.view.Surface
import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@SmallTest
internal class Camera2SourceTest {

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice
    private lateinit var session: CameraCaptureSession

    private lateinit var cameraSource: Camera2Source

    @Before
    fun before() {
        cameraManager = mock()
        cameraDevice = mock {
            on { id } doReturn "some id"
        }
        session = mock()

        cameraSource = spy(Camera2Source(cameraManager))
    }

    @Test
    fun nothing__isStarted__false() {
        // GIVEN
        // nothing

        // WHEN
        val result = cameraSource.isStarted()

        // THEN
        assertFalse(result)
    }

    @Test
    fun cameraDevice__isStarted__true() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice

        // WHEN
        val result = cameraSource.isStarted()

        // THEN
        assertTrue(result)
    }

    @Test
    fun nothing__isOpening__false() {
        // GIVEN
        // nothing

        // WHEN
        val result = cameraSource.isOpening()

        // THEN
        assertFalse(result)
    }

    @Test
    fun cameraOpening__isOpening__true() {
        // GIVEN
        cameraSource.cameraOpening = true

        // WHEN
        val result = cameraSource.isOpening()

        // THEN
        assertTrue(result)
    }

    @Test
    fun cameraDevice__release__closesCameraDevice() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        cameraSource.currentSession = session
        cameraSource.currentSurfaces = mock()

        // WHEN
        cameraSource.release()

        // THEN
        verify(session).close()
        assertNull(cameraSource.currentSession)
        verify(cameraDevice).close()
        assertNull(cameraSource.cameraDevice)
        assertFalse(cameraSource.cameraOpening)
        assertNull(cameraSource.currentSurfaces)
    }

    @Test
    fun cameraDevice__start__doesNothing() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice

        // WHEN
        cameraSource.start(mock(), mock())

        // THEN
        verify(cameraManager, never()).openCamera(
            any(), any(), anyOrNull<Handler>()
        )
    }

    @Test
    fun cameraOpening__start__doesNothing() {
        // GIVEN
        cameraSource.cameraOpening = true

        // WHEN
        cameraSource.start(mock(), mock())

        // THEN
        verify(cameraManager, never()).openCamera(
            any(), any(), anyOrNull<Handler>()
        )
    }

    @Test
    fun noCameraId__start__error() {
        // GIVEN
        doReturn(null).whenever(cameraSource).selectCamera()

        // WHEN
        val listener = mock<OnCameraReadyListener>()
        cameraSource.start(mock(), listener)

        // THEN
        val captor = argumentCaptor<CameraException>()
        verify(listener).onCameraFailure(captor.capture())
        assertTrue(captor.firstValue is CameraServiceException)
    }

    @Test
    fun cameraId__start__setsOpening_opensCamera() {
        // GIVEN
        val cameraId = "some id"
        doReturn(cameraId).whenever(cameraSource).selectCamera()

        // WHEN
        cameraSource.start(mock(), mock())

        // THEN
        assertTrue(cameraSource.cameraOpening)
        verify(cameraManager).openCamera(
            eq(cameraId), any(), anyOrNull<Handler>()
        )
    }

    @Test
    fun cameraId_cameraOpened_surfacesInvalid__start__setsNotOpening_setCameraDevice_error() {
        doNothing().whenever(cameraSource).createCaptureSession(any(), any())

        // GIVEN
        val cameraId = "some id"
        doReturn(cameraId).whenever(cameraSource).selectCamera()
        whenever(cameraManager.openCamera(any(), any(), anyOrNull<Handler>())).then {
            val stateCallback = it.getArgument<CameraDevice.StateCallback>(1)
            stateCallback.onOpened(cameraDevice)
        }

        // THEN
        val surface = mock<Surface> {
            on { isValid } doReturn false
        }
        val surfaces = listOf(surface)
        val listener = mock<OnCameraReadyListener>()
        cameraSource.start(surfaces, listener)

        // THEN
        assertFalse(cameraSource.cameraOpening)
        verify(cameraSource).cameraDevice = cameraDevice
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun cameraId_cameraOpened__start__setsNotOpening_setCameraDevice_callListener_createSession() {
        doNothing().whenever(cameraSource).createCaptureSession(any(), any())

        // GIVEN
        val cameraId = "some id"
        doReturn(cameraId).whenever(cameraSource).selectCamera()
        whenever(cameraManager.openCamera(any(), any(), anyOrNull<Handler>())).then {
            val stateCallback = it.getArgument<CameraDevice.StateCallback>(1)
            stateCallback.onOpened(cameraDevice)
        }

        // THEN
        val surface = mock<Surface> {
            on { isValid } doReturn true
        }
        val surfaces = listOf(surface)
        val listener = mock<OnCameraReadyListener>()
        cameraSource.start(surfaces, listener)

        // THEN
        assertFalse(cameraSource.cameraOpening)
        verify(cameraSource).cameraDevice = cameraDevice
        verify(listener).onCameraReady()
        verify(cameraSource).createCaptureSession(surfaces, listener)
    }

    @Test
    fun cameraId_cameraDisconnected__start__release() {
        // GIVEN
        val cameraId = "some id"
        doReturn(cameraId).whenever(cameraSource).selectCamera()
        whenever(cameraManager.openCamera(any(), any(), anyOrNull<Handler>())).then {
            val stateCallback = it.getArgument<CameraDevice.StateCallback>(1)
            stateCallback.onDisconnected(cameraDevice)
        }

        // THEN
        cameraSource.start(mock(), mock())

        // THEN
        verify(cameraSource).release()
    }

    @Test
    fun cameraId_cameraError__start__release_callListener() {
        // GIVEN
        val cameraId = "some id"
        doReturn(cameraId).whenever(cameraSource).selectCamera()
        whenever(cameraManager.openCamera(any(), any(), anyOrNull<Handler>())).then {
            val stateCallback = it.getArgument<CameraDevice.StateCallback>(1)
            stateCallback.onError(cameraDevice, -1)
        }

        // THEN
        val listener = mock<OnCameraReadyListener>()
        cameraSource.start(mock(), listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun cameraDevice_sizes__getOutputSize__callsChooseOutputSize() {
        doReturn(Size(-1, -1)).whenever(cameraSource).chooseOutputSize(any(), any())

        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val sizes = arrayOf(Size(-1, -1), Size(-2, -2))
        val sizeMap = mock<StreamConfigurationMap> {
            on { getOutputSizes(any()) } doReturn sizes
        }
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) } doReturn sizeMap
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val minWidth = -1
        cameraSource.getOutputSize(minWidth)

        // THEN
        verify(cameraSource).chooseOutputSize(sizes, minWidth)
    }

    @Test
    fun characteristics__getCameraFacing__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val facing = -1
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.LENS_FACING) } doReturn facing
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraFacing()

        // THEN
        assertEquals(facing, result)
    }

    @Test
    fun characteristics__getCameraSensorOrientation__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val orientation = -1
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.SENSOR_ORIENTATION) } doReturn orientation
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraSensorOrientation()

        // THEN
        assertEquals(orientation, result)
    }

    @Test
    fun characteristics__getCameraSensorInfoActiveArraySize__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val rect = mock<Rect>()
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) } doReturn rect
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraSensorInfoActiveArraySize()

        // THEN
        assertEquals(rect, result)
    }

    @Test
    fun session_noAf__requestFocus__nothing() {
        // GIVEN
        val session = mock<CameraCaptureSession>()
        cameraSource.currentSession = session
        val afModes = intArrayOf()
        doReturn(afModes).whenever(cameraSource).getCameraAvailableAfModes()

        // WHEN
        val regions = arrayOf(mock<MeteringRectangle>())
        cameraSource.requestFocus(regions)

        // THEN
        verifyZeroInteractions(session)
    }

    @Test
    fun session_noAfRegions__requestFocus__nothing() {
        // GIVEN
        val session = mock<CameraCaptureSession>()
        cameraSource.currentSession = session
        val afModes = intArrayOf(CameraMetadata.CONTROL_AF_MODE_AUTO)
        doReturn(afModes).whenever(cameraSource).getCameraAvailableAfModes()
        doReturn(0).whenever(cameraSource).getCameraAfMaxRegions()

        // WHEN
        val regions = arrayOf(mock<MeteringRectangle>())
        cameraSource.requestFocus(regions)

        // THEN
        verifyZeroInteractions(session)
    }

    @Test
    fun session_af__requestFocus__stopRepeating_cancelAf_startAf() {
        // GIVEN
        val session = mock<CameraCaptureSession>()
        cameraSource.currentSession = session
        val cancelRequest = mock<CaptureRequest>()
        val cancelRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn cancelRequest
        }
        val startRequest = mock<CaptureRequest>()
        val startRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn startRequest
        }
        doReturn(cancelRequestBuilder).doReturn(startRequestBuilder).whenever(cameraSource)
            .createDefaultCaptureRequestBuilder(anyOrNull(), anyOrNull())
        val afModes = intArrayOf(CameraMetadata.CONTROL_AF_MODE_AUTO)
        doReturn(afModes).whenever(cameraSource).getCameraAvailableAfModes()
        doReturn(1).whenever(cameraSource).getCameraAfMaxRegions()

        // WHEN
        val regions = arrayOf(mock<MeteringRectangle>())
        cameraSource.requestFocus(regions)

        // THEN
        verify(session).stopRepeating()
        verify(cameraSource).createDefaultCaptureRequestBuilder(null, null)
        verify(session, times(2)).capture(any(), anyOrNull(), anyOrNull())
        verify(cancelRequestBuilder)
            .set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
        verify(cameraSource).createDefaultCaptureRequestBuilder(null, regions)
        // Unable to verify the specifics due to mockito limitations
        // So have to settle for this
        verify(startRequestBuilder, times(2))
            .set(anyOrNull<CaptureRequest.Key<Int>>(), eq(1))
    }

    @Test
    fun session_af_completeAf__requestFocus__restartRepeatingWithRegions() {
        // STUB
        doNothing().whenever(cameraSource).createRepeatingRequest(any(), anyOrNull(), anyOrNull())

        // GIVEN
        val session = mock<CameraCaptureSession>()
        cameraSource.currentSession = session
        val cancelRequest = mock<CaptureRequest>()
        val cancelRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn cancelRequest
        }
        val startRequest = mock<CaptureRequest>()
        val startRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn startRequest
        }
        doReturn(cancelRequestBuilder).doReturn(startRequestBuilder).whenever(cameraSource)
            .createDefaultCaptureRequestBuilder(anyOrNull(), anyOrNull())
        val afModes = intArrayOf(CameraMetadata.CONTROL_AF_MODE_AUTO)
        doReturn(afModes).whenever(cameraSource).getCameraAvailableAfModes()
        doReturn(1).whenever(cameraSource).getCameraAfMaxRegions()
        whenever(session.capture(any(), anyOrNull(), anyOrNull())).thenAnswer {
            val request = it.getArgument<CaptureRequest>(0)
            val callback = it.getArgument<CameraCaptureSession.CaptureCallback?>(1)
            callback?.onCaptureCompleted(session, request, mock())
            1234
        }

        // WHEN
        val regions = arrayOf(mock<MeteringRectangle>())
        cameraSource.requestFocus(regions)

        // THEN
        verify(cameraSource).createRepeatingRequest(
            session = session, listener = null, regions = regions
        )
    }

    @Test
    fun session_af_failedAf__requestFocus__restartRepeatingWithRegions() {
        // STUB
        doNothing().whenever(cameraSource).createRepeatingRequest(any(), anyOrNull(), anyOrNull())

        // GIVEN
        val session = mock<CameraCaptureSession>()
        cameraSource.currentSession = session
        val cancelRequest = mock<CaptureRequest>()
        val cancelRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn cancelRequest
        }
        val startRequest = mock<CaptureRequest>()
        val startRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn startRequest
        }
        doReturn(cancelRequestBuilder).doReturn(startRequestBuilder).whenever(cameraSource)
            .createDefaultCaptureRequestBuilder(anyOrNull(), anyOrNull())
        val afModes = intArrayOf(CameraMetadata.CONTROL_AF_MODE_AUTO)
        doReturn(afModes).whenever(cameraSource).getCameraAvailableAfModes()
        doReturn(1).whenever(cameraSource).getCameraAfMaxRegions()
        whenever(session.capture(any(), anyOrNull(), anyOrNull())).thenAnswer {
            val request = it.getArgument<CaptureRequest>(0)
            val callback = it.getArgument<CameraCaptureSession.CaptureCallback?>(1)
            callback?.onCaptureFailed(session, request, mock())
            1234
        }

        // WHEN
        val regions = arrayOf(mock<MeteringRectangle>())
        cameraSource.requestFocus(regions)

        // THEN
        verify(cameraSource).createRepeatingRequest(
            session = session, listener = null, regions = null
        )
    }

    @Test
    fun session_af__clearFocusRegions__stopRepeating_cancelAf_restartRepeating() {
        // STUB
        doNothing().whenever(cameraSource).createRepeatingRequest(any(), anyOrNull(), anyOrNull())

        // GIVEN
        val session = mock<CameraCaptureSession>()
        cameraSource.currentSession = session
        val cancelRequest = mock<CaptureRequest>()
        val cancelRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn cancelRequest
        }
        doReturn(cancelRequestBuilder).whenever(cameraSource)
            .createDefaultCaptureRequestBuilder(anyOrNull(), anyOrNull())

        // WHEN
        cameraSource.clearFocusRegions()

        // THEN
        verify(session).stopRepeating()
        verify(cameraSource).createDefaultCaptureRequestBuilder(null, null)
        verify(session).capture(any(), anyOrNull(), anyOrNull())
        verify(cancelRequestBuilder)
            .set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
        verify(cameraSource).createRepeatingRequest(
            session = session, listener = null, regions = null
        )
    }

    @Test
    fun listInclFacing__selectCamera__isCorrect() {
        // GIVEN
        val cameraId1 = "1"
        val cameraCharacteristics1 = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.LENS_FACING) } doReturn 1
        }
        whenever(cameraManager.getCameraCharacteristics(cameraId1))
            .thenReturn(cameraCharacteristics1)
        val cameraId2 = "2"
        val cameraCharacteristics2 = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.LENS_FACING) } doReturn 2
        }
        whenever(cameraManager.getCameraCharacteristics(cameraId2))
            .thenReturn(cameraCharacteristics2)
        val ids = arrayOf(cameraId1, cameraId2)
        whenever(cameraManager.cameraIdList).thenReturn(ids)

        // WHEN
        cameraSource.requestedFacing = 2
        val result = cameraSource.selectCamera()

        // THEN
        assertEquals(cameraId2, result)
    }

    @Test
    fun listExclFacing__selectCamera__isCorrect() {
        // GIVEN
        val cameraId1 = "1"
        val cameraCharacteristics1 = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.LENS_FACING) } doReturn 1
        }
        whenever(cameraManager.getCameraCharacteristics(cameraId1))
            .thenReturn(cameraCharacteristics1)
        val cameraId2 = "2"
        val cameraCharacteristics2 = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.LENS_FACING) } doReturn 2
        }
        whenever(cameraManager.getCameraCharacteristics(cameraId2))
            .thenReturn(cameraCharacteristics2)
        val ids = arrayOf(cameraId1, cameraId2)
        whenever(cameraManager.cameraIdList).thenReturn(ids)

        // WHEN
        cameraSource.requestedFacing = 3
        val result = cameraSource.selectCamera()

        // THEN
        assertEquals(cameraId1, result)
    }

    @Test
    fun cameraDevice__createCaptureSession__createsCaptureSession() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        cameraSource.createCaptureSession(surfaces, mock())

        // THEN
        verify(cameraDevice).createCaptureSession(eq(surfaces), any(), anyOrNull())
    }

    @Test
    fun cameraDevice_configured__createCaptureSession__createRepeatingRequest() {
        // STUB
        doNothing().whenever(cameraSource).createRepeatingRequest(any(), anyOrNull(), anyOrNull())

        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        whenever(cameraDevice.createCaptureSession(any(), any(), anyOrNull())).then {
            val stateCallback = it.getArgument<CameraCaptureSession.StateCallback>(1)
            stateCallback.onConfigured(session)
        }

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureSession(surfaces, listener)

        // THEN
        verify(cameraSource).currentSession = session
        verify(cameraSource).currentSurfaces = surfaces
        verify(cameraSource).createRepeatingRequest(
            session = session, listener = listener, regions = null
        )
    }

    @Test
    fun cameraDevice_configurationFailed__createCaptureSession__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val session = mock<CameraCaptureSession>()
        whenever(cameraDevice.createCaptureSession(any(), any(), anyOrNull())).then {
            val stateCallback = it.getArgument<CameraCaptureSession.StateCallback>(1)
            stateCallback.onConfigureFailed(session)
        }

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureSession(surfaces, listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any<CameraSessionException>())
    }

    @Test
    fun cameraDevice_cameraAccessException__createCaptureSession__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        whenever(cameraDevice.createCaptureSession(any(), any(), anyOrNull()))
            .doThrow(mock<android.hardware.camera2.CameraAccessException>())

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureSession(surfaces, listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any<CameraAccessException>())
    }

    @Test
    fun cameraDevice_illegalArgumentException__createCaptureSession__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        whenever(cameraDevice.createCaptureSession(any(), any(), anyOrNull()))
            .doThrow(mock<IllegalArgumentException>())

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureSession(surfaces, listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun cameraDevice_illegalStateException__createCaptureSession__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        whenever(cameraDevice.createCaptureSession(any(), any(), anyOrNull()))
            .doThrow(mock<IllegalStateException>())

        // WHEN
        val surfaces = listOf<Surface>(mock(), mock())
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createCaptureSession(surfaces, listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun session_modes__createRepeatingRequest__setsRepeatingRequest() {
        // GIVEN
        val session = mock<CameraCaptureSession>()
        val captureRequest = mock<CaptureRequest>()
        val captureRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn captureRequest
        }
        doReturn(captureRequestBuilder).whenever(cameraSource)
            .createDefaultCaptureRequestBuilder(anyOrNull(), anyOrNull())
        val afMode = 123
        doReturn(afMode).whenever(cameraSource).selectBestContinuousAfMode()
        val aeMode = 456
        doReturn(aeMode).whenever(cameraSource).selectBestContinuousAeMode()
        val awbMode = 789
        doReturn(awbMode).whenever(cameraSource).selectBestContinuousAwbMode()

        // WHEN
        cameraSource.createRepeatingRequest(
            session = session, listener = mock(), regions = arrayOf(mock())
        )

        // THEN
        verify(captureRequestBuilder).set(CaptureRequest.CONTROL_AF_MODE, afMode)
        verify(captureRequestBuilder).set(CaptureRequest.CONTROL_AE_MODE, aeMode)
        verify(captureRequestBuilder).set(CaptureRequest.CONTROL_AWB_MODE, awbMode)
        verify(session).setRepeatingRequest(captureRequest, null, null)
    }

    @Test
    fun session_modes_requestCameraAccessExc__createRepeatingRequest__release_callsListener() {
        // GIVEN
        val session = mock<CameraCaptureSession> {
            on {
                setRepeatingRequest(any(), anyOrNull(), anyOrNull())
            } doThrow mock<android.hardware.camera2.CameraAccessException>()
        }
        val captureRequest = mock<CaptureRequest>()
        val captureRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn captureRequest
        }
        doReturn(captureRequestBuilder).whenever(cameraSource)
            .createDefaultCaptureRequestBuilder(anyOrNull(), anyOrNull())
        doReturn(123).whenever(cameraSource).selectBestContinuousAfMode()
        doReturn(456).whenever(cameraSource).selectBestContinuousAeMode()
        doReturn(789).whenever(cameraSource).selectBestContinuousAwbMode()

        // WHEN
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createRepeatingRequest(session = session, listener = listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any<CameraAccessException>())
    }

    @Test
    fun session_modes_requestIllegalArgExc__createRepeatingRequest__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val session = mock<CameraCaptureSession> {
            on {
                setRepeatingRequest(any(), anyOrNull(), anyOrNull())
            } doThrow mock<IllegalArgumentException>()
        }
        val captureRequest = mock<CaptureRequest>()
        val captureRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn captureRequest
        }
        doReturn(captureRequestBuilder).whenever(cameraSource)
            .createDefaultCaptureRequestBuilder(anyOrNull(), anyOrNull())
        doReturn(123).whenever(cameraSource).selectBestContinuousAfMode()
        doReturn(456).whenever(cameraSource).selectBestContinuousAeMode()
        doReturn(789).whenever(cameraSource).selectBestContinuousAwbMode()

        // WHEN
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createRepeatingRequest(session = session, listener = listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun session_modes_requestIllegalStateExc__createRepeatingRequest__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val session = mock<CameraCaptureSession> {
            on {
                setRepeatingRequest(any(), anyOrNull(), anyOrNull())
            } doThrow mock<IllegalStateException>()
        }
        val captureRequest = mock<CaptureRequest>()
        val captureRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn captureRequest
        }
        doReturn(captureRequestBuilder).whenever(cameraSource)
            .createDefaultCaptureRequestBuilder(anyOrNull(), anyOrNull())
        doReturn(123).whenever(cameraSource).selectBestContinuousAfMode()
        doReturn(456).whenever(cameraSource).selectBestContinuousAeMode()
        doReturn(789).whenever(cameraSource).selectBestContinuousAwbMode()

        // WHEN
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createRepeatingRequest(session = session, listener = listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }

    @Test
    fun sizesWithAboveAndBelow_minWidth__chooseOutputSize__lowestAboveMin() {
        // GIVEN
        val size1 = mock<Size> {
            on { width } doReturn 1
        }
        val size2 = mock<Size> {
            on { width } doReturn 2
        }
        val size3 = mock<Size> {
            on { width } doReturn 4
        }
        val size4 = mock<Size> {
            on { width } doReturn 5
        }
        val sizes = arrayOf(size1, size2, size3, size4)
        val minWidth = 3

        // WHEN
        val result = cameraSource.chooseOutputSize(sizes, minWidth)

        // THEN
        assertEquals(size3, result)
    }

    @Test
    fun sizesWithAboveOnly_minWidth__chooseOutputSize__lowest() {
        // GIVEN
        val size1 = mock<Size> {
            on { width } doReturn 2
        }
        val size2 = mock<Size> {
            on { width } doReturn 3
        }
        val sizes = arrayOf(size1, size2)
        val minWidth = 1

        // WHEN
        val result = cameraSource.chooseOutputSize(sizes, minWidth)

        // THEN
        assertEquals(size1, result)
    }

    @Test
    fun sizesWithBelowOnly_minWidth__chooseOutputSize__highest() {
        // GIVEN
        val size1 = mock<Size> {
            on { width } doReturn 1
        }
        val size2 = mock<Size> {
            on { width } doReturn 2
        }
        val sizes = arrayOf(size1, size2)
        val minWidth = 3

        // WHEN
        val result = cameraSource.chooseOutputSize(sizes, minWidth)

        // THEN
        assertEquals(size2, result)
    }

    @Test
    fun cameraInUseError__createExceptionFromCameraDeviceError__cameraInUseException() {
        // GIVEN
        val cameraDeviceError = CameraDevice.StateCallback.ERROR_CAMERA_IN_USE

        // WHEN
        val result = cameraSource.createExceptionFromCameraDeviceError(cameraDeviceError)

        // THEN
        assertTrue(result is CameraInUseException)
    }

    @Test
    fun maxCamerasInUseError__createExceptionFromCameraDeviceError__maxCamerasInUseException() {
        // GIVEN
        val cameraDeviceError = CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE

        // WHEN
        val result = cameraSource.createExceptionFromCameraDeviceError(cameraDeviceError)

        // THEN
        assertTrue(result is MaxCamerasInUseException)
    }

    @Test
    fun cameraDisabledError__createExceptionFromCameraDeviceError__cameraDisabledException() {
        // GIVEN
        val cameraDeviceError = CameraDevice.StateCallback.ERROR_CAMERA_DISABLED

        // WHEN
        val result = cameraSource.createExceptionFromCameraDeviceError(cameraDeviceError)

        // THEN
        assertTrue(result is CameraDisabledException)
    }

    @Test
    fun cameraDeviceError__createExceptionFromCameraDeviceError__cameraDeviceException() {
        // GIVEN
        val cameraDeviceError = CameraDevice.StateCallback.ERROR_CAMERA_DEVICE

        // WHEN
        val result = cameraSource.createExceptionFromCameraDeviceError(cameraDeviceError)

        // THEN
        assertTrue(result is CameraDeviceException)
    }

    @Test
    fun cameraServiceError__createExceptionFromCameraDeviceError__cameraServiceException() {
        // GIVEN
        val cameraDeviceError = CameraDevice.StateCallback.ERROR_CAMERA_SERVICE

        // WHEN
        val result = cameraSource.createExceptionFromCameraDeviceError(cameraDeviceError)

        // THEN
        assertTrue(result is CameraServiceException)
    }

    @Test
    fun characteristics__getCameraAvailableAfModes__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val modes = intArrayOf(1, 2, 3)
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES) } doReturn modes
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraAvailableAfModes()

        // THEN
        assertEquals(modes, result)
    }

    @Test
    fun characteristics__getCameraAvailableAeModes__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val modes = intArrayOf(1, 2, 3)
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES) } doReturn modes
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraAvailableAeModes()

        // THEN
        assertEquals(modes, result)
    }

    @Test
    fun characteristics__getCameraAvailableAwbModes__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val modes = intArrayOf(1, 2, 3)
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES) } doReturn modes
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraAvailableAwbModes()

        // THEN
        assertEquals(modes, result)
    }

    @Test
    fun characteristics__getCameraAfMaxRegions__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val maxRegions = 123
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) } doReturn maxRegions
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraAfMaxRegions()

        // THEN
        assertEquals(maxRegions, result)
    }

    @Test
    fun characteristics__getCameraAeMaxRegions__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val maxRegions = 123
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE) } doReturn maxRegions
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraAeMaxRegions()

        // THEN
        assertEquals(maxRegions, result)
    }

    @Test
    fun characteristics__getCameraAwbMaxRegions__isCorrect() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val maxRegions = 123
        val characteristics = mock<CameraCharacteristics> {
            on { get(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB) } doReturn maxRegions
        }
        whenever(cameraManager.getCameraCharacteristics(cameraDevice.id))
            .thenReturn(characteristics)

        // WHEN
        val result = cameraSource.getCameraAwbMaxRegions()

        // THEN
        assertEquals(maxRegions, result)
    }

    @Test
    fun continuousAvailable__selectBestContinuousAfMode__continuous() {
        // GIVEN
        val available = intArrayOf(
            CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE, CameraMetadata.CONTROL_AF_MODE_AUTO
        )
        doReturn(available).whenever(cameraSource).getCameraAvailableAfModes()

        // WHEN
        val result = cameraSource.selectBestContinuousAfMode()

        // THEN
        assertEquals(CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE, result)
    }

    @Test
    fun autoAvailable__selectBestContinuousAfMode__auto() {
        // GIVEN
        val available = intArrayOf(
            CameraMetadata.CONTROL_AF_MODE_AUTO
        )
        doReturn(available).whenever(cameraSource).getCameraAvailableAfModes()

        // WHEN
        val result = cameraSource.selectBestContinuousAfMode()

        // THEN
        assertEquals(CameraMetadata.CONTROL_AF_MODE_AUTO, result)
    }

    @Test
    fun available__selectBestContinuousAfMode__off() {
        // GIVEN
        val available = intArrayOf()
        doReturn(available).whenever(cameraSource).getCameraAvailableAfModes()

        // WHEN
        val result = cameraSource.selectBestContinuousAfMode()

        // THEN
        assertEquals(CameraMetadata.CONTROL_AF_MODE_OFF, result)
    }

    @Test
    fun onAvailable__selectBestContinuousAeMode__on() {
        // GIVEN
        val available = intArrayOf(
            CameraMetadata.CONTROL_AE_MODE_ON, CameraMetadata.CONTROL_AE_MODE_OFF
        )
        doReturn(available).whenever(cameraSource).getCameraAvailableAeModes()

        // WHEN
        val result = cameraSource.selectBestContinuousAeMode()

        // THEN
        assertEquals(CameraMetadata.CONTROL_AE_MODE_ON, result)
    }

    @Test
    fun available__selectBestContinuousAeMode__off() {
        // GIVEN
        val available = intArrayOf()
        doReturn(available).whenever(cameraSource).getCameraAvailableAeModes()

        // WHEN
        val result = cameraSource.selectBestContinuousAeMode()

        // THEN
        assertEquals(CameraMetadata.CONTROL_AE_MODE_OFF, result)
    }

    @Test
    fun onAvailable__selectBestContinuousAwbMode__auto() {
        // GIVEN
        val available = intArrayOf(
            CameraMetadata.CONTROL_AWB_MODE_AUTO, CameraMetadata.CONTROL_AWB_MODE_OFF
        )
        doReturn(available).whenever(cameraSource).getCameraAvailableAwbModes()

        // WHEN
        val result = cameraSource.selectBestContinuousAwbMode()

        // THEN
        assertEquals(CameraMetadata.CONTROL_AWB_MODE_AUTO, result)
    }

    @Test
    fun available__selectBestContinuousAwbMode__off() {
        // GIVEN
        val available = intArrayOf()
        doReturn(available).whenever(cameraSource).getCameraAvailableAwbModes()

        // WHEN
        val result = cameraSource.selectBestContinuousAwbMode()

        // THEN
        assertEquals(CameraMetadata.CONTROL_AWB_MODE_OFF, result)
    }

    @Test
    fun cameraDevice_surfaces_flashMode__createDefaultCaptureRequestBuilder__asExpected() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val surfaces = listOf<Surface>(mock(), mock())
        cameraSource.currentSurfaces = surfaces
        val captureRequest = mock<CaptureRequest>()
        val captureRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn captureRequest
        }
        whenever(cameraDevice.createCaptureRequest(any())).thenReturn(captureRequestBuilder)
        val flashMode = -1
        cameraSource.requestedFlashMode = flashMode

        // WHEN
        val result = cameraSource.createDefaultCaptureRequestBuilder(
            listener = mock(), regions = null
        )

        // THEN
        assertNotNull(result)
        verify(captureRequestBuilder).set(CaptureRequest.FLASH_MODE, flashMode)
        verify(captureRequestBuilder).addTarget(surfaces[0])
        verify(captureRequestBuilder).addTarget(surfaces[1])
    }

    @Test
    fun cameraDevice_surfaces_flashMode_regions__createDefaultCaptureRequestBuilder__asExpected() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        val surfaces = listOf<Surface>(mock(), mock())
        cameraSource.currentSurfaces = surfaces
        val captureRequest = mock<CaptureRequest>()
        val captureRequestBuilder = mock<CaptureRequest.Builder> {
            on { build() } doReturn captureRequest
        }
        whenever(cameraDevice.createCaptureRequest(any())).thenReturn(captureRequestBuilder)
        val flashMode = -1
        cameraSource.requestedFlashMode = flashMode
        val regions = arrayOf(mock<MeteringRectangle>())
        doReturn(1).whenever(cameraSource).getCameraAfMaxRegions()
        doReturn(1).whenever(cameraSource).getCameraAeMaxRegions()
        doReturn(1).whenever(cameraSource).getCameraAwbMaxRegions()

        // WHEN
        val result = cameraSource.createDefaultCaptureRequestBuilder(
            listener = mock(), regions = regions
        )

        // THEN
        assertNotNull(result)
        verify(captureRequestBuilder).set(CaptureRequest.FLASH_MODE, flashMode)
        verify(captureRequestBuilder, times(3))
            .set(anyOrNull<CaptureRequest.Key<Array<MeteringRectangle>>>(), eq(regions))
        verify(captureRequestBuilder).addTarget(surfaces[0])
        verify(captureRequestBuilder).addTarget(surfaces[1])
    }

    @Test
    fun cameraDevice_captureIllegalArgExc__createDefaultCaptureRequestBuilder__release_callsListener() {
        // GIVEN
        cameraSource.cameraDevice = cameraDevice
        whenever(cameraDevice.createCaptureRequest(any()))
            .doThrow(mock<IllegalArgumentException>())

        // WHEN
        val listener = mock<OnCameraReadyListener>()
        cameraSource.createDefaultCaptureRequestBuilder(listener)

        // THEN
        verify(cameraSource).release()
        verify(listener).onCameraFailure(any())
    }
}
