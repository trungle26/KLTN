package com.trungld.viberide.presentation.screens.shared.components.cards

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
import com.trungld.viberide.presentation.theme.Typography
import com.trungld.viberide.presentation.theme.VibeRideTheme
import com.trungld.viberide.presentation.viewmodels.Emotion
import com.trungld.viberide.presentation.viewmodels.EmotionResult

@Composable
fun SleepinessDetection(
    modifier: Modifier = Modifier,
    emotion: () -> EmotionResult,
    warningCount: () -> Int,
    onSleepinessDetected: () -> Unit
) {

    // Determine sleepiness alert
    val isSleepy = emotion().isSleepy // Threshold adjustable
    val dangerLevel = (emotion().sleepinessScore * 100).toInt().coerceIn(0, 100) // Convert to %

    // Update yawn count if sleepy (simulated; replace with real logic)
    LaunchedEffect(emotion().isSleepy) {
        if (emotion().isSleepy) {
            onSleepinessDetected.invoke()
        }
    }

    Column(
        modifier = modifier.padding(start = 10.dp),
    ) {
        Text(
            modifier = Modifier.padding(bottom = 10.dp),
            text = "Sleepiness Detection",
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
                    text = "${warningCount()}",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Warning Count",
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
        SleepinessDetection(
            modifier = Modifier.size(250.dp),
            emotion = {
                EmotionResult(
                    dominantEmotion = Emotion.Happy,
                    emotionIntensity = 1f,
                    isSleepy = true,
                    sleepinessScore = 0.8f,
                    allScores = mapOf()
                )
            },
            warningCount = { 10 },
            onSleepinessDetected = {}
        )
    }
}

