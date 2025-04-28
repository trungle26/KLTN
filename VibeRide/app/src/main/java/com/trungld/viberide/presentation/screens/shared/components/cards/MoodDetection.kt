package com.trungld.viberide.presentation.screens.shared.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungld.viberide.R
import com.trungld.viberide.presentation.theme.Typography
import com.trungld.viberide.presentation.theme.VibeRideTheme
import com.trungld.viberide.presentation.viewmodels.Emotion
import com.trungld.viberide.presentation.viewmodels.EmotionResult

@Composable
fun MoodDetection(
    modifier: Modifier = Modifier,
    emotion: () -> EmotionResult,
    isFetching: Boolean = false,
    onPlayPlaylist: (String) -> Unit = {} // Callback for playlist action
) {
    val moodImageId = when (emotion().dominantEmotion) {
        is Emotion.Happy -> R.drawable.happy
        is Emotion.Sad -> R.drawable.sad
        is Emotion.Angry -> R.drawable.angry
        is Emotion.Calm -> R.drawable.calm
        is Emotion.Unrecognized -> R.drawable.question_mark
    }

    // Mood string for display
    val moodString = when (emotion().dominantEmotion) {
        is Emotion.Happy -> "Happy"
        is Emotion.Sad -> "Sad"
        is Emotion.Angry -> "Angry"
        is Emotion.Calm -> "Calm"
        is Emotion.Unrecognized -> "Unrecognized"
    }

    // Tailored message
    val message = when (emotion().dominantEmotion) {
        is Emotion.Happy -> "Enjoy the ride with happy tunes!"
        is Emotion.Sad -> "Cheer up with uplifting music!"
        is Emotion.Angry -> "Cool off with calming vibes."
        is Emotion.Calm -> "Stay chill with soothing sounds."
        is Emotion.Unrecognized -> "Cannot recognize your emotion."
    }

    Row(
        modifier = modifier.padding(start = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon + Mood Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = "Mood: $moodString",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Message
            Text(
                text = message,
                fontSize = 24.sp,
                color = Color.White,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Play Button (skip for Unrecognized)
            if (emotion().dominantEmotion !is Emotion.Unrecognized && !isFetching) {
                FilledTonalButton (
                    onClick = { onPlayPlaylist(moodString) },
                ) {
                    Text("Get $moodString Playlist")
                }
            }
        }

        Image(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            painter = painterResource(id = moodImageId),
            contentDescription = "Mood image",
            contentScale = ContentScale.FillHeight,
            alignment = Alignment.CenterStart
        )

    }

}

@Preview
@Composable
private fun MoodDetectionCardPreview() {
    VibeRideTheme {
        Box(
            modifier = Modifier
                .width(500.dp)
                .height(300.dp)
                .background(Color.DarkGray)
        ) {
            MoodDetection(
                modifier = Modifier.fillMaxSize(),
                emotion = {
                    EmotionResult(
                        dominantEmotion = Emotion.Happy,
                        isSleepy = false,
                        sleepinessScore = 0.0f,
                        emotionIntensity = 1f,
                        allScores = emptyMap<Emotion, Float>()
                    )
                }
            )

        }

    }
}