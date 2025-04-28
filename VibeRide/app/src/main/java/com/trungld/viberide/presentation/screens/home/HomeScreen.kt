package com.trungld.viberide.presentation.screens.home

import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trungld.viberide.R
import com.trungld.viberide.presentation.screens.shared.components.FaceMeshDetectionView
import com.trungld.viberide.presentation.screens.shared.components.LoadingIndicator
import com.trungld.viberide.presentation.screens.shared.components.cards.CardContainer
import com.trungld.viberide.presentation.screens.shared.components.cards.HorizontalMediaList
import com.trungld.viberide.presentation.screens.shared.components.cards.HorizontalPagedMediaList
import com.trungld.viberide.presentation.screens.shared.components.cards.MediaControl
import com.trungld.viberide.presentation.screens.shared.components.cards.MoodDetection
import com.trungld.viberide.presentation.screens.shared.components.cards.SleepinessDetection
import com.trungld.viberide.presentation.viewmodels.AudioViewModel
import com.trungld.viberide.presentation.viewmodels.AuthState
import com.trungld.viberide.presentation.viewmodels.AuthViewModel
import com.trungld.viberide.presentation.viewmodels.Emotion
import com.trungld.viberide.presentation.viewmodels.FaceEmotionViewModel
import com.trungld.viberide.presentation.viewmodels.FetchingState
import com.trungld.viberide.presentation.viewmodels.UIEvents
import com.trungld.viberide.presentation.viewmodels.UIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    audioViewModel: AudioViewModel,
    faceEmotionViewModel: FaceEmotionViewModel,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var showAccountDialog by remember { mutableStateOf(false) } // State for dialog visibility

    val authState = authViewModel.authState.collectAsState()
    val fetchingState by audioViewModel.fetchingState.collectAsState()
    val context = LocalContext.current

    val recommendedMediaItems by audioViewModel.recommendedMediaList.collectAsState()
    val emotion by faceEmotionViewModel.currentEmotion.collectAsState()
    val showWarningDialog by faceEmotionViewModel.showWarningDialog.collectAsState()
    val warningCount by faceEmotionViewModel.warningCount.collectAsState()
    val favoritesMediaItems by audioViewModel.favoritesMediaList.collectAsState()

    val emotionString = when (emotion.dominantEmotion) {
        is Emotion.Happy -> "Happy"
        is Emotion.Sad -> "Sad"
        is Emotion.Angry -> "Angry"
        is Emotion.Calm -> "Calm"
        is Emotion.Unrecognized -> "Unrecognized"
    }

    // MediaPlayer to play beep sound when user is sleepy for too long
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.beep).apply {
            isLooping = true // Lặp lại âm thanh
        }
    }


    // Map emotions to gradient colors
    val (startColor, endColor) = when (emotion.dominantEmotion) {
        is Emotion.Happy -> Color(0xffc2a200) to Color(0xFFFFA500) // Gold to Orange
        is Emotion.Sad -> Color(0xFF00074a) to Color(0xFF00074a)   // DodgerBlue to DeepSkyBlue
        is Emotion.Angry -> Color(0xFF63000a) to Color(0xFF63000a) // OrangeRed to Crimson
        is Emotion.Calm -> Color(0xFF610063) to Color(0xFF610063)  // Aquamarine to MediumSpringGreen
        else -> Color(0xFFA9A9A9) to Color(0xFFA9A9A9)    // Gray to DarkGray
    }

    // Animate the colors
    val animatedStartColor by animateColorAsState(
        targetValue = startColor,
        animationSpec = tween(durationMillis = 1500), label = "" // 500ms transition
    )
    val animatedEndColor by animateColorAsState(
        targetValue = endColor,
        animationSpec = tween(durationMillis = 1500), label = ""
    )

    if (recommendedMediaItems.isEmpty()) {
        LaunchedEffect(emotionString) {
            audioViewModel.suggestMediaByEmotion(emotionString)
        }
    }

    // watch showWarningDialog to play/pause warning sound
    LaunchedEffect(showWarningDialog) {
        if (showWarningDialog) {
            audioViewModel.onUiEvents(UIEvents.PlayPause)
            if (!mediaPlayer.isPlaying) {
                try {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                    }
                } catch (e: IllegalStateException) {
                    mediaPlayer.reset()
                    MediaPlayer.create(context, R.raw.beep).apply {
                        isLooping = true
                        start()
                    }
                }
            }
        } else {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                mediaPlayer.seekTo(0) // Đặt lại vị trí phát
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }
    if (authState.value is AuthState.Authenticated) audioViewModel.loadFavorites((authState.value as AuthState.Authenticated).userId)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray.copy(alpha = 0.2f),
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .width(200.dp)
                            .height(90.dp),
                        contentScale = ContentScale.FillWidth
                    )
                },
                actions = {
                    IconButton(onClick = {
                        // navigate to search input screen
                        navController.navigate("search_input")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = {
                        // show account dialog
                        showAccountDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.PersonPin,
                            contentDescription = "Account"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to animatedStartColor,  // Top edge kicks off with start color
                                0.2f to animatedEndColor,    // At 20%, we hit the end color
                                1.0f to Color.Black          // Bottom fades to black
                            ),
                        ),
                        alpha = 0.2f
                    )
                }
                .padding(it)
                .padding(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(550.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CardContainer(
                        modifier = Modifier
                            .weight(1f)
                            .clipToBounds(),
                        color = animatedStartColor
                    ) {
                        if (fetchingState is FetchingState.Loading) {
                            LoadingIndicator(modifier = Modifier.fillMaxSize())
                        } else
                            HorizontalPagedMediaList(
                                modifier = Modifier.fillMaxSize(),
                                mediaItems = recommendedMediaItems,
                                onItemClick = { media, index ->
                                    audioViewModel.apply {
                                        setQueueItems(recommendedMediaItems)
                                        onUiEvents(UIEvents.SelectedAudioChange(index))
                                    }
                                },
                                onQueueButtonClick = { media, index ->
                                    audioViewModel.addToEndOfQueue(media)
                                    Toast.makeText(
                                        navController.context,
                                        "Added to queue",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                title = "Recommended for your mood"
                            )
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    CardContainer(
                        modifier = Modifier
                            .weight(1f)
                            .clipToBounds(),
                        color = animatedStartColor
                    ) {
                        MediaControl(
                            modifier = Modifier
                                .weight(1f),
                            exoPlayer = { audioViewModel.getExoPlayer() },
                            totalDuration = audioViewModel.duration,
                            isPlaying = { audioViewModel.isPlaying },
                            onReplayClick = { audioViewModel.onUiEvents(UIEvents.Backward) },
                            onPauseClick = { audioViewModel.onUiEvents(UIEvents.PlayPause) },
                            onForwardClick = { audioViewModel.onUiEvents(UIEvents.Forward) },
                            progress = { audioViewModel.progress },
                            onSeek = { audioViewModel.onUiEvents(UIEvents.SeekTo(it)) },
                            onFullScreenClick = {
                                navController.navigate("now_playing")
                            },
                            isFavorited = favoritesMediaItems.contains(audioViewModel.currentSelectedAudio),
                            onFavoriteClick = { isFavorited ->
                                val userId = when (authState.value) {
                                    is AuthState.Authenticated -> (authState.value as AuthState.Authenticated).userId
                                    else -> null
                                }
                                if(userId == null) return@MediaControl
                                if (isFavorited) {
                                    audioViewModel.removeFromFavorites(
                                        userId = userId,
                                        mediaId = audioViewModel.currentSelectedAudio.id,
                                        onAuthError = {
                                            Toast.makeText(
                                                context,
                                                "Please log in first",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                } else {
                                    Log.d(
                                        "Add to Favorites",
                                        "HomeScreen: mediaId = ${audioViewModel.currentSelectedAudio.id}"
                                    )
                                    audioViewModel.addToFavorites(
                                        userId = userId,
                                        mediaId = audioViewModel.currentSelectedAudio.id,
                                        onAuthError = {
                                            Toast.makeText(
                                                context,
                                                "Please log in first",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }

                        )
                    }


                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {

                        CardContainer(
                            modifier = Modifier
                                .weight(1f)
                                .clipToBounds(),
                            color = animatedStartColor
                        ) {
                            MoodDetection(
                                modifier = Modifier.weight(1f), emotion = { emotion },
                                onPlayPlaylist = {
                                    audioViewModel.suggestMediaByEmotion(
                                        emotionString
                                    )
                                },
                                isFetching = fetchingState is FetchingState.Loading
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        CardContainer(
                            modifier = Modifier
                                .weight(1f)
                                .clipToBounds(),
                            color = animatedStartColor
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            ) {
                                SleepinessDetection(
                                    modifier = Modifier.weight(1.5f),
                                    emotion = { emotion },
                                    warningCount = { warningCount }
                                ) {
                                    faceEmotionViewModel.onYawnDetected()
                                }
                                FaceMeshDetectionView(
                                    modifier = Modifier
                                        .weight(1f),
                                    faceEmotionViewModel.faceMeshes,
                                )
                            }
                        }
                    }
                }

            }
            item {
                HorizontalMediaList(
                    modifier = Modifier.height(300.dp),
                    mediaItems = favoritesMediaItems,
                    onItemClick = { media, index ->
                        audioViewModel.apply {
                            setQueueItems(favoritesMediaItems)
                            onUiEvents(UIEvents.SelectedAudioChange(index))
                        }
                    },
                    title = "Favorites"
                )
            }

        }

        // Show account dialog when triggered
        if (showAccountDialog) {
            AccountDialog(
                authState = authState.value,
                onDismiss = { showAccountDialog = false },
                onLogOut = {
                    audioViewModel.onSignedOut()
                    authViewModel.signOut()
                    showAccountDialog = false
                },
                onLogIn = {
                    navController.navigate("login")
                    showAccountDialog = false
                },
                onSignUp = {
                    navController.navigate("signup")
                    showAccountDialog = false
                }
            )
        }

        // Hiển thị dialog cảnh báo
        if (showWarningDialog) {
            mediaPlayer.start()
            AlertDialog(
                onDismissRequest = {
                    mediaPlayer.stop()
                },
                title = { Text("Danger warning") },
                text = { Text("You are too sleepy, please pull up") },
                confirmButton = {
                    Button(onClick = {
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = null
            )
        }
    }
}

