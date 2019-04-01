package uk.co.brightec.kbarcode

import android.graphics.Point
import android.graphics.Rect
import androidx.annotation.IntDef
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import uk.co.brightec.kbarcode.model.CalendarEvent
import uk.co.brightec.kbarcode.model.ContactInfo
import uk.co.brightec.kbarcode.model.DrivingLicense
import uk.co.brightec.kbarcode.model.Email
import uk.co.brightec.kbarcode.model.GeoPoint
import uk.co.brightec.kbarcode.model.Phone
import uk.co.brightec.kbarcode.model.Sms
import uk.co.brightec.kbarcode.model.UrlBookmark
import uk.co.brightec.kbarcode.model.WiFi
import uk.co.brightec.kbarcode.model.convert
import uk.co.brightec.kbarcode.util.OpenForTesting

/**
 * Main data class to represent a barcode
 *
 * This class wraps the underlying FirebaseVisionBarcode
 * https://firebase.google.com/docs/reference/android/com/google/firebase/ml/vision/barcode/FirebaseVisionBarcode
 */
@OpenForTesting
data class Barcode(
    val boundingBox: Rect?,
    val calendarEvent: CalendarEvent?,
    val contactInfo: ContactInfo?,
    val cornerPoints: List<Point>?,
    val displayValue: String?,
    val driverLicense: DrivingLicense?,
    val email: Email?,
    @BarcodeFormat
    val format: Int,
    val geoPoint: GeoPoint?,
    val phone: Phone?,
    val rawValue: String?,
    val sms: Sms?,
    val url: UrlBookmark?,
    @BarcodeValueType
    val valueType: Int,
    val wifi: WiFi?
) {

    internal constructor(fbBarcode: FirebaseVisionBarcode) : this(
        boundingBox = fbBarcode.boundingBox,
        calendarEvent = fbBarcode.calendarEvent?.convert(),
        contactInfo = fbBarcode.contactInfo?.convert(),
        cornerPoints = fbBarcode.cornerPoints?.toList(),
        displayValue = fbBarcode.displayValue,
        driverLicense = fbBarcode.driverLicense?.convert(),
        email = fbBarcode.email?.convert(),
        format = fbBarcode.format,
        geoPoint = fbBarcode.geoPoint?.convert(),
        phone = fbBarcode.phone?.convert(),
        rawValue = fbBarcode.rawValue,
        sms = fbBarcode.sms?.convert(),
        url = fbBarcode.url?.convert(),
        valueType = fbBarcode.valueType,
        wifi = fbBarcode.wifi?.convert()
    )

    override fun toString() = displayValue ?: super.toString()

    companion object {

        const val FORMAT_UNKNOWN = FirebaseVisionBarcode.FORMAT_UNKNOWN
        const val FORMAT_ALL_FORMATS = FirebaseVisionBarcode.FORMAT_ALL_FORMATS
        const val FORMAT_CODE_128 = FirebaseVisionBarcode.FORMAT_CODE_128
        const val FORMAT_CODE_39 = FirebaseVisionBarcode.FORMAT_CODE_39
        const val FORMAT_CODE_93 = FirebaseVisionBarcode.FORMAT_CODE_93
        const val FORMAT_CODABAR = FirebaseVisionBarcode.FORMAT_CODABAR
        const val FORMAT_DATA_MATRIX = FirebaseVisionBarcode.FORMAT_DATA_MATRIX
        const val FORMAT_EAN_13 = FirebaseVisionBarcode.FORMAT_EAN_13
        const val FORMAT_EAN_8 = FirebaseVisionBarcode.FORMAT_EAN_8
        const val FORMAT_ITF = FirebaseVisionBarcode.FORMAT_ITF
        const val FORMAT_QR_CODE = FirebaseVisionBarcode.FORMAT_QR_CODE
        const val FORMAT_UPC_A = FirebaseVisionBarcode.FORMAT_UPC_A
        const val FORMAT_UPC_E = FirebaseVisionBarcode.FORMAT_UPC_E
        const val FORMAT_PDF417 = FirebaseVisionBarcode.FORMAT_PDF417
        const val FORMAT_AZTEC = FirebaseVisionBarcode.FORMAT_AZTEC

        @IntDef(
            FORMAT_UNKNOWN,
            FORMAT_ALL_FORMATS,
            FORMAT_CODE_128,
            FORMAT_CODE_39,
            FORMAT_CODE_93,
            FORMAT_CODABAR,
            FORMAT_DATA_MATRIX,
            FORMAT_EAN_13,
            FORMAT_EAN_8,
            FORMAT_ITF,
            FORMAT_QR_CODE,
            FORMAT_UPC_A,
            FORMAT_UPC_E,
            FORMAT_PDF417,
            FORMAT_AZTEC
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class BarcodeFormat

        const val TYPE_UNKNOWN = FirebaseVisionBarcode.TYPE_UNKNOWN
        const val TYPE_CONTACT_INFO = FirebaseVisionBarcode.TYPE_CONTACT_INFO
        const val TYPE_EMAIL = FirebaseVisionBarcode.TYPE_EMAIL
        const val TYPE_ISBN = FirebaseVisionBarcode.TYPE_ISBN
        const val TYPE_PHONE = FirebaseVisionBarcode.TYPE_PHONE
        const val TYPE_PRODUCT = FirebaseVisionBarcode.TYPE_PRODUCT
        const val TYPE_SMS = FirebaseVisionBarcode.TYPE_SMS
        const val TYPE_TEXT = FirebaseVisionBarcode.TYPE_TEXT
        const val TYPE_URL = FirebaseVisionBarcode.TYPE_URL
        const val TYPE_WIFI = FirebaseVisionBarcode.TYPE_WIFI
        const val TYPE_GEO = FirebaseVisionBarcode.TYPE_GEO
        const val TYPE_CALENDAR_EVENT = FirebaseVisionBarcode.TYPE_CALENDAR_EVENT
        const val TYPE_DRIVER_LICENSE = FirebaseVisionBarcode.TYPE_DRIVER_LICENSE

        @IntDef(
            TYPE_UNKNOWN,
            TYPE_CONTACT_INFO,
            TYPE_EMAIL,
            TYPE_ISBN,
            TYPE_PHONE,
            TYPE_PRODUCT,
            TYPE_SMS,
            TYPE_TEXT,
            TYPE_URL,
            TYPE_WIFI,
            TYPE_GEO,
            TYPE_CALENDAR_EVENT,
            TYPE_DRIVER_LICENSE
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class BarcodeValueType
    }
}
