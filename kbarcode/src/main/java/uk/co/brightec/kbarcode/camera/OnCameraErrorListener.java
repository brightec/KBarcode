package uk.co.brightec.kbarcode.camera;

import androidx.annotation.NonNull;

// Java file for better inter-op with both languages
public interface OnCameraErrorListener {

    void onCameraError(@NonNull CameraException error);
}
