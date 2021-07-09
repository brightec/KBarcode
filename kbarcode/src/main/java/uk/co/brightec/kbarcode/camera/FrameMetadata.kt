@file:Suppress("UnusedImports") // Used in javadoc
package uk.co.brightec.kbarcode.camera

import android.hardware.camera2.CameraCharacteristics
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
 * @property cameraFacing
 *   The direction the camera is facing. Optional because some devices fail to report this value
 *   reliably every time, but usually expected to be present.
 *   @see CameraCharacteristics.LENS_FACING
 */
@OpenForTesting
public data class FrameMetadata(
    val width: Int,
    val height: Int,
    val rotation: Int,
    val cameraFacing: Int?
)
