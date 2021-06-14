package uk.co.brightec.kbarcode.processor.base

import android.graphics.Bitmap
import android.media.Image
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import uk.co.brightec.kbarcode.camera.FrameMetadata
import java.nio.ByteBuffer

internal abstract class VisionImageProcessorSingleBase<T> :
    VisionImageProcessorSingle {

    @VisibleForTesting
    internal var processingImage: Image? = null
    var onImageProcessed: ((Image) -> Unit)? = null

    private var currentJob: Job? = null

    @VisibleForTesting
    internal var scope = CoroutineScope(Dispatchers.Main)

    override fun process(data: ByteBuffer, frameMetadata: FrameMetadata) {
        throw NotImplementedError("This could be implemented similar to below")
    }

    override fun process(bitmap: Bitmap) {
        throw NotImplementedError("This could be implemented similar to below")
    }

    override fun process(
        image: Image,
        frameMetadata: FrameMetadata
    ) {
        if (!isProcessing()) {
            processingImage = image
            currentJob = scope.launch {
                startDetection(image, frameMetadata)
                processingImage = null
            }
        }
    }

    @CallSuper
    override fun stop() {
        currentJob?.cancel()
        processingImage?.let {
            onImageProcessed?.invoke(it)
            processingImage = null
        }
    }

    fun isProcessing() = processingImage != null

    @VisibleForTesting
    internal suspend fun startDetection(
        image: Image,
        metadata: FrameMetadata
    ) {
        @Suppress("TooGenericExceptionCaught") // As specific as we can be with Firebase
        try {
            val fbImage = convertToVisionImage(image, metadata)
            val result = detectInImage(fbImage).await()
            onSuccess(result, metadata)
        } catch (e: Exception) {
            onFailure(e)
        } finally {
            onImageProcessed?.invoke(image)
        }
    }

    @VisibleForTesting
    @Throws(IllegalStateException::class)
    internal suspend fun convertToVisionImage(image: Image, frameMetadata: FrameMetadata) =
        withContext(Dispatchers.Default) {
            InputImage.fromMediaImage(image, frameMetadata.rotation)
        }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal abstract fun detectInImage(image: InputImage): Task<T>

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal abstract fun onSuccess(
        results: T,
        frameMetadata: FrameMetadata
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal abstract fun onFailure(e: Exception)
}
