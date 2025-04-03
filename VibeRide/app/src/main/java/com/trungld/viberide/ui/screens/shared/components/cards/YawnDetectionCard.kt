package com.trungld.viberide.ui.screens.shared.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungld.viberide.ui.theme.Typography
import com.trungld.viberide.ui.theme.VibeRideTheme
import com.trungld.viberide.viewmodels.Emotion
import com.trungld.viberide.viewmodels.EmotionResult

@Composable
fun YawnDetectionCard(
    modifier: Modifier = Modifier,
    emotion: EmotionResult,
    yawnCount: Int,
    onYawnDetected: () -> Unit
) {

    // Determine sleepiness alert
    val isSleepy = emotion.isSleepy || emotion.sleepinessScore > 0.5f // Threshold adjustable
    val dangerLevel = (emotion.sleepinessScore * 100).toInt().coerceIn(0, 100) // Convert to %

    // Update yawn count if sleepy (simulated; replace with real logic)
    LaunchedEffect(emotion.isSleepy) {
        if (emotion.isSleepy) {
            onYawnDetected.invoke()
        }
    }

    Column(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.padding(10.dp),
            text = "Yawn Detection",
            style = Typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (isSleepy) {
            Text(
                text = "WARNING: You seem sleepy! Take a rest for safe driving.",
                fontSize = 24.sp,
                color = Color.Red,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = "All good! Stay alert on the road.",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$dangerLevel%",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSleepy) Color.Red else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Dangerous",
                    fontSize = 14.sp,
                    color = if (isSleepy) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$yawnCount",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Yawns Count",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

        }
    }
}

@Preview
@Composable
private fun YawnDetectionPreview() {
    VibeRideTheme {
        YawnDetectionCard(
            modifier = Modifier.size(200.dp),
            emotion = EmotionResult(
                dominantEmotion = Emotion.Happy,
                emotionIntensity = 1f,
                isSleepy = true,
                sleepinessScore = 0.8f,
                allScores = mapOf()
            ),
            yawnCount = 10,
            onYawnDetected = {}
        )
    }
}

