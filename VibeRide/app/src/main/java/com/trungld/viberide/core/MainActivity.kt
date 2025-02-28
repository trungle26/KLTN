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
    private val audioViewModel: AudioViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val faceEmotionViewModel : FaceEmotionViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VibeRideTheme {
                AppNavigation(
                    modifier = Modifier,
                    authViewModel = authViewModel,
                    audioViewModel = audioViewModel,
                    faceEmotionViewModel = faceEmotionViewModel
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

//    private var isServiceRunning = false
//    private fun startService() {
//        if (!isServiceRunning) {
//            val intent = Intent(this, VibeRideAudioService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(intent)
//                Log.d("service", "startService: ")
//            } else {
//                startService(intent)
//            }
//            isServiceRunning = true
//        }
//    }

    override fun onDestroy() {

        super.onDestroy()
    }
}

