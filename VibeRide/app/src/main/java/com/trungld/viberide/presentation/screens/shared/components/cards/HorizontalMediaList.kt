package com.trungld.viberide.presentation.screens.shared.components.cards

import android.R
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungld.viberide.domain.entity.Media
import com.trungld.viberide.presentation.screens.shared.components.SquareMediaItem
import com.trungld.viberide.presentation.theme.VibeRideTheme

@Composable
fun HorizontalMediaList(
    mediaItems: List<Media>,
    modifier: Modifier = Modifier,
    onItemClick: (Media, Int) -> Unit,
    title: String = "Media List",
) {
    Column(modifier = modifier.padding(18.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        if(mediaItems.isNotEmpty()){
            LazyRow (
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                itemsIndexed(mediaItems, key = { _, media -> media.id } ) { index, media ->
                    SquareMediaItem(
                        thumbnailUrl = { media.thumbnail_url },
                        title = { media.title },
                        artist = { media.artist },
                        onItemClick = { onItemClick(media, index) },
                        color = Color.Transparent
                    )
                }
            }
        }else{
            Text(text = "Your favorite list is empty", color = Color.White)
        }

    }
}

@Preview (showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun HorizontalMediaListPreview() {
    VibeRideTheme {
        HorizontalMediaList(
            mediaItems = listOf(),
            modifier = Modifier.size(200.dp),
            onItemClick = { _, _ -> },
        )
    }
}