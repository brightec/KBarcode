package uk.co.brightec.kbarcode.camera

internal interface OnCameraReadyListener {

    fun onCameraReady()

    fun onCameraFailure(e: CameraException)
}
