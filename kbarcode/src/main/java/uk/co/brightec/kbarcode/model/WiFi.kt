package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class WiFi(
    @EncryptionType
    val encryptionType: Int,
    val password: String?,
    val ssid: String?
) {

    internal constructor(fbWiFi: FirebaseVisionBarcode.WiFi) : this(
        encryptionType = fbWiFi.encryptionType,
        password = fbWiFi.password,
        ssid = fbWiFi.ssid
    )

    companion object {

        const val TYPE_OPEN = FirebaseVisionBarcode.WiFi.TYPE_OPEN
        const val TYPE_WEP = FirebaseVisionBarcode.WiFi.TYPE_WEP
        const val TYPE_WPA = FirebaseVisionBarcode.WiFi.TYPE_WPA

        @IntDef(
            TYPE_OPEN,
            TYPE_WEP,
            TYPE_WPA
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class EncryptionType
    }
}

internal fun FirebaseVisionBarcode.WiFi.convert() = WiFi(this)
