package com.trungld.viberide.core

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMesh
import com.google.mlkit.vision.facemesh.FaceMeshDetection

@SuppressLint("UnsafeOptInUsageError")
class FaceMeshDetectionAnalyzer(
    private val onFaceMeshDetected: (faces: MutableList<FaceMesh>, width: Int, height: Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val meshDetector = FaceMeshDetection.getClient()

    // Throttle settings: process one frame every 200ms (~5 FPS)
    private val desiredIntervalMillis = 200L
    private var lastAnalyzedTimestamp = 0L

    override fun analyze(imageProxy: ImageProxy) {
        // Check if enough time has passed since the last processed frame
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp < desiredIntervalMillis) {
            imageProxy.close() // Skip processing for this frame
            return
        }
        lastAnalyzedTimestamp = currentTimestamp

        imageProxy.image?.let { mediaImage ->
            val imageValue = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            meshDetector.process(imageValue)
                .addOnSuccessListener { meshes ->
                    onFaceMeshDetected(meshes, imageProxy.width, imageProxy.height)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } ?: run {
            imageProxy.close()
        }
    }
}