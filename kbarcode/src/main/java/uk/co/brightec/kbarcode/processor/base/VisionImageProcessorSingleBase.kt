package uk.co.brightec.kbarcode.processor.base

import android.graphics.Bitmap
import android.media.Image
import androidx.annotation.VisibleForTesting
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import uk.co.brightec.kbarcode.camera.FrameMetadata
import java.nio.ByteBuffer

internal abstract class VisionImageProcessorSingleBase<T> :
    VisionImageProcessorSingle {

    @VisibleForTesting
    internal var processingImage: Pair<Image, FrameMetadata>? = null
    var onImageProcessed: ((Image) -> Unit)? = null

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
            startDetection(image, frameMetadata)
        }
    }

    override fun stop() {}

    fun isProcessing() = processingImage != null

    @VisibleForTesting
    internal fun startDetection(
        image: Image,
        metadata: FrameMetadata
    ) {
        processingImage = image to metadata
        val fbImage = convertToVisionImage(image, metadata)
        detectInImage(fbImage)
            .addOnCompleteListener { task ->
                @Suppress("UnsafeCallOnNullableType")
                onImageProcessed?.invoke(processingImage!!.first)
                processingImage = null
                if (task.isSuccessful && task.result != null) {
                    @Suppress("UnsafeCallOnNullableType")
                    onSuccess(task.result!!, metadata)
                } else {
                    onFailure(task.exception ?: Exception("Unknown"))
                }
            }
    }

    @VisibleForTesting
    internal fun convertToVisionImage(image: Image, frameMetadata: FrameMetadata) =
        FirebaseVisionImage.fromMediaImage(image, frameMetadata.rotation)

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal abstract fun detectInImage(image: FirebaseVisionImage): Task<T>

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal abstract fun onSuccess(
        results: T,
        frameMetadata: FrameMetadata
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal abstract fun onFailure(e: Exception)
}
