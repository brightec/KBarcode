package uk.co.brightec.kbarcode.processor.sort

import uk.co.brightec.kbarcode.Barcode
import uk.co.brightec.kbarcode.camera.FrameMetadata

public abstract class BarcodeComparator : Comparator<Barcode> {

    public lateinit var frameMetadata: FrameMetadata
}
