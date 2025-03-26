package com.trungld.viberide.core

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import com.trungld.viberide.player.service.VibeRideAudioService
import com.trungld.viberide.ui.theme.VibeRideTheme
import com.trungld.viberide.viewmodels.AudioViewModel
import com.trungld.viberide.viewmodels.AuthViewModel
import com.trungld.viberide.viewmodels.FaceEmotionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var cameraManager: CameraManager

    private val audioViewModel: AudioViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val faceEmotionViewModel : FaceEmotionViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraManager = CameraManager(this)
        // Initialize camera with activity lifecycle
        val analyzer = FaceMeshDetectionAnalyzer { meshes, _, _ ->
            faceEmotionViewModel.updateFaceMeshes(meshes)
            faceEmotionViewModel.updateEmotionFromFaceMesh(if (meshes.isNotEmpty()) meshes.first() else null)
        }
        cameraManager.startCamera(this, analyzer)

        setContent {
            VibeRideTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    audioViewModel = audioViewModel,
                    faceEmotionViewModel = faceEmotionViewModel,
                    cameraManager = cameraManager,
                )

            }
        }
        val intent = Intent(this, VibeRideAudioService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
            Log.d("service", "startService: ")
        } else {
            startService(intent)
        }
    }


    override fun onDestroy() {
        stopService(Intent(this, VibeRideAudioService::class.java))
        cameraManager.stopCamera()
        super.onDestroy()
    }
}

