package uk.co.brightec.kbarcode.app.camerax

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import uk.co.brightec.kbarcode.app.R
import uk.co.brightec.kbarcode.app.databinding.ActivityCameraxBinding
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class CameraXActivity :
    AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val tag = CameraXActivity::class.simpleName ?: "CameraXActivity"

    private lateinit var binding: ActivityCameraxBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var camera: Camera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraxBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setTitle(R.string.title_camerax)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
        } else {
            startCamera()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
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
                startCamera()
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                val previewUseCase = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.preview.surfaceProvider)
                    }
                val imageAnalyzerUseCase = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            cameraExecutor,
                            BarcodeAnalyzer(this) { barcodes ->
                                val builder = StringBuilder()
                                for (barcode in barcodes) {
                                    builder.append(barcode.displayValue).append("\n")
                                }
                                binding.textBarcodes.text = builder.toString()
                            }
                        )
                    }
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, previewUseCase, imageAnalyzerUseCase
                )
                setupTapToFocus()
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun setupTapToFocus() {
        binding.preview.setOnTouchListener { view, event ->
            val actionMasked = event.actionMasked
            if (actionMasked == MotionEvent.ACTION_UP) {
                view.performClick()
                return@setOnTouchListener false
            }
            if (actionMasked != MotionEvent.ACTION_DOWN) {
                return@setOnTouchListener false
            }

            val cameraControl = camera.cameraControl
            val factory = binding.preview.meteringPointFactory
            val point = factory.createPoint(event.x, event.y)
            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                .addPoint(point, FocusMeteringAction.FLAG_AE)
                .addPoint(point, FocusMeteringAction.FLAG_AWB)
                .build()
            val future = cameraControl.startFocusAndMetering(action)
            future.addListener(
                {
                    try {
                        val result = future.get()
                        Log.d(tag, "Focus Success: ${result.isFocusSuccessful}")
                    } catch (e: CameraControl.OperationCanceledException) {
                        Log.d(tag, "Focus cancelled")
                    } catch (e: ExecutionException) {
                        Log.e(tag, "Focus failed", e)
                    } catch (e: InterruptedException) {
                        Log.e(tag, "Focus interrupted", e)
                    }
                },
                cameraExecutor
            )
            true
        }
    }

    companion object {

        private const val REQUEST_PERMISSION_CAMERA = 1

        fun getStartingIntent(context: Context) = Intent(context, CameraXActivity::class.java)
    }
}
