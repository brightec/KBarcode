package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

public data class Sms(
    val message: String?,
    val phoneNumber: String?
) {

    internal constructor(mlSms: MlBarcode.Sms) : this(
        message = mlSms.message,
        phoneNumber = mlSms.phoneNumber
    )
}

internal fun MlBarcode.Sms.convert() = Sms(this)
