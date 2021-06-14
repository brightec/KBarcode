package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class UrlBookmark(
    val title: String?,
    val url: String?
) {

    internal constructor(fbUrlBookmark: MlBarcode.UrlBookmark) : this(
        title = fbUrlBookmark.title,
        url = fbUrlBookmark.url
    )
}

internal fun MlBarcode.UrlBookmark.convert() = UrlBookmark(this)
