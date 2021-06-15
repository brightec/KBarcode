package uk.co.brightec.kbarcode

import android.graphics.Point
import android.graphics.Rect
import androidx.annotation.IntDef
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
import com.google.mlkit.vision.barcode.Barcode as MlBarcode

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

    internal constructor(mlBarcode: MlBarcode) : this(
        boundingBox = mlBarcode.boundingBox,
        calendarEvent = mlBarcode.calendarEvent?.convert(),
        contactInfo = mlBarcode.contactInfo?.convert(),
        cornerPoints = mlBarcode.cornerPoints?.toList(),
        displayValue = mlBarcode.displayValue,
        driverLicense = mlBarcode.driverLicense?.convert(),
        email = mlBarcode.email?.convert(),
        format = mlBarcode.format,
        geoPoint = mlBarcode.geoPoint?.convert(),
        phone = mlBarcode.phone?.convert(),
        rawValue = mlBarcode.rawValue,
        sms = mlBarcode.sms?.convert(),
        url = mlBarcode.url?.convert(),
        valueType = mlBarcode.valueType,
        wifi = mlBarcode.wifi?.convert()
    )

    override fun toString() = displayValue ?: super.toString()

    companion object {

        const val FORMAT_UNKNOWN = MlBarcode.FORMAT_UNKNOWN
        const val FORMAT_ALL_FORMATS = MlBarcode.FORMAT_ALL_FORMATS
        const val FORMAT_CODE_128 = MlBarcode.FORMAT_CODE_128
        const val FORMAT_CODE_39 = MlBarcode.FORMAT_CODE_39
        const val FORMAT_CODE_93 = MlBarcode.FORMAT_CODE_93
        const val FORMAT_CODABAR = MlBarcode.FORMAT_CODABAR
        const val FORMAT_DATA_MATRIX = MlBarcode.FORMAT_DATA_MATRIX
        const val FORMAT_EAN_13 = MlBarcode.FORMAT_EAN_13
        const val FORMAT_EAN_8 = MlBarcode.FORMAT_EAN_8
        const val FORMAT_ITF = MlBarcode.FORMAT_ITF
        const val FORMAT_QR_CODE = MlBarcode.FORMAT_QR_CODE
        const val FORMAT_UPC_A = MlBarcode.FORMAT_UPC_A
        const val FORMAT_UPC_E = MlBarcode.FORMAT_UPC_E
        const val FORMAT_PDF417 = MlBarcode.FORMAT_PDF417
        const val FORMAT_AZTEC = MlBarcode.FORMAT_AZTEC

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

        const val TYPE_UNKNOWN = MlBarcode.TYPE_UNKNOWN
        const val TYPE_CONTACT_INFO = MlBarcode.TYPE_CONTACT_INFO
        const val TYPE_EMAIL = MlBarcode.TYPE_EMAIL
        const val TYPE_ISBN = MlBarcode.TYPE_ISBN
        const val TYPE_PHONE = MlBarcode.TYPE_PHONE
        const val TYPE_PRODUCT = MlBarcode.TYPE_PRODUCT
        const val TYPE_SMS = MlBarcode.TYPE_SMS
        const val TYPE_TEXT = MlBarcode.TYPE_TEXT
        const val TYPE_URL = MlBarcode.TYPE_URL
        const val TYPE_WIFI = MlBarcode.TYPE_WIFI
        const val TYPE_GEO = MlBarcode.TYPE_GEO
        const val TYPE_CALENDAR_EVENT = MlBarcode.TYPE_CALENDAR_EVENT
        const val TYPE_DRIVER_LICENSE = MlBarcode.TYPE_DRIVER_LICENSE

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
