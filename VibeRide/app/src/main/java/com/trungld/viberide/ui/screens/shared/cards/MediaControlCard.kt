package com.trungld.viberide.ui.screens.shared.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.trungld.viberide.ui.screens.shared.components.Media3PlayerView
import com.trungld.viberide.ui.theme.VibeRideTheme
import com.trungld.viberide.viewmodels.AudioViewModel

@Composable
fun MediaControlCard(modifier: Modifier = Modifier, audioViewModel: AudioViewModel = AudioViewModel()) {
    val thumbnail =
    // Center pane: Music Player
    Column(
        modifier = modifier
            .background(Color.LightGray, shape = MaterialTheme.shapes.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Media3PlayerView(videoUrl = "", audioViewModel = audioViewModel)

    }
}

@Preview
@Composable
private fun MediaControlCardPreview() {
    VibeRideTheme {
        MediaControlCard()

    }
}