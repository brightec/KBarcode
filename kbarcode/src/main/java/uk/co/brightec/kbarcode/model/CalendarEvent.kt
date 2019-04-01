package uk.co.brightec.kbarcode.model

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

data class CalendarEvent(
    val description: String?,
    val end: CalendarDateTime?,
    val location: String?,
    val organizer: String?,
    val start: CalendarDateTime?,
    val status: String?,
    val summary: String?
) {

    internal constructor(fbEvent: FirebaseVisionBarcode.CalendarEvent) : this(
        description = fbEvent.description,
        end = fbEvent.end?.convert(),
        location = fbEvent.location,
        organizer = fbEvent.organizer,
        start = fbEvent.start?.convert(),
        status = fbEvent.status,
        summary = fbEvent.summary
    )
}

internal fun FirebaseVisionBarcode.CalendarEvent.convert() = CalendarEvent(this)
