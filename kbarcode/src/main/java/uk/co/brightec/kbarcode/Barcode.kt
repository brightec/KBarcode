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
 * This class wraps the underlying Barcode
 * https://developers.google.com/android/reference/com/google/mlkit/vision/barcode/Barcode
 */
@OpenForTesting
public data class Barcode(
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

    override fun toString(): String = displayValue ?: super.toString()

    public companion object {

        public const val FORMAT_UNKNOWN: Int = MlBarcode.FORMAT_UNKNOWN
        public const val FORMAT_ALL_FORMATS: Int = MlBarcode.FORMAT_ALL_FORMATS
        public const val FORMAT_CODE_128: Int = MlBarcode.FORMAT_CODE_128
        public const val FORMAT_CODE_39: Int = MlBarcode.FORMAT_CODE_39
        public const val FORMAT_CODE_93: Int = MlBarcode.FORMAT_CODE_93
        public const val FORMAT_CODABAR: Int = MlBarcode.FORMAT_CODABAR
        public const val FORMAT_DATA_MATRIX: Int = MlBarcode.FORMAT_DATA_MATRIX
        public const val FORMAT_EAN_13: Int = MlBarcode.FORMAT_EAN_13
        public const val FORMAT_EAN_8: Int = MlBarcode.FORMAT_EAN_8
        public const val FORMAT_ITF: Int = MlBarcode.FORMAT_ITF
        public const val FORMAT_QR_CODE: Int = MlBarcode.FORMAT_QR_CODE
        public const val FORMAT_UPC_A: Int = MlBarcode.FORMAT_UPC_A
        public const val FORMAT_UPC_E: Int = MlBarcode.FORMAT_UPC_E
        public const val FORMAT_PDF417: Int = MlBarcode.FORMAT_PDF417
        public const val FORMAT_AZTEC: Int = MlBarcode.FORMAT_AZTEC

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
        public annotation class BarcodeFormat

        public const val TYPE_UNKNOWN: Int = MlBarcode.TYPE_UNKNOWN
        public const val TYPE_CONTACT_INFO: Int = MlBarcode.TYPE_CONTACT_INFO
        public const val TYPE_EMAIL: Int = MlBarcode.TYPE_EMAIL
        public const val TYPE_ISBN: Int = MlBarcode.TYPE_ISBN
        public const val TYPE_PHONE: Int = MlBarcode.TYPE_PHONE
        public const val TYPE_PRODUCT: Int = MlBarcode.TYPE_PRODUCT
        public const val TYPE_SMS: Int = MlBarcode.TYPE_SMS
        public const val TYPE_TEXT: Int = MlBarcode.TYPE_TEXT
        public const val TYPE_URL: Int = MlBarcode.TYPE_URL
        public const val TYPE_WIFI: Int = MlBarcode.TYPE_WIFI
        public const val TYPE_GEO: Int = MlBarcode.TYPE_GEO
        public const val TYPE_CALENDAR_EVENT: Int = MlBarcode.TYPE_CALENDAR_EVENT
        public const val TYPE_DRIVER_LICENSE: Int = MlBarcode.TYPE_DRIVER_LICENSE

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
        public annotation class BarcodeValueType
    }
}
