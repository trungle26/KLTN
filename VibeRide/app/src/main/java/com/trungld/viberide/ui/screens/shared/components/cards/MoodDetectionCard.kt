package com.trungld.viberide.ui.screens.shared.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungld.viberide.viewmodels.Emotion
import com.trungld.viberide.viewmodels.EmotionResult

@Composable
fun MoodDetectionCard(
    modifier: Modifier = Modifier,
    emotion: EmotionResult,
    isFetching: Boolean = false,
    onPlayPlaylist: (String) -> Unit = {} // Callback for playlist action
) {
    // Map emotion to an icon (emojis for now, swap for icons later)
    val moodIcon = when (emotion.dominantEmotion) {
        is Emotion.Happy -> "😊"
        is Emotion.Sad -> "😢"
        is Emotion.Angry -> "😡"
        is Emotion.Calm -> "😌"
        is Emotion.Unrecognized -> "❓"
    }

    // Mood string for display
    val moodString = when (emotion.dominantEmotion) {
        is Emotion.Happy -> "Happy"
        is Emotion.Sad -> "Sad"
        is Emotion.Angry -> "Angry"
        is Emotion.Calm -> "Calm"
        is Emotion.Unrecognized -> "Unrecognized"
    }

    // Tailored message
    val message = when (emotion.dominantEmotion) {
        is Emotion.Happy -> "Enjoy the ride with happy tunes!"
        is Emotion.Sad -> "Cheer up with uplifting music!"
        is Emotion.Angry -> "Cool off with calming vibes."
        is Emotion.Calm -> "Stay chill with soothing sounds."
        is Emotion.Unrecognized -> "Hmm, let’s try that again."
    }

    // Background color (tweaked some for better vibes)
    val backgroundColor = when (emotion.dominantEmotion) {
        is Emotion.Happy -> Color(0xffc2a200) // Light yellow
        is Emotion.Sad -> Color(0xFF00074a) // Light blue
        is Emotion.Angry -> Color(0xFF63000a) // Light red
        is Emotion.Calm -> Color(0xFF610063) // Light green
        is Emotion.Unrecognized -> Color.LightGray
    }

    Card(
        modifier = modifier
            .fillMaxSize(),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor, // Start with semi-transparent black,
                            Color.Black // End with solid black
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon + Mood Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = moodIcon,
                        fontSize = 50.sp // Big icon for impact
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = moodString,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Message
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Play Button (skip for Unrecognized)
                if (emotion.dominantEmotion !is Emotion.Unrecognized && !isFetching) {
                    Button(
                        onClick = { onPlayPlaylist(moodString) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Get $moodString Playlist")
                    }
                }
            }
        }

    }
}

@Preview(showSystemUi = true)
@Composable
private fun MoodDetectionCardPreview() {
    MoodDetectionCard(
        modifier = Modifier
            .width(500.dp)
            .height(300.dp),
        emotion = EmotionResult(
            dominantEmotion = Emotion.Happy,
            isSleepy = false,
            sleepinessScore = 0.0f,
            emotionIntensity = 1f,
            allScores = emptyMap<Emotion, Float>()
        )
    )
}