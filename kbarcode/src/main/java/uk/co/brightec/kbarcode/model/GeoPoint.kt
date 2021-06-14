package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class GeoPoint(
    val lat: Double,
    val lng: Double
) {

    internal constructor(fbGeoPoint: MlBarcode.GeoPoint) : this(
        lat = fbGeoPoint.lat,
        lng = fbGeoPoint.lng
    )
}

internal fun MlBarcode.GeoPoint.convert() = GeoPoint(this)
