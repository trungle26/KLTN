package com.trungld.viberide.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.facemesh.FaceMesh
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

@HiltViewModel
class FaceEmotionViewModel @Inject constructor() : ViewModel() {
    private val _currentEmotion = MutableLiveData<String>("Neutral")
    val currentEmotion: LiveData<String> get() = _currentEmotion

    private val emotionHistory = mutableListOf<String>()
    private val historySize = 3 // Smooth over last 3 frames

    fun updateEmotionFromFaceMesh(faceMesh: FaceMesh?) {
        if (faceMesh == null) {
            _currentEmotion.value = "No face detected"
            emotionHistory.clear()
            return
        }

        val emotion = suggestMusic(faceMesh)
        emotionHistory.add(emotion)
        if (emotionHistory.size > historySize) {
            emotionHistory.removeAt(0)
        }

        val mostCommonEmotion = emotionHistory.groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: emotion
        _currentEmotion.value = mostCommonEmotion
    }

    private fun inferEmotionForMusicAndSleepiness(face: FaceMesh): EmotionResult {
        // Extract landmarks (unchanged)
        val leftMouth = face.allPoints[61].position
        val rightMouth = face.allPoints[291].position
        val mouthUpper = face.allPoints[13].position
        val mouthLower = face.allPoints[14].position
        val leftEyebrowInner = face.allPoints[285].position
        val rightEyebrowInner = face.allPoints[55].position
        val leftCheek = face.allPoints[234].position
        val rightCheek = face.allPoints[454].position
        val forehead = face.allPoints[10].position
        val chin = face.allPoints[152].position
        val leftEyeUpper1 = face.allPoints[159].position
        val leftEyeLower1 = face.allPoints[145].position
        val rightEyeUpper1 = face.allPoints[386].position
        val rightEyeLower1 = face.allPoints[374].position

        // Normalization (unchanged)
        val faceWidth = abs(leftCheek.x - rightCheek.x)
        val faceHeight = abs(forehead.y - chin.y)
        val mouthWidth = abs(leftMouth.x - rightMouth.x) / faceWidth
        val mouthHeight = abs(mouthUpper.y - mouthLower.y) / faceHeight
        val mouthCenterY = (mouthUpper.y + mouthLower.y) / 2f
        val normalizedMouthThreshold = 0.005f * faceHeight

        // Mouth features (unchanged)
        val leftSmileCoef = (mouthCenterY - leftMouth.y) / faceHeight
        val rightSmileCoef = (mouthCenterY - rightMouth.y) / faceHeight
        val smileCoef = (leftSmileCoef + rightSmileCoef) / 2f
        val isFrowning = leftMouth.y > mouthCenterY && rightMouth.y > mouthCenterY
        val frownIntensity = if (isFrowning) {
            ((leftMouth.y - mouthCenterY) + (rightMouth.y - mouthCenterY)) / (2 * faceHeight)
        } else 0f

        // Eye features (unchanged)
        val leftEyeOpenness = abs(leftEyeUpper1.y - leftEyeLower1.y) / faceHeight
        val rightEyeOpenness = abs(rightEyeUpper1.y - rightEyeLower1.y) / faceHeight
        val avgEyeOpenness = (leftEyeOpenness + rightEyeOpenness) / 2f

        // Eyebrow features (unchanged)
        val innerEyebrowDistance = abs(leftEyebrowInner.x - rightEyebrowInner.x) / faceWidth

        // Emotion scores
        var happyScore = 0f
        var sadScore = 0f
        var angryScore = 0f
        var calmScore = 0.2f
        var sleepyScore = 0f

        // Happy detection (unchanged)
        if (smileCoef > 0.01f && mouthWidth > 0.35f) {
            happyScore = smileCoef * 6f + (mouthWidth - 0.35f) * 3f
        }

        // Sad detection (unchanged)
        if (isFrowning && frownIntensity > 0.005f) {
            sadScore += frownIntensity * 15f
        }
        if (innerEyebrowDistance < 0.15f) {
            sadScore += (0.15f - innerEyebrowDistance) * 20f
        }
        if (avgEyeOpenness < 0.04f) {
            sadScore += (0.04f - avgEyeOpenness) * 15f
        }

        // Angry detection (unchanged)
        if (innerEyebrowDistance < 0.15f) {
            angryScore += (0.15f - innerEyebrowDistance) * 15f
            if (avgEyeOpenness < 0.035f) {
                angryScore += (0.035f - avgEyeOpenness) * 20f
            }
            if (mouthWidth < 0.28f && mouthHeight < 0.04f) {
                angryScore += 1.0f
            }
        }

        // Calm detection (unchanged)
        if (avgEyeOpenness > 0.035f && avgEyeOpenness < 0.08f &&
            abs(smileCoef) < 0.015f && innerEyebrowDistance > 0.15f) {
            calmScore += 0.4f
        }

        // Sleepy detection (tuned for "O" shape)
        if (mouthHeight > 0.08f && mouthWidth > 0.25f && smileCoef < 0.005f) { // "O" shape: tall and wide, not a smile
            sleepyScore += mouthHeight * 12f // Boost for yawn height
            sleepyScore += (mouthWidth - 0.25f) * 8f // Add width contribution
        }
        if (avgEyeOpenness < 0.035f) {
            sleepyScore += (0.035f - avgEyeOpenness) * 20f
        }

        // Normalize scores (your tuned values)
        happyScore = min(0.7f, happyScore)
        sadScore = min(1.2f, sadScore)
        angryScore = min(1.2f, angryScore)
        calmScore = min(0.7f, calmScore)
        sleepyScore = min(1.2f, sleepyScore)

        // Debug info (unchanged)
        val debugInfo = mapOf(
            "smileCoef" to smileCoef,
            "frownIntensity" to frownIntensity,
            "innerEyebrowDistance" to innerEyebrowDistance,
            "avgEyeOpenness" to avgEyeOpenness,
            "mouthWidth" to mouthWidth,
            "mouthHeight" to mouthHeight
        )

        // Determine dominant emotion (unchanged)
        val musicEmotionScores = mapOf(
            "Happy" to happyScore,
            "Sad" to sadScore,
            "Angry" to angryScore,
            "Calm" to calmScore
        )

        val dominantEmotion = musicEmotionScores.maxByOrNull { it.value }?.key ?: "Calm"
        val emotionIntensity = musicEmotionScores[dominantEmotion] ?: 0f

        return EmotionResult(
            dominantEmotion = dominantEmotion,
            emotionIntensity = emotionIntensity,
            isSleepy = sleepyScore > 0.5f,
            sleepinessScore = sleepyScore,
            allScores = musicEmotionScores + ("Sleepy" to sleepyScore),
            debugInfo = debugInfo
        )
    }

    fun suggestMusic(face: FaceMesh): String {
        val emotionResult = inferEmotionForMusicAndSleepiness(face)
        if (emotionResult.sleepinessScore > 0.7f) {
            return "Suggesting: Gentle sleep music to help you rest"
        }

        val intensity = when {
            emotionResult.emotionIntensity > 0.8f -> "intense"
            emotionResult.emotionIntensity > 0.5f -> "moderate"
            else -> "mild"
        }

        return when (emotionResult.dominantEmotion) {
            "Happy" -> "Happy: $intensity upbeat, cheerful music"
            "Sad" -> "Sad: $intensity melancholic, reflective music"
            "Angry" -> "Angry: $intensity energetic, powerful music"
            "Calm" -> "Calm: Gentle ambient, relaxing music"
            else -> "Suggesting: Balanced, neutral music"
        }
    }

    fun testSadDetection(face: FaceMesh): String {
        val result = inferEmotionForMusicAndSleepiness(face)
        return """
            Sad Score: ${result.allScores["Sad"]}
            Angry Score: ${result.allScores["Angry"]}
            Happy Score: ${result.allScores["Happy"]}
            Calm Score: ${result.allScores["Calm"]}
            - Eyebrow Distance: ${result.debugInfo["innerEyebrowDistance"]}
            - Eye Openness: ${result.debugInfo["avgEyeOpenness"]}
            - Smile Coef: ${result.debugInfo["smileCoef"]}
            - Mouth Width: ${result.debugInfo["mouthWidth"]}
        """.trimIndent()
    }
}

data class EmotionResult(
    val dominantEmotion: String,
    val emotionIntensity: Float,
    val isSleepy: Boolean,
    val sleepinessScore: Float,
    val allScores: Map<String, Float>,
    val debugInfo: Map<String, Float> = emptyMap()
)