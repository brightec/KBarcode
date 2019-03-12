package uk.co.brightec.kbarcode.processor.base

import android.media.Image
import androidx.test.filters.SmallTest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.brightec.kbarcode.camera.FrameMetadata

@SmallTest
internal class VisionImageProcessorSingleBaseTest {

    private lateinit var processor: VisionImageProcessorSingleBaseImpl

    @Before
    fun before() {
        processor = spy(VisionImageProcessorSingleBaseImpl())
    }

    @Test
    fun isProcessing__process__notProcessed() {
        // GIVEN
        doReturn(true).whenever(processor).isProcessing()

        // WHEN
        processor.process(mock<Image>(), mock())

        // THEN
        verify(processor, never()).startDetection(any(), any())
    }

    @Test
    fun image__process__callsDetect() {
        doNothing().whenever(processor).startDetection(any(), any())

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
        processor.processingImage = mock<Image>() to mock()

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
    fun image__startDetection__setsVar_runsDetection() {
        // GIVEN
        val image = mock<Image>()
        val frameMetadata = mock<FrameMetadata>()
        val visionImage = mock<FirebaseVisionImage>()
        doReturn(visionImage).whenever(processor).convertToVisionImage(any(), any())

        // WHEN
        processor.startDetection(image, frameMetadata)

        // THEN
        assertEquals(image to frameMetadata, processor.processingImage)
        verify(processor).detectInImage(visionImage)
        verify(processor.task).addOnCompleteListener(any())
    }

    @Test
    fun image_completeSuccess_listener__startDetection__callsListener_setsVar_onSuccess() {
        // GIVEN
        val image = mock<Image>()
        val frameMetadata = mock<FrameMetadata>()
        val visionImage = mock<FirebaseVisionImage>()
        doReturn(visionImage).whenever(processor).convertToVisionImage(any(), any())
        val foo = mock<Foo>()
        val result = mock<Task<Foo>> {
            on { isSuccessful } doReturn true
            on { result } doReturn foo
        }
        val task = processor.task
        doAnswer {
            val listener = it.getArgument<OnCompleteListener<Foo>>(0)
            listener.onComplete(result)
            return@doAnswer task
        }.whenever(task).addOnCompleteListener(any())
        val listener = mock<((Image) -> Unit)>()
        processor.onImageProcessed = listener

        // WHEN
        processor.startDetection(image, frameMetadata)

        // THEN
        verify(listener).invoke(image)
        assertNull(processor.processingImage)
        verify(processor).onSuccess(foo, frameMetadata)
    }

    @Test
    fun image_completeError_listener__startDetection__callsListener_setsVar_onFailure() {
        // GIVEN
        val image = mock<Image>()
        val frameMetadata = mock<FrameMetadata>()
        val visionImage = mock<FirebaseVisionImage>()
        doReturn(visionImage).whenever(processor).convertToVisionImage(any(), any())
        val error = mock<Exception>()
        val result = mock<Task<Foo>> {
            on { isSuccessful } doReturn false
            on { exception } doReturn error
        }
        val task = processor.task
        doAnswer {
            val listener = it.getArgument<OnCompleteListener<Foo>>(0)
            listener.onComplete(result)
            return@doAnswer task
        }.whenever(task).addOnCompleteListener(any())
        val listener = mock<((Image) -> Unit)>()
        processor.onImageProcessed = listener

        // WHEN
        processor.startDetection(image, frameMetadata)

        // THEN
        verify(listener).invoke(image)
        assertNull(processor.processingImage)
        verify(processor).onFailure(error)
    }

    class Foo

    class VisionImageProcessorSingleBaseImpl : VisionImageProcessorSingleBase<Foo>() {

        internal val task = mock<Task<Foo>>()

        override fun detectInImage(image: FirebaseVisionImage): Task<Foo> {
            return task
        }

        override fun onSuccess(results: Foo, frameMetadata: FrameMetadata) {}

        override fun onFailure(e: Exception) {}
    }
}
