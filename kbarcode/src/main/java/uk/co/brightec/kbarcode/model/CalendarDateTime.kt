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

    internal constructor(fbDateTime: MlBarcode.CalendarDateTime) : this(
        day = fbDateTime.day,
        hours = fbDateTime.hours,
        minutes = fbDateTime.minutes,
        month = fbDateTime.month,
        rawValue = fbDateTime.rawValue,
        seconds = fbDateTime.seconds,
        year = fbDateTime.year,
        isUtc = fbDateTime.isUtc
    )
}

internal fun MlBarcode.CalendarDateTime.convert() = CalendarDateTime(this)
