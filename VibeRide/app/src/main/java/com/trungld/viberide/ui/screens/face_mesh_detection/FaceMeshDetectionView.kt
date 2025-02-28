package com.trungld.viberide.ui.screens.face_mesh_detection

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import androidx.compose.ui.platform.LocalContext
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
            ScanSurface(modifier.fillMaxSize(),faceEmotionViewModel)
        }
    )
}

@Composable
fun ScanSurface(
    modifier: Modifier = Modifier,
    viewModel: FaceEmotionViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var faceMeshes: MutableList<FaceMesh> = remember { mutableStateListOf<FaceMesh>() }

//    val screenWidth = remember { mutableStateOf(context.resources.displayMetrics.widthPixels) }
//    val screenHeight = remember { mutableStateOf(context.resources.displayMetrics.heightPixels) }

    val imageWidth = remember { mutableStateOf(0) }
    val imageHeight = remember { mutableStateOf(0) }

    val analyzer = FaceMeshDetectionAnalyzer { meshes, _, _ ->
        faceMeshes.clear()         // Clear the current contents
        faceMeshes.addAll(meshes)  // Add the new meshes to the existing list
        viewModel.updateEmotionFromFaceMesh(if (meshes.isNotEmpty()) meshes.first() else null)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        CameraPreview(
            context = context,
            lifecycleOwner = lifecycleOwner,
            analyzer = analyzer
        )

        DrawFaces(
            faceMeshes = faceMeshes,
        )

        // Display the current emotion
        val emotion by viewModel.currentEmotion.observeAsState("Neutral")
        Text(
            text = "Emotion: $emotion",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}


@Composable
fun DrawFaces(
    faceMeshes: List<FaceMesh>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Define a fixed target rectangle (300 x 300) centered in the canvas.
        val targetWidth = 500f
        val targetHeight = 500f
        val targetLeft = (size.width - targetWidth) / 2f
        val targetTop = (size.height - targetHeight) / 2f
        val targetRect =
            Rect(targetLeft, targetTop, targetLeft + targetWidth, targetTop + targetHeight)

        // (Optional) Draw a border for the static target area.
        drawRect(
            color = Color.Yellow,
            topLeft = Offset(targetRect.left, targetRect.top),
            size = Size(targetRect.width, targetRect.height),
            style = Stroke(width = 3f)
        )

        faceMeshes.forEach { face ->
            // Get the face bounding box in image coordinates.
            val faceBox = face.boundingBox.toComposeRect()
            // Draw each landmark using the utility function.
            face.allPoints.forEach { landmark ->
                val mappedPoint = mapFacePointToTarget(faceBox, landmark.position, targetRect)
                drawCircle(
                    color = Color.Cyan,
                    radius = 3f,
                    center = mappedPoint
                )
            }

            // Draw triangles.
            face.allTriangles.forEach { triangle ->
                val points = triangle.allPoints.map {
                    mapFacePointToTarget(
                        faceBox,
                        it.position,
                        targetRect
                    )
                }
                if (points.size >= 3) {
                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        lineTo(points[1].x, points[1].y)
                        lineTo(points[2].x, points[2].y)
                        close()
                    }
                    drawPath(path, color = Color.Cyan, style = Stroke(width = 1f))
                }
            }

            // Emotion inference: Happiness example
            val leftMouth = mapFacePointToTarget(faceBox, face.allPoints[61].position, targetRect)
            val rightMouth = mapFacePointToTarget(faceBox, face.allPoints[291].position, targetRect)
            val mouthWidth = Math.abs(leftMouth.x - rightMouth.x)

            val leftCheek = mapFacePointToTarget(faceBox, face.allPoints[234].position, targetRect)
            val rightCheek = mapFacePointToTarget(faceBox, face.allPoints[454].position, targetRect)
            val faceWidth = Math.abs(leftCheek.x - rightCheek.x)
            val normalizedMouthWidth = mouthWidth / faceWidth

            val emotion = if (normalizedMouthWidth > 0.5) "Happy" else "Neutral"
            // Draw the emotion text on the canvas
            drawContext.canvas.nativeCanvas.drawText(
                emotion,
                targetRect.left,
                targetRect.top - 20f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 40f
                }
            )
        }
    }
}