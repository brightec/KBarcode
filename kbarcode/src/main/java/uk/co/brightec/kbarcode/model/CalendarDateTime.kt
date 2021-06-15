package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class CalendarDateTime(
    val day: Int,
    val hours: Int,
    val minutes: Int,
    val month: Int,
    val rawValue: String?,
    val seconds: Int,
    val year: Int,
    val isUtc: Boolean
) {

    internal constructor(mlDateTime: MlBarcode.CalendarDateTime) : this(
        day = mlDateTime.day,
        hours = mlDateTime.hours,
        minutes = mlDateTime.minutes,
        month = mlDateTime.month,
        rawValue = mlDateTime.rawValue,
        seconds = mlDateTime.seconds,
        year = mlDateTime.year,
        isUtc = mlDateTime.isUtc
    )
}

internal fun MlBarcode.CalendarDateTime.convert() = CalendarDateTime(this)
