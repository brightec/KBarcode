package uk.co.brightec.kbarcode.model

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

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

    internal constructor(fbDateTime: FirebaseVisionBarcode.CalendarDateTime) : this(
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

internal fun FirebaseVisionBarcode.CalendarDateTime.convert() = CalendarDateTime(this)
