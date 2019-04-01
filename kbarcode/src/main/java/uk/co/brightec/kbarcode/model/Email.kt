package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class Email(
    val address: String?,
    val body: String?,
    val subject: String?,
    @EmailType
    val type: Int
) {

    internal constructor(fbEmail: FirebaseVisionBarcode.Email) : this(
        address = fbEmail.address,
        body = fbEmail.body,
        subject = fbEmail.subject,
        type = fbEmail.type
    )

    companion object {

        const val TYPE_HOME =
            FirebaseVisionBarcode.Email.TYPE_HOME
        const val TYPE_UNKNOWN =
            FirebaseVisionBarcode.Email.TYPE_UNKNOWN
        const val TYPE_WORK =
            FirebaseVisionBarcode.Email.TYPE_WORK

        @IntDef(
            TYPE_HOME,
            TYPE_UNKNOWN,
            TYPE_WORK
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class EmailType
    }
}

internal fun FirebaseVisionBarcode.Email.convert() = Email(this)
