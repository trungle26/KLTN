package com.trungld.viberide.presentation.screens.shared.components.cards

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungld.viberide.domain.entity.Media
import com.trungld.viberide.presentation.screens.shared.components.MediaItem
import com.trungld.viberide.presentation.theme.VibeRideTheme

@Composable
fun MediaList(
    mediaItems: List<Media>,
    modifier: Modifier = Modifier,
    onItemClick: (Media, Int) -> Unit,
    onQueueButtonClick: (Media, Int) -> Unit,
    title: String = "Media List",
    currentIndex: Int = -1,
    maxItems: Int = Int.MAX_VALUE,
    isQueue: Boolean = false
) {
    var maxVisibleItems by remember { mutableIntStateOf(maxItems) }
    Column(modifier = modifier.padding(18.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        val displayItems = if (maxVisibleItems < mediaItems.size) {
            mediaItems.take(maxVisibleItems)
        } else {
            mediaItems
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            itemsIndexed(displayItems) { index, media ->
                val isPlaying = index == currentIndex
                MediaItem(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    thumbnailUrl = media.thumbnail_url,
                    title = media.title,
                    artist = media.artist,
                    onItemClick = { onItemClick(media, index) },
                    onQueueButtonClick = { onQueueButtonClick(media, index) },
                    isInQueue = isQueue,
                    color = if (isPlaying) Color.DarkGray else Color.Transparent
                )
            }
            if(maxVisibleItems < maxItems)item{
                OutlinedButton(
                    modifier = Modifier
                        .width(200.dp)
                        .height(80.dp)
                        .padding(8.dp),
                    onClick = {
                        maxVisibleItems = if (maxVisibleItems == maxItems) Int.MAX_VALUE else maxItems
                    }
                ) {
                    Text(text = "See All")
                }
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
            listOf("Description 1"),
            "Description 1",
            "Description 1",
            "Description 1"
        ),
        Media(
            "1",
            "Title 1",
            "Title 1",
            listOf("Description 1"),
            "Description 1",
            "Description 1",
            "Description 1"
        ),
        Media(
            "1",
            "Title 1",
            "Title 1",
            listOf("Description 1"),
            "Description 1",
            "Description 1",
            "Description 1"
        ),
    )
    VibeRideTheme {
        MediaList(
            modifier = Modifier
                .width(400.dp)
                .height(500.dp),
            mediaItems = dummyMediaItems,
            onItemClick = { _, _ -> },
            title = "Media List",
            currentIndex = 1,
            maxItems = 1,
            onQueueButtonClick = { _, _ -> }
        )
    }

}
