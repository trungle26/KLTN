package com.trungld.viberide.ui.screens.home.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungld.viberide.data.entity.Media
import com.trungld.viberide.ui.screens.shared.components.MediaItem

@Composable
fun RecommendationCard(
    mediaItems: List<Media>,
    modifier: Modifier = Modifier,
    onItemClick: (Int) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxSize(),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(
                    "Recommended for your mood",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    itemsIndexed(mediaItems) { index, media ->
                        MediaItem(
                            media = media,
                            onItemClick = { onItemClick(index) }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun RecommendationCardPreview() {
    RecommendationCard(
        mediaItems = emptyList(),
        modifier = TODO(),
        onItemClick = TODO()
    )
}