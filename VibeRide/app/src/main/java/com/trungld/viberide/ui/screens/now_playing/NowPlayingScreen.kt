package com.trungld.viberide.ui.screens.now_playing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trungld.viberide.ui.screens.shared.components.Media3PlayerView
import com.trungld.viberide.ui.screens.shared.components.cards.MediaListCard
import com.trungld.viberide.viewmodels.AudioViewModel
import com.trungld.viberide.viewmodels.UIEvents
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    audioViewModel: AudioViewModel
) {
    val queueItems by audioViewModel.queueMediaList.collectAsState()
    val currentIndex by audioViewModel.currentMediaIndex.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Now Playing",
                        color = Color.White // Ensure visibility on dark background
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent // Material 3 way for transparent background
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f), // Start with semi-transparent black
                                Color.Black // End with solid black
                            )
                        )
                    )
                    .padding(20.dp) // Apply padding to the content inside the card
            ) {
                Row(
                    modifier = modifier.fillMaxSize()
                ) {
                    Media3PlayerView(
                        player = audioViewModel.audioServiceHandler.exoPlayer,
                        modifier = modifier.weight(3f),
                        useDefaultController = true
                    )
                    MediaListCard(
                        modifier = modifier.weight(1f),
                        mediaItems = queueItems,
                        onItemClick = { media, index ->
                            audioViewModel.onUiEvents(UIEvents.SelectedAudioChange(index))
                        },
                        title = "Next in queue",
                        currentIndex = currentIndex
                    )
                }
            }
        }
    }
}