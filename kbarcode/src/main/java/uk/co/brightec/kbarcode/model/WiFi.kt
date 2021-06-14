package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class WiFi(
    @EncryptionType
    val encryptionType: Int,
    val password: String?,
    val ssid: String?
) {

    internal constructor(fbWiFi: MlBarcode.WiFi) : this(
        encryptionType = fbWiFi.encryptionType,
        password = fbWiFi.password,
        ssid = fbWiFi.ssid
    )

    companion object {

        const val TYPE_OPEN = MlBarcode.WiFi.TYPE_OPEN
        const val TYPE_WEP = MlBarcode.WiFi.TYPE_WEP
        const val TYPE_WPA = MlBarcode.WiFi.TYPE_WPA

        @IntDef(
            TYPE_OPEN,
            TYPE_WEP,
            TYPE_WPA
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class EncryptionType
    }
}

internal fun MlBarcode.WiFi.convert() = WiFi(this)
