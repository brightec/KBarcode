package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class Email(
    val address: String?,
    val body: String?,
    val subject: String?,
    @EmailType
    val type: Int
) {

    internal constructor(mlEmail: MlBarcode.Email) : this(
        address = mlEmail.address,
        body = mlEmail.body,
        subject = mlEmail.subject,
        type = mlEmail.type
    )

    companion object {

        const val TYPE_HOME =
            MlBarcode.Email.TYPE_HOME
        const val TYPE_UNKNOWN =
            MlBarcode.Email.TYPE_UNKNOWN
        const val TYPE_WORK =
            MlBarcode.Email.TYPE_WORK

        @IntDef(
            TYPE_HOME,
            TYPE_UNKNOWN,
            TYPE_WORK
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class EmailType
    }
}

internal fun MlBarcode.Email.convert() = Email(this)
