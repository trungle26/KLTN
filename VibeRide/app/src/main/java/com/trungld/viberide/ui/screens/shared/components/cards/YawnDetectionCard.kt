package com.trungld.viberide.ui.screens.shared.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungld.viberide.ui.theme.Typography
import com.trungld.viberide.viewmodels.EmotionResult

@Composable
fun YawnDetectionCard(
    modifier: Modifier = Modifier,
    emotion: EmotionResult
) {
    // State for yawn count (for demo; replace with real tracking if available)
    var yawnCount = remember { mutableIntStateOf(0) }

    // Determine sleepiness alert
    val isSleepy = emotion.isSleepy || emotion.sleepinessScore > 0.5f // Threshold adjustable
    val dangerLevel = (emotion.sleepinessScore * 100).toInt().coerceIn(0, 100) // Convert to %

    // Update yawn count if sleepy (simulated; replace with real logic)
    LaunchedEffect(emotion.isSleepy) {
        if (emotion.isSleepy) {
            yawnCount.value += 1
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f), // Start with semi-transparent black
                            Color.Black // End with solid black
                        )
                    )
                )
                .padding(20.dp) // Apply padding to the content inside the card
        ){
            Column(
                modifier = Modifier.padding(8.dp),
            ) {
                Text(
                    text = "Yawn Detection",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (isSleepy) {
                    Text(
                        text = "WARNING: You seem sleepy! Take a rest for safe driving.",
                        fontSize = 14.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "All good! Stay alert on the road.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Danger Level: $dangerLevel%",
                    fontSize = 14.sp,
                    color = if (isSleepy) Color.Red else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Yawns Detected: $yawnCount",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}