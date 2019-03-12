package uk.co.brightec.kbarcode

import android.graphics.Point
import android.graphics.Rect
import androidx.annotation.IntDef
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import uk.co.brightec.kbarcode.util.OpenForTesting

@Suppress("unused") // API class
@OpenForTesting
data class Barcode internal constructor(
    private val fbBarcode: FirebaseVisionBarcode
) {

    val boundingBox: Rect?
        get() = fbBarcode.boundingBox
    val cornerPoints: Array<Point>?
        get() = fbBarcode.cornerPoints
    val displayValue: String?
        get() = fbBarcode.displayValue
    @BarcodeFormat
    val format: Int
        get() = fbBarcode.format
    val rawValue: String?
        get() = fbBarcode.rawValue
    @BarcodeValueType
    val valueType: Int
        get() = fbBarcode.valueType

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
