package uk.co.brightec.kbarcode.camera

import uk.co.brightec.kbarcode.util.OpenForTesting

@OpenForTesting
data class FrameMetadata(
    val width: Int,
    val height: Int,
    val rotation: Int,
    val cameraFacing: Int
)
