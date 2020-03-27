package uk.co.brightec.kbarcode.processor;

import androidx.annotation.NonNull;

import java.util.List;

import uk.co.brightec.kbarcode.Barcode;

// Java file for better inter-op with both languages
public interface OnBarcodesListener {

    void onBarcodes(@NonNull List<Barcode> barcodes);
}
