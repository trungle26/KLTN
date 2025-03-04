package com.trungld.viberide.ui.screens.shared.components

import android.content.Context
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    context: Context,
    analyzer: ImageAnalysis.Analyzer,
    lifecycleOwner: LifecycleOwner,
    showPreview: Boolean = true // Default to true for backward compatibility
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    val executor = ContextCompat.getMainExecutor(context)
    val cameraProvider = cameraProviderFuture.get()
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                // Hide the preview if showPreview is false
                visibility = if (showPreview) View.VISIBLE else View.GONE
            }
            cameraProviderFuture.addListener({
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(cameraExecutor, analyzer)
                    }
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()
                cameraProvider.unbindAll()

                if (showPreview) {
                    // Bind both Preview and ImageAnalysis if showing preview
                    preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        imageAnalysis,
                        preview
                    )
                } else {
                    // Only bind ImageAnalysis if no preview
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        imageAnalysis
                    )
                }
            }, executor)
            previewView
        }
    )
}