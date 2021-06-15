package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class PersonName(
    val first: String?,
    val formattedName: String?,
    val last: String?,
    val middle: String?,
    val prefix: String?,
    val pronunciation: String?,
    val suffix: String?
) {

    internal constructor(mlPersonName: MlBarcode.PersonName) : this(
        first = mlPersonName.first,
        formattedName = mlPersonName.formattedName,
        last = mlPersonName.last,
        middle = mlPersonName.middle,
        prefix = mlPersonName.prefix,
        pronunciation = mlPersonName.pronunciation,
        suffix = mlPersonName.suffix
    )
}

internal fun MlBarcode.PersonName.convert() = PersonName(this)
