package com.trungld.viberide.ui.screens.shared.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.trungld.viberide.ui.screens.shared.components.SeekBarControl
import com.trungld.viberide.ui.screens.shared.components.Media3PlayerView
import com.trungld.viberide.ui.screens.shared.components.PlayerControls
import com.trungld.viberide.ui.theme.VibeRideTheme
import kotlin.math.max
import kotlin.math.min

@Composable
fun MediaControlCard(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer?,
    isPlaying: Boolean,
    onReplayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onForwardClick: () -> Unit,
    progress: Float,
    onSeek: (Float) -> Unit,
    onFullScreenClick: () -> Unit // Callback for fullscreen navigation
) {

    // Control overlay
    Column(
        modifier = modifier
            .clickable(onClick = onFullScreenClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player view
        Media3PlayerView(
            player = exoPlayer,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        SeekBarControl(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            totalDuration = { exoPlayer?.duration ?: 0 },
            currentTime = { exoPlayer?.currentPosition ?: 0 },
            progress = progress,
            onProgress = onSeek,
            bufferPercentage = { exoPlayer?.bufferedPercentage ?: 0 }
        )
        PlayerControls(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            isPlaying = isPlaying,
            onReplayClick = onReplayClick,
            onPauseToggle = onPauseClick,
            onForwardClick = onForwardClick
        )

    }
}

@Preview
@Composable
private fun MediaControlCardPreview() {
    VibeRideTheme {
        MediaControlCard(
            modifier = Modifier
                .width(300.dp)
                .height(600.dp),
            exoPlayer = null,
            isPlaying = true,
            onReplayClick = {},
            onPauseClick = {},
            onForwardClick = {},
            progress = 0f,
            onSeek = {},
            onFullScreenClick = {}
        )
    }
}

