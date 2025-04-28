package com.trungld.viberide.presentation.screens.shared.components.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungld.viberide.domain.entity.Media
import com.trungld.viberide.presentation.screens.shared.components.MediaItem

@OptIn(ExperimentalFoundationApi::class) // Opt-in for Pager API
@Composable
fun HorizontalPagedMediaList(
    mediaItems: List<Media>,
    modifier: Modifier = Modifier,
    onItemClick: (Media, Int) -> Unit, // Int is the absolute index in the original list
    onQueueButtonClick: (Media, Int) -> Unit, // Int is the absolute index in the original list
    title: String = "Media List",
    itemsPerPage: Int = 5 // Number of items to show per page
) {
    // 1. Chunk the media items into pages
    val chunkedMediaItems = mediaItems.chunked(itemsPerPage)
    val pageCount = chunkedMediaItems.size

    if (pageCount == 0) {
        // Optionally handle the case with no items
        // You might want to show nothing or a placeholder
        return
    }

    // 2. Create PagerState
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column(modifier = modifier.padding(vertical = 18.dp)) { // Keep vertical padding for the whole section
        // Title - Placed outside the Pager, horizontal padding applied here
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 14.dp) // Horizontal padding for title
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3. Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(), // Let the pager wrap the height of the column inside
            pageSpacing = 8.dp,
            // Add horizontal content padding if you want items inside the page
            // to not touch the screen edges when the page is centered.
            contentPadding = PaddingValues(horizontal = 28.dp),
            verticalAlignment = Alignment.Top,
        ) { pageIndex ->
            // 4. Content for each page (A Column of MediaItems)
            Column(
                modifier = Modifier
                    .fillMaxWidth() // Fill the width allocated by the Pager page
                    .wrapContentHeight()
            ) {
                val pageItems = chunkedMediaItems[pageIndex]
                pageItems.forEachIndexed { indexInPage, media ->
                    // Calculate the absolute index in the original list
                    val absoluteIndex = pageIndex * itemsPerPage + indexInPage

                    // Use your existing MediaItem composable
                    MediaItem(
                        modifier = Modifier
                            .height(100.dp) // Keep the fixed height for each item
                            .fillMaxWidth(), // Fill the width of the inner Column
                        thumbnailUrl = media.thumbnail_url,
                        title = media.title,
                        artist = media.artist,
                        onItemClick = { onItemClick(media, absoluteIndex) },
                        onQueueButtonClick = {onQueueButtonClick(media, absoluteIndex)},
                        color = Color.Transparent,
                    )
                }
            }
        }
    }
}