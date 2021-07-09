package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

public data class WiFi(
    @EncryptionType
    val encryptionType: Int,
    val password: String?,
    val ssid: String?
) {

    internal constructor(mlWiFi: MlBarcode.WiFi) : this(
        encryptionType = mlWiFi.encryptionType,
        password = mlWiFi.password,
        ssid = mlWiFi.ssid
    )

    public companion object {

        public const val TYPE_OPEN: Int = MlBarcode.WiFi.TYPE_OPEN
        public const val TYPE_WEP: Int = MlBarcode.WiFi.TYPE_WEP
        public const val TYPE_WPA: Int = MlBarcode.WiFi.TYPE_WPA

        @IntDef(
            TYPE_OPEN,
            TYPE_WEP,
            TYPE_WPA
        )
        @Retention(AnnotationRetention.SOURCE)
        public annotation class EncryptionType
    }
}

internal fun MlBarcode.WiFi.convert() = WiFi(this)
