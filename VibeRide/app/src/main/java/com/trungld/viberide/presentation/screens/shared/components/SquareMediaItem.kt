package com.trungld.viberide.presentation.screens.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.trungld.viberide.R
import com.trungld.viberide.presentation.theme.VibeRideTheme


@Composable
fun SquareMediaItem(
    modifier: Modifier = Modifier,
    thumbnailUrl: () -> String,
    title: () -> String,
    artist: () -> String,
    onItemClick: () -> Unit,
    color: Color = Color.Black
) {
    var imageWidth by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .padding(5.dp)
            .background(color)
            .clickable(enabled = true, onClick = onItemClick),
        horizontalAlignment = Alignment.Start
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl())
                .build(),
            contentDescription = "Media Thumbnail",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(150.dp) // Fixed height
                .clip(RoundedCornerShape(5.dp))
                .onGloballyPositioned { coordinates ->
                    imageWidth = coordinates.size.width
                },
            placeholder = painterResource(id = R.drawable.ic_launcher_background),
            error = painterResource(id = R.drawable.ic_launcher_foreground)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (imageWidth > 0) {
            val textWidth = with(LocalDensity.current) { imageWidth.toDp() }

            Column(modifier = Modifier.width(textWidth)) {
                Text(
                    text = title(),
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    fontSize = 19.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist(),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Preview
@Composable
private fun SquareMediaItemPreview() {
    VibeRideTheme {
        SquareMediaItem(
            modifier = Modifier
                .height(200.dp)
                .width(500.dp),
            thumbnailUrl = { "https://example.com/thumbnail.jpg" },
            title = { "Example Titleeeeeeeeeeeeeeeeeee" },
            artist = { "Example Artist" },
            onItemClick = {  }
        )
    }
}