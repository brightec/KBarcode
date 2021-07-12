package uk.co.brightec.kbarcode.app.camerax

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutionException

internal class BarcodeAnalyzer(
    context: Context,
    private val barcodeListener: (List<Barcode>) -> Unit,
) : ImageAnalysis.Analyzer {

    private val tag = BarcodeAnalyzer::class.simpleName ?: "BarcodeAnalyzer"
    private val mainExecutor = ContextCompat.getMainExecutor(context)
    private val scanner: BarcodeScanner = createScanner()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = imageProxy.image
        if (image != null) {
            val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
            val task = scanner.process(inputImage)
            try {
                val result = Tasks.await(task)
                if (result.isNotEmpty()) {
                    mainExecutor.execute {
                        barcodeListener.invoke(result)
                    }
                }
            } catch (e: ExecutionException) {
                Log.e(tag, "Scanner process failed", e)
            } catch (e: InterruptedException) {
                Log.e(tag, "Scanner process interrupted", e)
            }
        }
        imageProxy.close()
    }

    private fun createScanner(): BarcodeScanner {
        val options = BarcodeScannerOptions.Builder()
        val formats = intArrayOf(
            Barcode.FORMAT_CODABAR,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E
        )
        if (formats.size > 1) {
            @Suppress("SpreadOperator") // Required by Google API
            options.setBarcodeFormats(
                formats[0], *formats.slice(IntRange(1, formats.size - 1)).toIntArray()
            )
        } else if (formats.size == 1) {
            options.setBarcodeFormats(formats[0])
        }
        return BarcodeScanning.getClient(options.build())
    }
}
