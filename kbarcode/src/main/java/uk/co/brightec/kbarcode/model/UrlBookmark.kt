package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

public data class UrlBookmark(
    val title: String?,
    val url: String?
) {

    internal constructor(mlUrlBookmark: MlBarcode.UrlBookmark) : this(
        title = mlUrlBookmark.title,
        url = mlUrlBookmark.url
    )
}

internal fun MlBarcode.UrlBookmark.convert() = UrlBookmark(this)
