package uk.co.brightec.kbarcode

import android.hardware.camera2.CameraCharacteristics
import androidx.annotation.Px
import uk.co.brightec.kbarcode.processor.sort.BarcodeComparator

@Suppress("unused") // API class
data class Options(
    val cameraFacing: Int = CameraCharacteristics.LENS_FACING_BACK,
    val barcodeFormats: IntArray = intArrayOf(Barcode.FORMAT_ALL_FORMATS),
    @Px val minBarcodeWidth: Int? = null,
    val barcodesSort: BarcodeComparator? = null,
    @BarcodeView.ScaleType val scaleType: Int
) {

    class Builder(
        private var cameraFacing: Int = CameraCharacteristics.LENS_FACING_BACK,
        private var barcodeFormats: IntArray = intArrayOf(Barcode.FORMAT_ALL_FORMATS),
        @Px private var minBarcodeWidth: Int? = null,
        private var barcodesSort: BarcodeComparator? = null,
        @BarcodeView.ScaleType private var scaleType: Int = BarcodeView.CENTER_INSIDE
    ) {

        fun cameraFacing(cameraFacing: Int) = apply {
            this.cameraFacing = cameraFacing
        }

        fun barcodeFormats(barcodeFormats: IntArray) = apply {
            this.barcodeFormats = if (barcodeFormats.isEmpty()) {
                intArrayOf(Barcode.FORMAT_ALL_FORMATS)
            } else {
                barcodeFormats
            }
        }

        @Suppress("ArrayPrimitive") // Method is deprecated
        @Deprecated(
            message = "More efficient to use an array of primitives",
            replaceWith = ReplaceWith(
                expression = "barcodeFormats(\n" +
                        "// Consider using an IntArray\n" +
                        "barcodeFormats.toIntArray())"
            ),
            level = DeprecationLevel.ERROR
        )
        fun barcodeFormats(barcodeFormats: Array<Int>) = apply {
            barcodeFormats(barcodeFormats.toIntArray())
        }

        fun minBarcodeWidth(@Px minBarcodeWidth: Int) = apply {
            this.minBarcodeWidth = minBarcodeWidth
        }

        fun barcodesSort(barcodesSort: BarcodeComparator?) = apply {
            this.barcodesSort = barcodesSort
        }

        fun scaleType(@BarcodeView.ScaleType scaleType: Int) = apply {
            this.scaleType = scaleType
        }

        fun build() = Options(
            cameraFacing = cameraFacing,
            barcodeFormats = barcodeFormats,
            minBarcodeWidth = minBarcodeWidth,
            barcodesSort = barcodesSort,
            scaleType = scaleType
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Options

        if (cameraFacing != other.cameraFacing) return false
        if (!barcodeFormats.contentEquals(other.barcodeFormats)) return false
        if (minBarcodeWidth != other.minBarcodeWidth) return false
        if (barcodesSort != other.barcodesSort) return false
        if (scaleType != other.scaleType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cameraFacing
        result = 31 * result + barcodeFormats.contentHashCode()
        result = 31 * result + minBarcodeWidth.hashCode()
        result = 31 * result + (barcodesSort?.hashCode() ?: 0)
        result = 31 * result + scaleType.hashCode()
        return result
    }
}
