package uk.co.brightec.kbarcode.processor;

import androidx.annotation.NonNull;
import uk.co.brightec.kbarcode.Barcode;

// Java file for better inter-op with both languages
public interface OnBarcodeListener {

    void onBarcode(@NonNull Barcode barcode);
}
