package com.trungld.viberide.ui.screens.shared.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.trungld.viberide.data.entity.Media
import com.trungld.viberide.ui.theme.VibeRideTheme

@Composable
fun TopResultCard(
    modifier: Modifier = Modifier,
    media: Media,
    onPlayClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(media.thumbnail_url),
            contentDescription = null,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = media.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = media.artist,
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                text = when (media.type) {
                    "video" -> "Video • ${media.artist} • Views" // Placeholder
                    "music" -> "Song • Duration • Plays" // Placeholder
                    else -> ""
                },
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().height(80.dp)
                .padding(8.dp),
            onClick = onPlayClick
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
            )
            Text(text = "Play")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().height(80.dp)
                .padding(8.dp),
            onClick = onAddClick
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
            )
            Text(text = "Add to queue")
        }
    }
}

@Preview
@Composable
private fun TopResultPreview() {
    VibeRideTheme {
        TopResultCard(
            modifier = Modifier.wrapContentSize(),
            media = Media(
                id = "1",
                title = "Title 1",
                artist = "Artist 1",
                genre = listOf("Description 1"),
                thumbnail_url = "https://via.placeholder.com/80", // Ensure URL is valid
                type = "music"
            ),
            onPlayClick = {}, // Empty lambdas instead of TODO()
            onAddClick = {}
        )
    }
}
