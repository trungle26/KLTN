package com.trungld.viberide.ui.screens.search_results

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.trungld.viberide.ui.screens.shared.components.LoadingIndicator
import com.trungld.viberide.ui.screens.shared.components.cards.MediaListCard
import com.trungld.viberide.ui.screens.shared.components.cards.TopResultCard
import com.trungld.viberide.viewmodels.AudioViewModel
import com.trungld.viberide.viewmodels.FetchingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    modifier: Modifier = Modifier,
    query: String,
    navController: NavController,
    audioViewModel: AudioViewModel // Assume this provides searchResultsState
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
                    .padding(20.dp) // Apply padding to the content inside the card
            ) {
                when (searchResultsState.value) {
                    is FetchingState.Loading -> {
                        LoadingIndicator(modifier = Modifier.size(100.dp))
                    }

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
                                        TopResultCard(
                                            modifier = Modifier.weight(1f),
                                            media = topResult,
                                            onPlayClick = { /* TODO: Play media */ },
                                            onAddClick = { /* TODO: Add to playlist */ }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    if (mediaItems.isNotEmpty()) {
                                        MediaListCard(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            mediaItems = mediaItems,
                                            title = "Songs",
                                            maxItems = 3,
                                            onItemClick = { index -> /* TODO: Handle song click */ },
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

                    FetchingState.Initial -> {

                    }
                }

            }
        }
    }
}