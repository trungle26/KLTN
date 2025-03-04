package com.trungld.viberide.ui.screens.face_mesh_detection

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.facemesh.FaceMesh
import com.trungld.viberide.core.FaceMeshDetectionAnalyzer
import com.trungld.viberide.ui.screens.shared.components.CameraPreview
import com.trungld.viberide.ui.screens.shared.utils.mapFacePointToTarget
import com.trungld.viberide.viewmodels.FaceEmotionViewModel
import kotlin.math.abs

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceMeshDetectionView(
    modifier: Modifier = Modifier,
    faceEmotionViewModel: FaceEmotionViewModel
) {
    val context = LocalContext.current
    val cameraPermissionState =
        rememberPermissionState(permission = android.Manifest.permission.CAMERA)

    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        permissionNotAvailableContent = {
            Column {
                Toast.makeText(context, "Permission denied.", Toast.LENGTH_LONG).show()
            }
        },
        content = {
            ScanSurface(modifier.fillMaxSize(), faceEmotionViewModel)
        }
    )
}

@Composable
fun ScanSurface(
    modifier: Modifier = Modifier,
    viewModel: FaceEmotionViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var faceMeshes: MutableList<FaceMesh> = remember { mutableStateListOf<FaceMesh>() }

    var size by remember { mutableStateOf(IntSize.Zero) }

    val analyzer = FaceMeshDetectionAnalyzer { meshes, _, _ ->
        faceMeshes.clear()         // Clear the current contents
        faceMeshes.addAll(meshes)  // Add the new meshes to the existing list
        viewModel.updateEmotionFromFaceMesh(if (meshes.isNotEmpty()) meshes.first() else null)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size = it }
    ) {
        CameraPreview(
            context = context,
            lifecycleOwner = lifecycleOwner,
            analyzer = analyzer,
            showPreview = false,
        )

        DrawFaces(
            faceMeshes = faceMeshes,
            targetHeight = size.height.toFloat(),
        )

        // Display the current emotion
        val emotion by viewModel.currentEmotion.collectAsState()
        Text(
            text = "Emotion: ${emotion.dominantEmotion}, Intensity: ${emotion.emotionIntensity}",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}


@Composable
fun DrawFaces(
    faceMeshes: List<FaceMesh>,
    targetHeight: Float,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Calculate the target width with 1.618:1 aspect ratio (golden ratio)
        val targetWidth = (targetHeight / 1.618f)
        val targetLeft = (size.width - targetWidth) / 2f
        val targetTop = (size.height - targetHeight) / 2f
        val targetRect = Rect(targetLeft, targetTop, targetLeft + targetWidth, targetTop + targetHeight)

        faceMeshes.forEach { face ->
            // Get the face bounding box in image coordinates
            val faceBox = face.boundingBox.toComposeRect()

            // Draw each landmark with flipped x-coordinate
            face.allPoints.forEach { landmark ->
                val originalPoint = mapFacePointToTarget(faceBox, landmark.position, targetRect)
                val flippedX = size.width - originalPoint.x // Flip around canvas center
                val mappedPoint = Offset(flippedX, originalPoint.y)
                drawCircle(
                    color = Color.Cyan,
                    radius = 1f,
                    center = mappedPoint
                )
            }

            // Draw triangles with flipped x-coordinates
            face.allTriangles.forEach { triangle ->
                val points = triangle.allPoints.map {
                    val originalPoint = mapFacePointToTarget(faceBox, it.position, targetRect)
                    val flippedX = size.width - originalPoint.x // Flip around canvas center
                    Offset(flippedX, originalPoint.y)
                }
                if (points.size >= 3) {
                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        lineTo(points[1].x, points[1].y)
                        lineTo(points[2].x, points[2].y)
                        close()
                    }
                    drawPath(path, color = Color.Cyan, style = Stroke(width = 0.5f))
                }
            }
        }
    }
}