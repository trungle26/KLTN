package com.trungld.viberide.viewmodels

import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.facemesh.FaceMesh
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

@HiltViewModel
class FaceEmotionViewModel @Inject constructor() : ViewModel() {
    val unrecognizedEmotion = EmotionResult(
        Emotion.Unrecognized,
        0f,
        false,
        0f,
        emptyMap()
    )
    private val _currentEmotion = MutableStateFlow<EmotionResult>(unrecognizedEmotion)
    val currentEmotion: StateFlow<EmotionResult> get() = _currentEmotion

    private val _faceMeshes = MutableStateFlow<List<FaceMesh>>(emptyList())
    val faceMeshes: StateFlow<List<FaceMesh>> = _faceMeshes

    private val _yawnCount = MutableStateFlow<Int>(0)
    val yawnCount: StateFlow<Int> = _yawnCount

    private val emotionHistory = mutableListOf<EmotionResult>()
    private val historySize = 5 // Smooth over last 5 frames

    fun updateEmotionFromFaceMesh(faceMesh: FaceMesh?) {
        if (faceMesh == null) {
            _currentEmotion.value = unrecognizedEmotion
            emotionHistory.clear()
            return
        }

        val emotion = inferEmotionForMusicAndSleepiness(faceMesh)
        emotionHistory.add(emotion)
        if (emotionHistory.size > historySize) {
            emotionHistory.removeAt(0)
        }

        val mostCommonEmotion = emotionHistory.groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: emotion
        _currentEmotion.value = mostCommonEmotion
    }

    fun updateFaceMeshes(meshes: List<FaceMesh>) {
        _faceMeshes.value = meshes
    }

    fun onYawnDetected() {
        _yawnCount.value++
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
            abs(smileCoef) < 0.015f && innerEyebrowDistance > 0.15f
        ) {
            calmScore += 0.4f
        }

        // Sleepy detection (tuned for "O" shape)
        if (mouthHeight > 0.08f && mouthWidth > 0.25f && smileCoef < 0.3f) {
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

        // Determine dominant emotion
        val musicEmotionScores = mapOf(
            Emotion.Happy to happyScore,
            Emotion.Sad to sadScore,
            Emotion.Angry to angryScore,
            Emotion.Calm to calmScore
        )

        val dominantEmotion = musicEmotionScores.maxByOrNull { it.value }?.key ?: Emotion.Calm
        val emotionIntensity = musicEmotionScores[dominantEmotion] ?: 0f

        return EmotionResult(
            dominantEmotion = dominantEmotion,
            emotionIntensity = emotionIntensity,
            isSleepy = sleepyScore > 0.5f,
            sleepinessScore = sleepyScore,
            allScores = musicEmotionScores,
        )
    }

}

data class EmotionResult(
    val dominantEmotion: Emotion,
    val emotionIntensity: Float,
    val isSleepy: Boolean,
    val sleepinessScore: Float,
    val allScores: Map<Emotion, Float>,
)

sealed class Emotion {
    object Happy : Emotion()
    object Sad : Emotion()
    object Angry : Emotion()
    object Calm : Emotion()
    object Unrecognized : Emotion()
}