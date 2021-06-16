package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

public data class Phone(
    val number: String?,
    @PhoneType
    val type: Int
) {

    internal constructor(mlPhone: MlBarcode.Phone) : this(
        number = mlPhone.number,
        type = mlPhone.type
    )

    public companion object {

        public const val TYPE_FAX: Int = MlBarcode.Phone.TYPE_FAX
        public const val TYPE_HOME: Int = MlBarcode.Phone.TYPE_HOME
        public const val TYPE_MOBILE: Int = MlBarcode.Phone.TYPE_MOBILE
        public const val TYPE_UNKNOWN: Int = MlBarcode.Phone.TYPE_UNKNOWN
        public const val TYPE_WORK: Int = MlBarcode.Phone.TYPE_WORK

        @IntDef(
            TYPE_FAX,
            TYPE_HOME,
            TYPE_MOBILE,
            TYPE_UNKNOWN,
            TYPE_WORK
        )
        @Retention(AnnotationRetention.SOURCE)
        public annotation class PhoneType
    }
}

internal fun MlBarcode.Phone.convert() = Phone(this)
