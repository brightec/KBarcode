package uk.co.brightec.kbarcode.model

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class GeoPoint(
    val lat: Double,
    val lng: Double
) {

    internal constructor(fbGeoPoint: FirebaseVisionBarcode.GeoPoint) : this(
        lat = fbGeoPoint.lat,
        lng = fbGeoPoint.lng
    )
}

internal fun FirebaseVisionBarcode.GeoPoint.convert() = GeoPoint(this)
