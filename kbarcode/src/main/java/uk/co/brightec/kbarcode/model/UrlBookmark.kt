package uk.co.brightec.kbarcode.model

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class UrlBookmark(
    val title: String?,
    val url: String?
) {

    internal constructor(fbUrlBookmark: FirebaseVisionBarcode.UrlBookmark) : this(
        title = fbUrlBookmark.title,
        url = fbUrlBookmark.url
    )
}

internal fun FirebaseVisionBarcode.UrlBookmark.convert() = UrlBookmark(this)
