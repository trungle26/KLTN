package com.trungld.viberide.presentation.screens.shared.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungld.viberide.presentation.theme.VibeRideTheme
import java.util.concurrent.TimeUnit
import com.trungld.viberide.R

@Composable
fun SeekBarControl(
    modifier: Modifier = Modifier,
    totalDuration: Long,
    progress: () -> Float,
    onProgress: (Float) -> Unit,
    isFavorited: Boolean = false,
    onFavoriteClick: (Boolean) -> Unit = {}
) {

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // seek bar
            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                value = progress(),
                onValueChange = { onProgress(it) },
                valueRange = 0f..100f,
                colors =
                SliderDefaults.colors(
                    thumbColor = Color.LightGray,
                    activeTickColor = Color.Gray,
                    activeTrackColor = Color.Blue
                ),

            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text =  totalDuration.formatMinSec(),
                color = Color.LightGray
            )

            IconButton(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = {onFavoriteClick(isFavorited)}
            ) {
                Image(
                    contentScale = ContentScale.Crop,
                    painter = painterResource(id = if(isFavorited) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
                    contentDescription = "Enter/Exit fullscreen"
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun Long.formatMinSec(): String {
    return if (this <= 0L) {
        "00:00"
    } else {
        String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(this)
                    )
        )
    }
}

@Preview
@Composable
private fun SeekBarControlPreview() {
    VibeRideTheme {
        SeekBarControl(
            modifier = Modifier
                .height(200.dp)
                .width(300.dp),
            totalDuration =  10000L  ,
            progress = { 50f },
            onProgress = {}
        )
    }
}
