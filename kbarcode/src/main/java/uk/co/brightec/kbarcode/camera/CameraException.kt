package uk.co.brightec.kbarcode.camera

/**
 * An exception relating to the camera device.
 */
open class CameraException : IllegalStateException()

/**
 * The camera device is in use already.
 *
 * This error can be produced when opening the camera fails due to the camera
 * being used by a higher-priority camera API client.
 *
 * @see android.hardware.camera2.CameraDevice.StateCallback.ERROR_CAMERA_IN_USE
 */
class CameraInUseException : CameraException()

/**
 * The camera device could not be opened because there are too many other open camera devices.
 *
 * The system-wide limit for number of open cameras has been reached,
 * and more camera devices cannot be opened until previous instances are
 * closed.
 *
 * @see android.hardware.camera2.CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE
 */
class MaxCamerasInUseException : CameraException()

/**
 * The camera device could not be opened due to a device policy.
 *
 * @see android.hardware.camera2.CameraDevice.StateCallback.ERROR_CAMERA_DISABLED
 */
class CameraDisabledException : CameraException()

/**
 * The camera device has encountered a fatal error.
 *
 * The camera device needs to be re-opened to be used again.
 *
 * @see android.hardware.camera2.CameraDevice.StateCallback.ERROR_CAMERA_DEVICE
 */
class CameraDeviceException : CameraException()

/**
 * The camera service has encountered a fatal error.
 *
 * The Android device may need to be shut down and restarted to restore
 * camera function, or there may be a persistent hardware problem.
 *
 * @see android.hardware.camera2.CameraDevice.StateCallback.ERROR_CAMERA_SERVICE
 */
class CameraServiceException : CameraException()

/**
 * This can happen if too many outputs are requested at once
 *
 * @see android.hardware.camera2.CameraCaptureSession.StateCallback.onConfigureFailed
 */
class CameraSessionException : CameraException()

/**
 * If the camera device is no longer connected or has encountered a fatal error
 *
 * @see android.hardware.camera2.CameraDevice.createCaptureSession
 */
class CameraAccessException : CameraException()
