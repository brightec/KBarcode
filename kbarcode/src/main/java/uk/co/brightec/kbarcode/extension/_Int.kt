package uk.co.brightec.kbarcode.extension

import androidx.annotation.Px
import uk.co.brightec.kbarcode.Barcode

/**
 * The minimum pixel required for good quality results. Going for a larger size than these will
 * produce better results but will slow down performance. So it is recommended you use as close
 * to these minimums as you can.
 *
 * https://firebase.google.com/docs/ml-kit/android/read-barcodes#input-image-guidelines
 */
@Suppress("MagicNumber", "ComplexMethod")
@Px
internal fun @receiver:Barcode.Companion.BarcodeFormat Int.getMinWidth() = when (this) {
    Barcode.FORMAT_UNKNOWN, Barcode.FORMAT_ALL_FORMATS -> BARCODE_FORMAT_ALL_MIN_WIDTH
    Barcode.FORMAT_CODE_128 -> 11 * NUM_CHARS // 11px per char
    Barcode.FORMAT_CODE_39 -> 16 * NUM_CHARS // 16px per char
    Barcode.FORMAT_CODE_93 -> 9 * NUM_CHARS // 9px per char
    Barcode.FORMAT_CODABAR -> 11 * NUM_CHARS // 10.75px per char
    Barcode.FORMAT_DATA_MATRIX -> 1 * NUM_CELLS // 1px per cell
    Barcode.FORMAT_EAN_13 -> 2 * 95 // 2px per unit, 95 units total
    Barcode.FORMAT_EAN_8 -> 2 * 67 // 2px per unit, 67 units total
    Barcode.FORMAT_ITF -> 7 * NUM_CHARS // 7px per single digit
    Barcode.FORMAT_QR_CODE -> 1 * NUM_CELLS // 1px per cell
    Barcode.FORMAT_UPC_A -> 2 * 95 // 2px per unit, 95 units total
    Barcode.FORMAT_UPC_E -> 2 * 51 // 2px per unit, 51 units total
    Barcode.FORMAT_PDF417 -> 2 * 17 * 34 // 2px per unit, 17 units per word, 34 words
    Barcode.FORMAT_AZTEC -> 1 * NUM_CELLS // 1px per cell
    else -> BARCODE_FORMAT_ALL_MIN_WIDTH
}

internal const val BARCODE_FORMAT_ALL_MIN_WIDTH = 2 * 17 * 34
private const val NUM_CHARS = 20
private const val NUM_CELLS = 200
