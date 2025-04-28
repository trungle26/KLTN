package com.trungld.viberide.presentation.screens.shared.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungld.viberide.R
import com.trungld.viberide.presentation.theme.VibeRideTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isPlaying: () -> Boolean,
    onReplayClick: () -> Unit,
    onPauseToggle: () -> Unit,
    onForwardClick: () -> Unit
) {
    Row(
        modifier = modifier.background(Color.Transparent),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //replay button
        IconButton(modifier = Modifier.size(70.dp), onClick = onReplayClick) {
            Image(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillHeight,
                painter = painterResource(id = R.drawable.ic_replay),
                contentDescription = "Replay 5 seconds"
            )
        }

        //pause/play toggle button
        IconButton(
            modifier = Modifier
                .size(100.dp)
                .background(color = Color(0xff14161a), shape = CircleShape)
            ,
            onClick = onPauseToggle,
            shape = IconButtonDefaults.largeRoundShape,

        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillHeight,
                painter = painterResource(
                    id = if (isPlaying()) R.drawable.ic_pause else R.drawable.ic_play
                ),
                contentDescription = "Play/Pause"
            )
        }

        //forward button
        IconButton(modifier = Modifier.size(70.dp), onClick = onForwardClick) {
            Image(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillHeight,
                painter = painterResource(id = R.drawable.ic_forward_10),
                contentDescription = "Forward 10 seconds"
            )
        }
    }

}

@Preview
@Composable
private fun PlayerControlsPreview() {
    VibeRideTheme {
        PlayerControls(
            modifier = Modifier
                .width(400.dp)
                .height(100.dp),
            isPlaying = { false },
            onReplayClick = {},
            onPauseToggle = {},
            onForwardClick = {}
        )
    }
}
