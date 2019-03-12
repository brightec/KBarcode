package uk.co.brightec.kbarcode.processor;

import java.util.List;

import androidx.annotation.NonNull;
import uk.co.brightec.kbarcode.Barcode;

// Java file for better inter-op with both languages
public interface OnBarcodesListener {

    void onBarcodes(@NonNull List<Barcode> barcodes);
}
