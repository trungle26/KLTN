package com.trungld.viberide.ui.screens.shared.components.cards

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    currentIndex: Int = -1,
    maxItems: Int = Int.MAX_VALUE, // Default to show all items
) {
    var maxVisibleItems by remember { mutableIntStateOf(maxItems) }
    Column(modifier = modifier) {
        Text(
            text = title,
            modifier = Modifier.padding(18.dp),
            fontSize = 24.sp, // Adjusted to match design (20-24sp)
            fontFamily = FontFamily.SansSerif, // Modern sans-serif font
            fontWeight = FontWeight.Bold,
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
                    modifier = Modifier.height(100.dp).fillMaxWidth(),
                    thumbnailUrl = media.thumbnail_url,
                    title = media.title,
                    artist = media.artist,
                    onItemClick = { onItemClick(index) },
                    color = if (isPlaying) Color.DarkGray else Color.Transparent
                )
            }
            item{
                OutlinedButton(
                    modifier = Modifier
                        .width(200.dp)
                        .height(80.dp)
                        .padding(8.dp),
                    onClick = {
                        maxVisibleItems = if (maxVisibleItems == maxItems) Int.MAX_VALUE else maxItems
                    }
                ) {
                    Text(text = if (maxVisibleItems == Int.MAX_VALUE) "Show Less" else "See All")
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
        MediaListCard(
            modifier = Modifier
                .width(400.dp)
                .height(500.dp),
            mediaItems = dummyMediaItems,
            onItemClick = {},
            title = "Media List",
            currentIndex = 1,
            maxItems = 1
        )
    }

}
