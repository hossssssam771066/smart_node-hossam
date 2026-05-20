package com.smartnode.app.presentation.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX [ImageAnalysis.Analyzer] that pipes each frame into the bundled
 * ML Kit barcode scanner.
 *
 * The scanner model ships inside the APK (`com.google.mlkit:barcode-scanning`
 * — *not* the play-services-vision variant), so detection runs entirely on
 * device without any network access. That's what makes the scanner usable
 * in the offline-first flow the operator needs in the field.
 */
class BarcodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val onBarcode: (rawValue: String, format: Int) -> Unit,
) : ImageAnalysis.Analyzer {

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val input = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )

        scanner.process(input)
            .addOnSuccessListener { barcodes: List<Barcode> ->
                val first = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                if (first != null) {
                    onBarcode(first.rawValue!!, first.format)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
