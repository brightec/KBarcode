package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class GeoPoint(
    val lat: Double,
    val lng: Double
) {

    internal constructor(mlGeoPoint: MlBarcode.GeoPoint) : this(
        lat = mlGeoPoint.lat,
        lng = mlGeoPoint.lng
    )
}

internal fun MlBarcode.GeoPoint.convert() = GeoPoint(this)
