package com.trungld.viberide.ui.screens.home.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.media3.exoplayer.ExoPlayer
import com.trungld.viberide.ui.screens.shared.components.Media3PlayerView

@Composable
fun MediaControlCard(
    modifier: Modifier,
    exoPlayer: ExoPlayer
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
        ) {
            Media3PlayerView(exoPlayer)
        }
    }
}