package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

data class CalendarEvent(
    val description: String?,
    val end: CalendarDateTime?,
    val location: String?,
    val organizer: String?,
    val start: CalendarDateTime?,
    val status: String?,
    val summary: String?
) {

    internal constructor(fbEvent: MlBarcode.CalendarEvent) : this(
        description = fbEvent.description,
        end = fbEvent.end?.convert(),
        location = fbEvent.location,
        organizer = fbEvent.organizer,
        start = fbEvent.start?.convert(),
        status = fbEvent.status,
        summary = fbEvent.summary
    )
}

internal fun MlBarcode.CalendarEvent.convert() = CalendarEvent(this)
