package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class Address(
    val addressLines: List<String>,
    @AddressType
    val type: Int
) {

    internal constructor(mlAddress: MlBarcode.Address) : this(
        addressLines = mlAddress.addressLines.toList(),
        type = mlAddress.type
    )

    companion object {

        const val TYPE_HOME =
            MlBarcode.Address.TYPE_HOME
        const val TYPE_UNKNOWN =
            MlBarcode.Address.TYPE_UNKNOWN
        const val TYPE_WORK =
            MlBarcode.Address.TYPE_WORK

        @IntDef(
            TYPE_HOME,
            TYPE_UNKNOWN,
            TYPE_WORK
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class AddressType
    }
}
