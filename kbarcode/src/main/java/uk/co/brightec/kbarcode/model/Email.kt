package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

public data class Email(
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

    public companion object {

        public const val TYPE_HOME: Int = MlBarcode.Email.TYPE_HOME
        public const val TYPE_UNKNOWN: Int = MlBarcode.Email.TYPE_UNKNOWN
        public const val TYPE_WORK: Int = MlBarcode.Email.TYPE_WORK

        @IntDef(
            TYPE_HOME,
            TYPE_UNKNOWN,
            TYPE_WORK
        )
        @Retention(AnnotationRetention.SOURCE)
        public annotation class EmailType
    }
}

internal fun MlBarcode.Email.convert() = Email(this)
