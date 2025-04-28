package com.trungld.viberide.presentation.screens.search_results

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.trungld.viberide.presentation.screens.shared.components.cards.MediaList
import com.trungld.viberide.presentation.screens.shared.components.cards.TopResult
import com.trungld.viberide.presentation.viewmodels.AudioViewModel
import com.trungld.viberide.presentation.viewmodels.FetchingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    modifier: Modifier = Modifier,
    query: String,
    navController: NavController,
    audioViewModel: AudioViewModel
) {
    val searchResultsState = audioViewModel.searchResultsState.collectAsState()

    // Trigger search when the screen is composed or query changes
    LaunchedEffect(query) {
        audioViewModel.searchMedia(query)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search results",
                        color = Color.White // Ensure visibility on dark background
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
                    .padding(20.dp) // Apply padding to the content inside the card,
            ) {
                when (searchResultsState.value) {
                    is FetchingState.Success -> {
                        val mediaItems =
                            (searchResultsState.value as FetchingState.Success).mediaList
                        Log.d("Search Media", "SearchResultsScreen: ${mediaItems.size} items")

                        if (mediaItems.isEmpty()) {
                            Text("No results found", color = Color.White)
                        } else {
                            // Top Result: First video or first item
                            val topResult = mediaItems.firstOrNull()
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (topResult != null) {
                                        Text(
                                            text = "Top Results", // "Kết quả hàng đầu" for Vietnamese
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TopResult(
                                            modifier = Modifier.weight(1f),
                                            media = topResult,
                                            onPlayClick = {
                                                audioViewModel.addToQueueAndPlay(topResult)
                                                navController.popBackStack()
                                                navController.navigate("now_playing")
                                            },
                                            onAddClick = {
                                                audioViewModel.addToEndOfQueue(topResult)
                                                Toast.makeText(
                                                    navController.context,
                                                    "Added to queue",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    if (mediaItems.isNotEmpty()) {
                                        MediaList(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            mediaItems = mediaItems,
                                            title = "Songs",
                                            maxItems = 3,
                                            onItemClick = {media, index ->
                                                audioViewModel.addToQueueAndPlay(media)
                                                navController.popBackStack()
                                                navController.navigate("now_playing")
                                            },
                                            onQueueButtonClick = { media, index ->
                                                audioViewModel.addToEndOfQueue(media)
                                                Toast.makeText(
                                                    navController.context,
                                                    "Added to queue",
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                        )

                                    }
                                }
                            }
                        }
                    }

                    is FetchingState.Error -> {
                        Text(
                            "Error: ${(searchResultsState.value as FetchingState.Error).message}",
                            color = Color.Red
                        )
                        Log.d("Search Media", "SearchResultsScreen: error")
                    }

                    else -> {}
                }

            }
        }
    }
}