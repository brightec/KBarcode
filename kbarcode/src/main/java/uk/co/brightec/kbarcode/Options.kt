package uk.co.brightec.kbarcode

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import androidx.annotation.Px
import uk.co.brightec.kbarcode.processor.sort.BarcodeComparator

@Suppress("unused") // API class
public data class Options(
    val cameraFacing: Int = CameraCharacteristics.LENS_FACING_BACK,
    val cameraFlashMode: Int = CameraMetadata.FLASH_MODE_OFF,
    val barcodeFormats: IntArray = intArrayOf(Barcode.FORMAT_ALL_FORMATS),
    @Px val minBarcodeWidth: Int? = null,
    val barcodesSort: BarcodeComparator? = null,
    @BarcodeView.PreviewScaleType val previewScaleType: Int,
    val clearFocusDelay: Long
) {

    public class Builder(
        private var cameraFacing: Int = CameraCharacteristics.LENS_FACING_BACK,
        private var cameraFlashMode: Int = CameraMetadata.FLASH_MODE_OFF,
        private var barcodeFormats: IntArray = intArrayOf(Barcode.FORMAT_ALL_FORMATS),
        @Px private var minBarcodeWidth: Int? = null,
        private var barcodesSort: BarcodeComparator? = null,
        @BarcodeView.PreviewScaleType private var previewScaleType: Int = BarcodeView.CENTER_INSIDE,
        private var clearFocusDelay: Long = BarcodeView.CLEAR_FOCUS_DELAY_DEFAULT
    ) {

        public fun cameraFacing(cameraFacing: Int): Builder = apply {
            this.cameraFacing = cameraFacing
        }

        public fun cameraFlashMode(cameraFlashMode: Int): Builder = apply {
            this.cameraFlashMode = cameraFlashMode
        }

        public fun barcodeFormats(barcodeFormats: IntArray): Builder = apply {
            this.barcodeFormats = if (barcodeFormats.isEmpty()) {
                intArrayOf(Barcode.FORMAT_ALL_FORMATS)
            } else {
                barcodeFormats
            }
        }

        public fun minBarcodeWidth(@Px minBarcodeWidth: Int): Builder = apply {
            this.minBarcodeWidth = minBarcodeWidth
        }

        public fun barcodesSort(barcodesSort: BarcodeComparator?): Builder = apply {
            this.barcodesSort = barcodesSort
        }

        public fun previewScaleType(
            @BarcodeView.PreviewScaleType previewScaleType: Int
        ): Builder = apply {
            this.previewScaleType = previewScaleType
        }

        public fun clearFocusDelay(clearFocusDelay: Long): Builder = apply {
            this.clearFocusDelay = clearFocusDelay
        }

        public fun build(): Options = Options(
            cameraFacing = cameraFacing,
            cameraFlashMode = cameraFlashMode,
            barcodeFormats = barcodeFormats,
            minBarcodeWidth = minBarcodeWidth,
            barcodesSort = barcodesSort,
            previewScaleType = previewScaleType,
            clearFocusDelay = clearFocusDelay
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Options

        if (cameraFacing != other.cameraFacing) return false
        if (cameraFlashMode != other.cameraFlashMode) return false
        if (!barcodeFormats.contentEquals(other.barcodeFormats)) return false
        if (minBarcodeWidth != other.minBarcodeWidth) return false
        if (barcodesSort != other.barcodesSort) return false
        if (previewScaleType != other.previewScaleType) return false
        if (clearFocusDelay != other.clearFocusDelay) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cameraFacing
        result = 31 * result + cameraFlashMode.hashCode()
        result = 31 * result + barcodeFormats.contentHashCode()
        result = 31 * result + minBarcodeWidth.hashCode()
        result = 31 * result + (barcodesSort?.hashCode() ?: 0)
        result = 31 * result + previewScaleType.hashCode()
        result = 31 * result + clearFocusDelay.hashCode()
        return result
    }
}
