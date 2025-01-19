package com.trungld.viberide.ui.screens.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun YawnDetectionCard(modifier: Modifier = Modifier) {
    // Yawn Detection Widget
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray, shape = MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        Text("Yawn Detection", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("You seem to be sleepy. Consider taking a rest.", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Dangerous: 38%", fontSize = 14.sp)
        Text("Yawns Count: 20", fontSize = 14.sp)
    }
}