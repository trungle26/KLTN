package com.trungld.viberide.ui.screens.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MediaControlCard(modifier: Modifier = Modifier) {
    // Center pane: Music Player
    Column(
        modifier = modifier
            .background(Color.LightGray, shape = MaterialTheme.shapes.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//                Image(
//                    painter = painterResource(id = R.drawable.ic_album_art),
//                    contentDescription = "Album Art",
//                    modifier = Modifier.size(150.dp)
//                )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Santa Tell Me", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Ariana Grande", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            IconButton(onClick = { /*TODO*/ }) {
//                        Icon(painter = painterResource(id = R.drawable.ic_skip_previous), contentDescription = "Previous")
            }
            IconButton(onClick = { /*TODO*/ }) {
//                        Icon(painter = painterResource(id = R.drawable.ic_play), contentDescription = "Play")
            }
            IconButton(onClick = { /*TODO*/ }) {
//                        Icon(painter = painterResource(id = R.drawable.ic_skip_next), contentDescription = "Next")
            }
        }
    }
}