package uk.co.brightec.kbarcode.app;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import uk.co.brightec.kbarcode.Barcode;
import uk.co.brightec.kbarcode.BarcodeView;

public class XmlJavaActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_PERMISSION_CAMERA = 1;

    @NonNull
    public static Intent getStartingIntent(@NonNull Context context) {
        return new Intent(context, XmlJavaActivity.class);
    }

    private BarcodeView mBarcodeView;
    private TextView mTextBarcodes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml);
        getWindow().getDecorView().setBackgroundColor(
                ContextCompat.getColor(this, R.color.black)
        );
        setTitle(R.string.title_xml_java);

        mBarcodeView = findViewById(R.id.view_barcode);
        mTextBarcodes = findViewById(R.id.text_barcodes);

        getLifecycle().addObserver(mBarcodeView);

        mBarcodeView.getBarcodes().observe(this, new Observer<List<Barcode>>() {
            @Override
            public void onChanged(@NonNull List<Barcode> barcodes) {
                StringBuilder builder = new StringBuilder();
                for (Barcode barcode : barcodes) {
                    builder.append(barcode.getDisplayValue()).append('\n');
                }
                mTextBarcodes.setText(builder.toString());
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mBarcodeView.start();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
        )) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_camera_rationale)
                    .setMessage(R.string.message_camera_rationale)
                    .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    XmlJavaActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_PERMISSION_CAMERA
                            );
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_PERMISSION_CAMERA
            );
        }
    }
}
