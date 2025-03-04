package com.trungld.viberide.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trungld.viberide.ui.screens.face_mesh_detection.FaceMeshDetectionView
import com.trungld.viberide.ui.screens.home.cards.MediaControlCard
import com.trungld.viberide.ui.screens.home.cards.MoodDetectionCard
import com.trungld.viberide.ui.screens.home.cards.RecommendationCard
import com.trungld.viberide.ui.screens.home.cards.YawnDetectionCard
import com.trungld.viberide.viewmodels.AudioViewModel
import com.trungld.viberide.viewmodels.AuthState
import com.trungld.viberide.viewmodels.AuthViewModel
import com.trungld.viberide.viewmodels.FaceEmotionViewModel
import com.trungld.viberide.viewmodels.UIEvents
import com.trungld.viberide.viewmodels.UIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    audioViewModel: AudioViewModel,
    faceEmotionViewModel: FaceEmotionViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val uiState = audioViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val exoPlayer = audioViewModel.audioServiceHandler.exoPlayer
    val mediaItems by audioViewModel.mediaList.collectAsState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = modifier,
                authState = authState.value,
                signOut = {
                    try {
                        authViewModel.signOut()
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Error logging out: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                logIn = {
                    navController.navigate("login")
                },
                signUp = {
                    navController.navigate("signup")
                }
            )
        }
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
        ) {

            Column(
                modifier = modifier
                    .fillMaxSize()
            ) {
                Row(
                    modifier = modifier
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uiState.value is UIState.Initial) {
                        CircularProgressIndicator(
                            modifier = modifier.width(50.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else
                        RecommendationCard(
                            modifier = Modifier.width(screenWidth / 3),
                            mediaItems = mediaItems,
                            onItemClick = {
                                audioViewModel.onUiEvents(UIEvents.SelectedAudioChange(it))
                            }
                        )
                    Spacer(modifier = modifier.width(16.dp))
                    Column {
                        if (uiState.value != UIState.Initial)
                            MediaControlCard(
                                modifier = Modifier
                                    .width(screenWidth / 3)
                                    .height(300.dp),
                                exoPlayer
                            )
                        Spacer(modifier = modifier.height(16.dp))
                        FaceMeshDetectionView(
                            modifier = Modifier
                                .width(screenWidth / 3)
                                .height(300.dp),
                            faceEmotionViewModel
                        )
                    }
                    Spacer(modifier = modifier.width(16.dp))

                    Column(modifier = modifier.weight(1f)) {
                        YawnDetectionCard()
                        Spacer(modifier = modifier.height(16.dp))
                        MoodDetectionCard()
                    }
                }
            }
        }

    }
}

