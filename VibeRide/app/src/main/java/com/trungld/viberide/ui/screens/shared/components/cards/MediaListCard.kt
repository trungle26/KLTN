package com.trungld.viberide.ui.screens.shared.components.cards

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungld.viberide.data.entity.Media
import com.trungld.viberide.ui.screens.shared.components.MediaItem
import com.trungld.viberide.ui.theme.VibeRideTheme

@Composable
fun MediaListCard(
    mediaItems: List<Media>,
    modifier: Modifier = Modifier,
    onItemClick: (Int) -> Unit,
    title: String = "Media List",
    currentIndex: Int = -1
) {

    Column(
        modifier = modifier
    ) {
        Text(
            modifier = Modifier.padding(18.dp),
            text = title,
            fontSize = 30.sp,
            fontFamily = FontFamily.Serif,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            itemsIndexed(mediaItems) { index, media ->
                val isPlaying = index == currentIndex
                MediaItem(
                    media = media,
                    onItemClick = { onItemClick(index) },
                    color = if (isPlaying) Color.DarkGray else Color.Transparent,
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MediaListCardPreview() {
    val dummyMediaItems = listOf(
        Media(
            "1",
            "Title 1",
            "Title 1",
            "Description 1",
            "Description 1",
            "Description 1",
            "Description 1"
        ),
        Media(
            "1",
            "Title 1",
            "Title 1",
            "Description 1",
            "Description 1",
            "Description 1",
            "Description 1"
        ),
        Media(
            "1",
            "Title 1",
            "Title 1",
            "Description 1",
            "Description 1",
            "Description 1",
            "Description 1"
        ),
    )
    VibeRideTheme {
        MediaListCard(
            modifier = Modifier
                .width(400.dp)
                .height(500.dp),
            mediaItems = dummyMediaItems,
            onItemClick = {},
            title = "Media List",
            currentIndex = 1
        )
    }

}
