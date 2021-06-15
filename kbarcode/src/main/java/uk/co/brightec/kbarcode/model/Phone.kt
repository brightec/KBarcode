package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class Phone(
    val number: String?,
    @PhoneType
    val type: Int
) {

    internal constructor(mlPhone: MlBarcode.Phone) : this(
        number = mlPhone.number,
        type = mlPhone.type
    )

    companion object {

        const val TYPE_FAX =
            MlBarcode.Phone.TYPE_FAX
        const val TYPE_HOME =
            MlBarcode.Phone.TYPE_HOME
        const val TYPE_MOBILE =
            MlBarcode.Phone.TYPE_MOBILE
        const val TYPE_UNKNOWN =
            MlBarcode.Phone.TYPE_UNKNOWN
        const val TYPE_WORK =
            MlBarcode.Phone.TYPE_WORK

        @IntDef(
            TYPE_FAX,
            TYPE_HOME,
            TYPE_MOBILE,
            TYPE_UNKNOWN,
            TYPE_WORK
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class PhoneType
    }
}

internal fun MlBarcode.Phone.convert() = Phone(this)
