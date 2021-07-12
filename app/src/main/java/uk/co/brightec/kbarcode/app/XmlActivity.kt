package uk.co.brightec.kbarcode.app

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import uk.co.brightec.kbarcode.app.databinding.ActivityXmlBinding

internal class XmlActivity :
    AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityXmlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXmlBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
        setTitle(R.string.title_xml)

        lifecycle.addObserver(binding.viewBarcode)

        binding.viewBarcode.barcodes.observe(this) { barcodes ->
            val builder = StringBuilder()
            for (barcode in barcodes) {
                builder.append(barcode.displayValue).append("\n")
            }
            binding.textBarcodes.text = builder.toString()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_CAMERA -> if (
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                binding.viewBarcode.start()
            }
            else ->
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle(R.string.title_camera_rationale)
                .setMessage(R.string.message_camera_rationale)
                .setPositiveButton(R.string.action_ok) { _: DialogInterface, _: Int ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_PERMISSION_CAMERA
                    )
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CAMERA
            )
        }
    }

    companion object {

        private const val REQUEST_PERMISSION_CAMERA = 1

        fun getStartingIntent(context: Context) = Intent(context, XmlActivity::class.java)
    }
}
