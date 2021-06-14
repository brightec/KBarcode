package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class Sms(
    val message: String?,
    val phoneNumber: String?
) {

    internal constructor(fbSms: MlBarcode.Sms) : this(
        message = fbSms.message,
        phoneNumber = fbSms.phoneNumber
    )
}

internal fun MlBarcode.Sms.convert() = Sms(this)
