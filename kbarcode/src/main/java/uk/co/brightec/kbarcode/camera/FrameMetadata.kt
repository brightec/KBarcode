package uk.co.brightec.kbarcode.camera

import android.hardware.camera2.CameraCharacteristics
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import uk.co.brightec.kbarcode.BarcodeScanner
import uk.co.brightec.kbarcode.util.OpenForTesting

/**
 * Holds metadata useful when implementing a `BarcodeComparator`
 *
 * @property width
 *   The width of the image
 * @property height
 *   The height of the image
 * @property rotation
 *   The rotation relative to the device's current orientation
 *   @see BarcodeScanner.getRotationCompensation
 *   @see FirebaseVisionImageMetadata
 * @property cameraFacing
 *   The direction the camera is facing. Optional because not all devices report this value.
 *   @see CameraCharacteristics.LENS_FACING
 */
@OpenForTesting
data class FrameMetadata(
    val width: Int,
    val height: Int,
    val rotation: Int,
    val cameraFacing: Int?
)
