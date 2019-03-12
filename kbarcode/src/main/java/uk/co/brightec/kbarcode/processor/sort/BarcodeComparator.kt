package uk.co.brightec.kbarcode.processor.sort

import uk.co.brightec.kbarcode.Barcode
import uk.co.brightec.kbarcode.camera.FrameMetadata

abstract class BarcodeComparator : Comparator<Barcode> {

    lateinit var frameMetadata: FrameMetadata
}
