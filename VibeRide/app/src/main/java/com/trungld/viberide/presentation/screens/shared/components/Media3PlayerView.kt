package com.trungld.viberide.presentation.screens.shared.components

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView


@Composable
fun Media3PlayerView(
    player: () ->ExoPlayer?, // Passed from service, we won't modify its state
    modifier: Modifier = Modifier,
    useDefaultController: Boolean = false
) {
    val context = LocalContext.current

    // Reuse the PlayerView instance across recompositions
    val playerView = remember { PlayerView(context) }

    // Set up PlayerView properties only when they change
    DisposableEffect(player(), useDefaultController) {
        playerView.apply {
            this.player = player() // Attach the service's ExoPlayer
            this.useController = useDefaultController
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        onDispose {
            playerView.player = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { playerView }, // Return the remembered instance
        update = { view ->
            // Only update if the player or controller changes
            if (view.player !== player()) {
                view.player = player() // Use === for reference equality
            }
            if (view.useController != useDefaultController) {
                view.useController = useDefaultController
            }
        }
    )
}