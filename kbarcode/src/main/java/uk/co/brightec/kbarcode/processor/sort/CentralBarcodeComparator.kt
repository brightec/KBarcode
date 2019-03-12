package uk.co.brightec.kbarcode.processor.sort

import android.graphics.Point
import uk.co.brightec.kbarcode.Barcode
import uk.co.brightec.kbarcode.extension.distanceTo

class CentralBarcodeComparator : BarcodeComparator() {

    private val centerFrame by lazy {
        Point(frameMetadata.width / 2, frameMetadata.height / 2)
    }

    override fun compare(o1: Barcode?, o2: Barcode?): Int {
        val boundBoxO1 = o1?.boundingBox
        val boundBoxO2 = o2?.boundingBox
        return when {
            boundBoxO1 == null -> 1
            boundBoxO2 == null -> -1
            else -> {
                val centerO1 = Point(boundBoxO1.centerX(), boundBoxO1.centerY())
                val centerO2 = Point(boundBoxO2.centerX(), boundBoxO2.centerY())
                val distanceO1 = centerO1.distanceTo(centerFrame)
                val distanceO2 = centerO2.distanceTo(centerFrame)
                distanceO2.compareTo(distanceO1)
            }
        }
    }
}
