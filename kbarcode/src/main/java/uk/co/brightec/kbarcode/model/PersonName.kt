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

    internal constructor(fbPersonName: MlBarcode.PersonName) : this(
        first = fbPersonName.first,
        formattedName = fbPersonName.formattedName,
        last = fbPersonName.last,
        middle = fbPersonName.middle,
        prefix = fbPersonName.prefix,
        pronunciation = fbPersonName.pronunciation,
        suffix = fbPersonName.suffix
    )
}

internal fun MlBarcode.PersonName.convert() = PersonName(this)
