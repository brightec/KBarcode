package uk.co.brightec.kbarcode.model

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class Sms(
    val message: String?,
    val phoneNumber: String?
) {

    internal constructor(fbSms: FirebaseVisionBarcode.Sms) : this(
        message = fbSms.message,
        phoneNumber = fbSms.phoneNumber
    )
}

internal fun FirebaseVisionBarcode.Sms.convert() = Sms(this)
