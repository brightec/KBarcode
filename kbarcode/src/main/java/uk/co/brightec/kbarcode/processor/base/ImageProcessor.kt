package uk.co.brightec.kbarcode.processor.base

import android.graphics.Bitmap
import android.media.Image
import uk.co.brightec.kbarcode.camera.FrameMetadata
import java.nio.ByteBuffer

/**
 * An interface to process the images with different ML Kit detectors and custom image models.
 */
internal interface ImageProcessor {

    /**
     * Processes the images with the underlying machine learning models.
     *
     * @return Boolean - TRUE if this frame will be processed
     */
    fun process(data: ByteBuffer, frameMetadata: FrameMetadata)

    /**
     * Processes the bitmap images.
     */
    fun process(bitmap: Bitmap)

    /**
     * Processes the ImageReader images.
     */
    fun process(image: Image, frameMetadata: FrameMetadata)

    /**
     * Stops the underlying machine learning model and release resources.
     */
    fun stop()
}
