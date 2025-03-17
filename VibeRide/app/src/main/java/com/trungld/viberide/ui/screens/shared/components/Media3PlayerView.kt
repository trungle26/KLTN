package com.trungld.viberide.ui.screens.shared.components

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView


@Composable
fun Media3PlayerView(
    player: ExoPlayer?,
    modifier: Modifier = Modifier,
    useDefaultController: Boolean = false
) {
    val playerViewRef = remember { mutableStateOf<PlayerView?>(null) }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = useDefaultController
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                playerViewRef.value = this
            }
        },
        update = { playerView ->
            playerView.player = player
            playerView.useController = useDefaultController
        }
    )

    // Cleanup when composable leaves composition
    DisposableEffect(player) {
        onDispose {
            playerViewRef.value?.player = null
        }
    }
}