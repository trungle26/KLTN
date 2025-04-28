package com.trungld.viberide.presentation.screens.shared.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.trungld.viberide.presentation.screens.shared.components.Media3PlayerView
import com.trungld.viberide.presentation.screens.shared.components.PlayerControls
import com.trungld.viberide.presentation.screens.shared.components.SeekBarControl
import com.trungld.viberide.presentation.theme.Typography
import com.trungld.viberide.presentation.theme.VibeRideTheme

@Composable
fun MediaControl(
    modifier: Modifier = Modifier,
    exoPlayer: () -> ExoPlayer?,
    totalDuration: Long = 0,
    isPlaying: () -> Boolean,
    onReplayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onForwardClick: () -> Unit,
    progress: () -> Float,
    onSeek: (Float) -> Unit,
    onFullScreenClick: () -> Unit,
    isFavorited: Boolean = false,
    onFavoriteClick: (Boolean) -> Unit = {}
) {

    Box(
        modifier = modifier
            .clickable(onClick = onFullScreenClick)
    ) {
        // Control overlay
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Player view
            Media3PlayerView(
                player = exoPlayer,
                modifier = Modifier.weight(2f)
            )
            Text(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(10.dp),
                text = exoPlayer()?.currentMediaItem?.mediaMetadata?.title?.toString()?:"No media is playing",
                maxLines = 1,
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(10.dp),
                text = exoPlayer()?.currentMediaItem?.mediaMetadata?.title?.toString()?:"",
                style = Typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            SeekBarControl(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                totalDuration = totalDuration,
                progress = progress,
                onProgress = onSeek,
                isFavorited = isFavorited,
                onFavoriteClick = onFavoriteClick
            )
            PlayerControls(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                isPlaying = isPlaying,
                onReplayClick = onReplayClick,
                onPauseToggle = onPauseClick,
                onForwardClick = onForwardClick,
            )

        }

    }
}

@Preview
@Composable
private fun MediaControlCardPreview() {
    VibeRideTheme {
        MediaControl(
            modifier = Modifier
                .width(300.dp)
                .height(600.dp)
                .background(Color.Black),
            exoPlayer = { null },
            isPlaying = { true },
            onReplayClick = {},
            onPauseClick = {},
            onForwardClick = {},
            progress = { 0.5f },
            onSeek = {},
            onFullScreenClick = {},
        )
    }
}

