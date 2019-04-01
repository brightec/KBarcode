package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class Phone(
    val number: String?,
    @PhoneType
    val type: Int
) {

    internal constructor(fbPhone: FirebaseVisionBarcode.Phone) : this(
        number = fbPhone.number,
        type = fbPhone.type
    )

    companion object {

        const val TYPE_FAX =
            FirebaseVisionBarcode.Phone.TYPE_FAX
        const val TYPE_HOME =
            FirebaseVisionBarcode.Phone.TYPE_HOME
        const val TYPE_MOBILE =
            FirebaseVisionBarcode.Phone.TYPE_MOBILE
        const val TYPE_UNKNOWN =
            FirebaseVisionBarcode.Phone.TYPE_UNKNOWN
        const val TYPE_WORK =
            FirebaseVisionBarcode.Phone.TYPE_WORK

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

internal fun FirebaseVisionBarcode.Phone.convert() = Phone(this)
