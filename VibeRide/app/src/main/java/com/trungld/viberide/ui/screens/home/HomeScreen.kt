package com.trungld.viberide.ui.screens.home

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trungld.viberide.core.CameraManager
import com.trungld.viberide.ui.screens.shared.components.FaceMeshDetectionView
import com.trungld.viberide.ui.screens.shared.components.LoadingIndicator
import com.trungld.viberide.ui.screens.shared.components.cards.MediaControlCard
import com.trungld.viberide.ui.screens.shared.components.cards.MediaListCard
import com.trungld.viberide.ui.screens.shared.components.cards.MoodDetectionCard
import com.trungld.viberide.ui.screens.shared.components.cards.YawnDetectionCard
import com.trungld.viberide.viewmodels.AudioViewModel
import com.trungld.viberide.viewmodels.AuthState
import com.trungld.viberide.viewmodels.AuthViewModel
import com.trungld.viberide.viewmodels.Emotion
import com.trungld.viberide.viewmodels.FaceEmotionViewModel
import com.trungld.viberide.viewmodels.FetchingState
import com.trungld.viberide.viewmodels.UIEvents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    audioViewModel: AudioViewModel,
    faceEmotionViewModel: FaceEmotionViewModel,
    cameraManager: CameraManager
) {
    val authState = authViewModel.authState.observeAsState()
    val fetchingState by audioViewModel.fetchingState.collectAsState()
    val context = LocalContext.current

    val recommendedMediaItems by audioViewModel.recommendedMediaList.collectAsState()
    val emotion by faceEmotionViewModel.currentEmotion.collectAsState()
    val faceMeshes by faceEmotionViewModel.faceMeshes.collectAsState()
    val yawnCount by faceEmotionViewModel.yawnCount.collectAsState()

    val emotionString = when (emotion.dominantEmotion) {
        is Emotion.Happy -> "Happy"
        is Emotion.Sad -> "Sad"
        is Emotion.Angry -> "Angry"
        is Emotion.Calm -> "Calm"
        is Emotion.Unrecognized -> "Unrecognized"
    }

    // Map emotions to gradient colors
    val (startColor, endColor) = when (emotion.dominantEmotion) {
        is Emotion.Happy -> Color(0xFFFFD700) to Color(0xFFFFA500) // Gold to Orange
        is Emotion.Sad -> Color(0xFF1E90FF) to Color(0xFF00BFFF)   // DodgerBlue to DeepSkyBlue
        is Emotion.Angry -> Color(0xFFFF4500) to Color(0xFFDC143C) // OrangeRed to Crimson
        is Emotion.Calm -> Color(0xFF7FFFD4) to Color(0xFF00FA9A)  // Aquamarine to MediumSpringGreen
        is Emotion.Unrecognized -> Color(0xFFFFD700) to Color(0xFFA9A9A9)    // Gray to DarkGray
    }

    // Animate the colors
    val animatedStartColor by animateColorAsState(
        targetValue = startColor,
        animationSpec = tween(durationMillis = 500), label = "" // 500ms transition
    )
    val animatedEndColor by animateColorAsState(
        targetValue = endColor,
        animationSpec = tween(durationMillis = 500), label = ""
    )

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

    if (recommendedMediaItems.isEmpty()) {
        LaunchedEffect(emotionString) {
            audioViewModel.suggestMediaByEmotion(emotionString)
        }
    }

    Scaffold(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(animatedStartColor, animatedEndColor)
                )
            ),
        topBar = {
            TopAppBar(
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
                },
                onSearch = { query ->
                    navController.navigate("search_results/$query")
                }
            )
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(it)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (fetchingState is FetchingState.Loading) {
                    LoadingIndicator(modifier = Modifier.weight(1f))
                } else
                    MediaListCard(
                        modifier = Modifier
                            .weight(1f),
                        mediaItems = recommendedMediaItems,
                        maxItems = 3,
                        onItemClick = { index ->
                            audioViewModel.apply {
                                updateMediaItems()
                                onUiEvents(UIEvents.SelectedAudioChange(index))
                            }
                        },
                        title = "Recommended for your mood"
                    )
                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    YawnDetectionCard(
                        modifier = Modifier.weight(2f),
                        emotion = emotion,
                        yawnCount = yawnCount
                    ){
                        faceEmotionViewModel.onYawnDetected()
                    }
                    MediaControlCard(
                        modifier = Modifier
                            .weight(3f),
                        exoPlayer = audioViewModel.audioServiceHandler.exoPlayer,
                        isPlaying = audioViewModel.isPlaying,
                        onReplayClick = { audioViewModel.onUiEvents(UIEvents.Backward) },
                        onPauseClick = { audioViewModel.onUiEvents(UIEvents.PlayPause) },
                        onForwardClick = { audioViewModel.onUiEvents(UIEvents.Forward) },
                        progress = audioViewModel.progress,
                        onSeek = { audioViewModel.onUiEvents(UIEvents.SeekTo(it)) },
                        onFullScreenClick = {
                            navController.navigate("now_playing")
                        }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    FaceMeshDetectionView(
                        modifier = Modifier
                            .weight(1f),
                        faceMeshes,
                        emotion
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MoodDetectionCard(
                        modifier = Modifier.weight(1f), emotion = emotion,
                        onPlayPlaylist = { audioViewModel.suggestMediaByEmotion(emotionString) },
                        isFetching = fetchingState is FetchingState.Loading
                    )
                }
            }

        }

    }
}

