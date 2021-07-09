package uk.co.brightec.kbarcode.model

import androidx.annotation.IntDef
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

public data class Address(
    val addressLines: List<String>,
    @AddressType
    val type: Int
) {

    internal constructor(mlAddress: MlBarcode.Address) : this(
        addressLines = mlAddress.addressLines.toList(),
        type = mlAddress.type
    )

    public companion object {

        public const val TYPE_HOME: Int = MlBarcode.Address.TYPE_HOME
        public const val TYPE_UNKNOWN: Int = MlBarcode.Address.TYPE_UNKNOWN
        public const val TYPE_WORK: Int = MlBarcode.Address.TYPE_WORK

        @IntDef(
            TYPE_HOME,
            TYPE_UNKNOWN,
            TYPE_WORK
        )
        @Retention(AnnotationRetention.SOURCE)
        public annotation class AddressType
    }
}
