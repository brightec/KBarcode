package uk.co.brightec.kbarcode.model

import com.google.mlkit.vision.barcode.Barcode as MlBarcode

public data class CalendarEvent(
    val description: String?,
    val end: CalendarDateTime?,
    val location: String?,
    val organizer: String?,
    val start: CalendarDateTime?,
    val status: String?,
    val summary: String?
) {

    internal constructor(mlEvent: MlBarcode.CalendarEvent) : this(
        description = mlEvent.description,
        end = mlEvent.end?.convert(),
        location = mlEvent.location,
        organizer = mlEvent.organizer,
        start = mlEvent.start?.convert(),
        status = mlEvent.status,
        summary = mlEvent.summary
    )
}

internal fun MlBarcode.CalendarEvent.convert() = CalendarEvent(this)
