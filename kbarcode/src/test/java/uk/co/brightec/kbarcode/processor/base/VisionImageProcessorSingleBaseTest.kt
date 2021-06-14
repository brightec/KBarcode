package uk.co.brightec.kbarcode.processor.base

import android.media.Image
import androidx.test.filters.SmallTest
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.brightec.kbarcode.camera.FrameMetadata

@ExperimentalCoroutinesApi
@SmallTest
internal class VisionImageProcessorSingleBaseTest {

    private val testScope = TestCoroutineScope()

    private lateinit var processor: VisionImageProcessorSingleBaseImpl

    @Before
    fun before() {
        processor = spy(VisionImageProcessorSingleBaseImpl())
        processor.scope = testScope
    }

    @After
    fun after() {
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun isProcessing__process__notProcessed() = runBlockingTest {
        // GIVEN
        doReturn(true).whenever(processor).isProcessing()

        // WHEN
        processor.process(mock<Image>(), mock())

        // THEN
        verify(processor, never()).startDetection(any(), any())
    }

    @Test
    fun image__process__callsDetect() = runBlockingTest {
        // STUB
        doReturn(Unit).whenever(processor).startDetection(any(), any())

        // GIVEN
        val image = mock<Image>()
        val frameMetadata = mock<FrameMetadata>()

        // WHEN
        processor.process(image, frameMetadata)

        // THEN
        verify(processor).startDetection(image, frameMetadata)
    }

    @Test
    fun isProcessing__isProcessing__true() {
        // GIVEN
        processor.processingImage = mock()

        // WHEN
        val result = processor.isProcessing()

        // THEN
        assertTrue(result)
    }

    @Test
    fun notProcessing__isProcessing__false() {
        // GIVEN
        processor.processingImage = null

        // WHEN
        val result = processor.isProcessing()

        // THEN
        assertFalse(result)
    }

    @Test
    fun image__startDetection__runsDetection() = runBlockingTest {
        // STUB
        processor.mockSuccess(mock())

        // GIVEN
        val image = mock<Image>()
        val frameMetadata = mock<FrameMetadata>()
        val visionImage = mock<InputImage>()
        doReturn(visionImage).whenever(processor).convertToVisionImage(any(), any())

        // WHEN
        processor.startDetection(image, frameMetadata)

        // THEN
        verify(processor).detectInImage(visionImage)
    }

    @Test
    fun image_completeSuccess_listener__startDetection__callsListener_onSuccess() =
        runBlockingTest {
            // GIVEN
            val image = mock<Image>()
            val frameMetadata = mock<FrameMetadata>()
            val visionImage = mock<InputImage>()
            doReturn(visionImage).whenever(processor).convertToVisionImage(any(), any())
            val foo = mock<Foo>()
            processor.mockSuccess(foo)
            val listener = mock<((Image) -> Unit)>()
            processor.onImageProcessed = listener

            // WHEN
            processor.startDetection(image, frameMetadata)

            // THEN
            verify(processor).onSuccess(foo, frameMetadata)
            verify(listener).invoke(image)
        }

    @Test
    fun image_completeError_listener__startDetection__callsListener_onFailure() = runBlockingTest {
        // GIVEN
        val image = mock<Image>()
        val frameMetadata = mock<FrameMetadata>()
        val visionImage = mock<InputImage>()
        doReturn(visionImage).whenever(processor).convertToVisionImage(any(), any())
        val error = mock<Exception>()
        processor.mockError(error)
        val listener = mock<((Image) -> Unit)>()
        processor.onImageProcessed = listener

        // WHEN
        processor.startDetection(image, frameMetadata)

        // THEN
        verify(processor).onFailure(error)
        verify(listener).invoke(image)
    }

    private class Foo

    private class VisionImageProcessorSingleBaseImpl : VisionImageProcessorSingleBase<Foo>() {

        internal val task = mock<Task<Foo>> {
            on { isCanceled } doReturn false
        }

        override fun detectInImage(image: InputImage): Task<Foo> = task

        override fun onSuccess(results: Foo, frameMetadata: FrameMetadata) {
            // no-op
        }

        override fun onFailure(e: Exception) {
            // no-op
        }

        internal fun mockSuccess(foo: Foo) {
            whenever(task.isComplete).doReturn(true)
            whenever(task.result).doReturn(foo)
            whenever(task.exception).doReturn(null)
        }

        internal fun mockError(error: Exception) {
            whenever(task.isComplete).doReturn(true)
            whenever(task.result).doReturn(null)
            whenever(task.exception).doReturn(error)
        }
    }
}
