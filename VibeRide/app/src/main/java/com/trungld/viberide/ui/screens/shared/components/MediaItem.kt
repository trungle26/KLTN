package com.trungld.viberide.ui.screens.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.trungld.viberide.R
import com.trungld.viberide.ui.theme.VibeRideTheme


@Composable
fun MediaItem(
    modifier: Modifier = Modifier,
    thumbnailUrl: String,
    title: String,
    artist: String,
    onItemClick: () -> Unit,
    color: Color = Color.Black
) {
    Row(
        modifier = modifier
            .padding(5.dp)
            .background(color)
            .clickable(enabled = true, onClick = onItemClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .build(),
            contentDescription = "Media Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(5.dp)),
            placeholder = painterResource(id = R.drawable.ic_launcher_background), // Add a placeholder
            error = painterResource(id = R.drawable.ic_launcher_foreground) // Add an error image
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White
                ),
                fontSize = 19.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
private fun MediaItemPreview() {
    VibeRideTheme {
        MediaItem(
            modifier = Modifier
                .size(500.dp),
            thumbnailUrl = "https://example.com/thumbnail.jpg",
            title = "Example Title",
            artist = "Example Artist",
            onItemClick = {  }
        )
    }
}