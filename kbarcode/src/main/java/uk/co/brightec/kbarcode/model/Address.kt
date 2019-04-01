package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class Address(
    val addressLines: List<String>,
    @AddressType
    val type: Int
) {

    internal constructor(fbAddress: FirebaseVisionBarcode.Address) : this(
        addressLines = fbAddress.addressLines.toList(),
        type = fbAddress.type
    )

    companion object {

        const val TYPE_HOME =
            FirebaseVisionBarcode.Address.TYPE_HOME
        const val TYPE_UNKNOWN =
            FirebaseVisionBarcode.Address.TYPE_UNKNOWN
        const val TYPE_WORK =
            FirebaseVisionBarcode.Address.TYPE_WORK

        @IntDef(
            TYPE_HOME,
            TYPE_UNKNOWN,
            TYPE_WORK
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class AddressType
    }
}
