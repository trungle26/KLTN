package com.trungld.viberide.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trungld.viberide.ui.screens.shared.components.FaceMeshDetectionView
import com.trungld.viberide.ui.screens.shared.components.LoadingIndicator
import com.trungld.viberide.ui.screens.shared.components.cards.CardContainer
import com.trungld.viberide.ui.screens.shared.components.cards.MediaControlCard
import com.trungld.viberide.ui.screens.shared.components.cards.MediaListCard
import com.trungld.viberide.ui.screens.shared.components.cards.MoodDetectionCard
import com.trungld.viberide.ui.screens.shared.components.cards.YawnDetectionCard
import com.trungld.viberide.viewmodels.AudioViewModel
import com.trungld.viberide.viewmodels.AuthViewModel
import com.trungld.viberide.viewmodels.Emotion
import com.trungld.viberide.viewmodels.FaceEmotionViewModel
import com.trungld.viberide.viewmodels.FetchingState
import com.trungld.viberide.viewmodels.UIEvents
import com.trungld.viberide.R

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
                        modifier = Modifier.width(200.dp).height(90.dp),
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
                    }) {
                        Icon(
                            imageVector = Icons.Filled.PersonPin,
                            contentDescription = "Account"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
//            TopAppBar(
//                authState = authState.value,
//                signOut = {
//                    try {
//                        authViewModel.signOut()
//                    } catch (e: Exception) {
//                        Toast.makeText(
//                            context,
//                            "Error logging out: ${e.message}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                },
//                logIn = {
//                    navController.navigate("login")
//                },
//                signUp = {
//                    navController.navigate("signup")
//                },
//                onSearch = { query ->
//                    navController.navigate("search_results/$query")
//                }
//            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to animatedStartColor,  // Top edge kicks off with start color
                            0.2f to animatedEndColor,    // At 20%, we hit the end color
                            1.0f to Color.Black          // Bottom fades to black
                        ),
                    ),
                    alpha = 0.2f
                )
                .padding(it)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
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
                        MediaListCard(
                            modifier = Modifier.fillMaxSize(),
                            mediaItems = recommendedMediaItems,
                            maxItems = Int.MAX_VALUE,
                            onItemClick = { media, index ->
                                audioViewModel.apply {
                                    updateMediaItems()
                                    onUiEvents(UIEvents.SelectedAudioChange(index))
                                }
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
                    MediaControlCard(
                        modifier = Modifier
                            .weight(1f),
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

                    CardContainer(
                        modifier = Modifier
                            .weight(1f)
                            .clipToBounds(),
                        color = animatedStartColor
                    ) {
                        MoodDetectionCard(
                            modifier = Modifier.weight(1f), emotion = emotion,
                            onPlayPlaylist = { audioViewModel.suggestMediaByEmotion(emotionString) },
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
                            modifier = Modifier.fillMaxSize()
                        ) {
                            YawnDetectionCard(
                                modifier = Modifier.weight(1f),
                                emotion = emotion,
                                yawnCount = yawnCount
                            ) {
                                faceEmotionViewModel.onYawnDetected()
                            }
                            FaceMeshDetectionView(
                                modifier = Modifier
                                    .weight(1f),
                                faceMeshes,
                            )
                        }
                    }
                }
            }

        }

    }
}

