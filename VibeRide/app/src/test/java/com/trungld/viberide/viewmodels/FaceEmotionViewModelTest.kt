package com.trungld.viberide.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.facemesh.FaceMesh
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.trungld.viberide.presentation.viewmodels.Emotion
import com.trungld.viberide.presentation.viewmodels.FaceEmotionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.mockito.Mockito.*
import java.lang.reflect.Field

@ExperimentalCoroutinesApi
class FaceEmotionViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FaceEmotionViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FaceEmotionViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Helper to create a fake FaceMesh with specified points
    private fun createFakeFaceMesh(points: Map<Int, Pair<Float, Float>>): FaceMesh {
        val faceMesh = mock(FaceMesh::class.java)
        // Create a list large enough to hold all required indices (up to 454)
        val maxIndex = points.keys.maxOrNull() ?: 0
        val faceMeshPoints = MutableList(maxIndex + 1) { index ->
            val position = points[index] ?: Pair(0f, 0f) // Default position if not specified
            val point = mock(FaceMeshPoint::class.java)
            val pointF3D = mock(PointF3D::class.java)
            `when`(pointF3D.x).thenReturn(position.first)
            `when`(pointF3D.y).thenReturn(position.second)
            `when`(pointF3D.z).thenReturn(0f)
            `when`(point.getPosition()).thenReturn(pointF3D)
            `when`(point.getIndex()).thenReturn(index)
            point
        }
        `when`(faceMesh.allPoints).thenReturn(faceMeshPoints)
        return faceMesh
    }

    @Test
    fun `inferEmotionForMusicAndSleepiness should detect Happy emotion with smile`() {
        // Arrange: Adjusted coordinates to ensure smileCoef > 0.01f and mouthWidth > 0.35f
        val faceMesh = createFakeFaceMesh(
            mapOf(
                61 to Pair(40f, 90f),    // leftMouth (raised for smile)
                291 to Pair(160f, 90f),  // rightMouth (raised for smile)
                13 to Pair(100f, 100f),  // mouthUpper
                14 to Pair(100f, 110f),  // mouthLower
                234 to Pair(0f, 100f),   // leftCheek
                454 to Pair(200f, 100f), // rightCheek
                10 to Pair(100f, 0f),    // forehead
                152 to Pair(100f, 200f), // chin
                159 to Pair(60f, 40f),   // leftEyeUpper1
                145 to Pair(60f, 50f),   // leftEyeLower1
                386 to Pair(140f, 40f),  // rightEyeUpper1
                374 to Pair(140f, 50f),  // rightEyeLower1
                285 to Pair(80f, 20f),   // leftEyebrowInner
                55 to Pair(120f, 20f)    // rightEyebrowInner
            )
        )

        // Act
        val result = viewModel.inferEmotionForMusicAndSleepiness(faceMesh)

        // Assert
        Assert.assertEquals(Emotion.Happy, result.dominantEmotion)
        Assert.assertTrue(result.emotionIntensity > 0f)
        Assert.assertFalse(result.isSleepy)
    }

    @Test
    fun `inferEmotionForMusicAndSleepiness should detect Sad emotion with frown`() {
        // Arrange: Adjusted coordinates to ensure isFrowning and innerEyebrowDistance < 0.15f
        val faceMesh = createFakeFaceMesh(
            mapOf(
                61 to Pair(50f, 120f),   // leftMouth (below mouthCenterY -> frowning)
                291 to Pair(150f, 120f), // rightMouth (below mouthCenterY -> frowning)
                13 to Pair(100f, 100f),  // mouthUpper
                14 to Pair(100f, 110f),  // mouthLower
                234 to Pair(0f, 100f),   // leftCheek
                454 to Pair(200f, 100f), // rightCheek
                10 to Pair(100f, 0f),    // forehead
                152 to Pair(100f, 200f), // chin
                159 to Pair(60f, 40f),   // leftEyeUpper1
                145 to Pair(60f, 45f),   // leftEyeLower1
                386 to Pair(140f, 40f),  // rightEyeUpper1
                374 to Pair(140f, 45f),  // rightEyeLower1
                285 to Pair(95f, 20f),   // leftEyebrowInner (closer to center)
                55 to Pair(105f, 20f)    // rightEyebrowInner (closer to center)
            )
        )

        // Act
        val result = viewModel.inferEmotionForMusicAndSleepiness(faceMesh)

        // Assert
        Assert.assertEquals(Emotion.Sad, result.dominantEmotion)
        Assert.assertTrue(result.emotionIntensity > 0f)
        Assert.assertFalse(result.isSleepy)
    }

    @Test
    fun `inferEmotionForMusicAndSleepiness should detect Angry emotion with furrowed brows and tight mouth`() {
        // Arrange: Adjusted coordinates to ensure innerEyebrowDistance < 0.15f, avgEyeOpenness < 0.035f, mouthWidth < 0.28f
        val faceMesh = createFakeFaceMesh(
            mapOf(
                61 to Pair(90f, 105f),   // leftMouth (narrow mouth)
                291 to Pair(110f, 105f), // rightMouth (narrow mouth)
                13 to Pair(100f, 100f),  // mouthUpper
                14 to Pair(100f, 102f),  // mouthLower (tight mouth)
                234 to Pair(0f, 100f),   // leftCheek
                454 to Pair(200f, 100f), // rightCheek
                10 to Pair(100f, 0f),    // forehead
                152 to Pair(100f, 200f), // chin
                159 to Pair(60f, 40f),   // leftEyeUpper1
                145 to Pair(60f, 41f),   // leftEyeLower1 (eyes narrowed)
                386 to Pair(140f, 40f),  // rightEyeUpper1
                374 to Pair(140f, 41f),  // rightEyeLower1 (eyes narrowed)
                285 to Pair(95f, 20f),   // leftEyebrowInner (closer to center)
                55 to Pair(105f, 20f)    // rightEyebrowInner (closer to center)
            )
        )

        // Act
        val result = viewModel.inferEmotionForMusicAndSleepiness(faceMesh)

        // Assert
        Assert.assertEquals(Emotion.Angry, result.dominantEmotion)
        Assert.assertTrue(result.emotionIntensity > 0f)
        Assert.assertFalse(result.isSleepy)
    }

    @Test
    fun `inferEmotionForMusicAndSleepiness should detect Calm emotion with neutral features`() {
        // Arrange: FaceMesh with neutral characteristics (Calm)
        val faceMesh = createFakeFaceMesh(
            mapOf(
                61 to Pair(50f, 100f),   // leftMouth
                291 to Pair(150f, 100f), // rightMouth (neutral mouth)
                13 to Pair(100f, 100f),  // mouthUpper
                14 to Pair(100f, 105f),  // mouthLower
                234 to Pair(0f, 100f),   // leftCheek
                454 to Pair(200f, 100f), // rightCheek
                10 to Pair(100f, 0f),    // forehead
                152 to Pair(100f, 200f), // chin
                159 to Pair(60f, 40f),   // leftEyeUpper1
                145 to Pair(60f, 50f),   // leftEyeLower1 (eyes moderately open)
                386 to Pair(140f, 40f),  // rightEyeUpper1
                374 to Pair(140f, 50f),  // rightEyeLower1 (eyes moderately open)
                285 to Pair(80f, 20f),   // leftEyebrowInner
                55 to Pair(120f, 20f)    // rightEyebrowInner (eyebrows relaxed)
            )
        )

        // Act
        val result = viewModel.inferEmotionForMusicAndSleepiness(faceMesh)

        // Assert
        Assert.assertEquals(Emotion.Calm, result.dominantEmotion)
        Assert.assertTrue(result.emotionIntensity > 0f)
        Assert.assertFalse(result.isSleepy)
    }

    @Test
    fun `inferEmotionForMusicAndSleepiness should detect Sleepy state with yawn and closed eyes`() {
        // Arrange: FaceMesh with characteristics of a yawn and closed eyes (Sleepy)
        val faceMesh = createFakeFaceMesh(
            mapOf(
                61 to Pair(50f, 110f),   // leftMouth
                291 to Pair(150f, 110f), // rightMouth (wide mouth)
                13 to Pair(100f, 90f),   // mouthUpper
                14 to Pair(100f, 130f),  // mouthLower (large mouth opening - yawn)
                234 to Pair(0f, 100f),   // leftCheek
                454 to Pair(200f, 100f), // rightCheek
                10 to Pair(100f, 0f),    // forehead
                152 to Pair(100f, 200f), // chin
                159 to Pair(60f, 40f),   // leftEyeUpper1
                145 to Pair(60f, 42f),   // leftEyeLower1 (eyes closed)
                386 to Pair(140f, 40f),  // rightEyeUpper1
                374 to Pair(140f, 42f),  // rightEyeLower1 (eyes closed)
                285 to Pair(80f, 20f),   // leftEyebrowInner
                55 to Pair(120f, 20f)    // rightEyebrowInner
            )
        )

        // Act
        val result = viewModel.inferEmotionForMusicAndSleepiness(faceMesh)

        // Assert
        Assert.assertTrue(result.isSleepy)
        Assert.assertTrue(result.sleepinessScore > 0.5f)
    }

    @Test
    fun `updateEmotionFromFaceMesh should set unrecognized emotion when faceMesh is null`() = testScope.runTest {
        // Act
        viewModel.updateEmotionFromFaceMesh(null)

        // Assert
        val emotion = viewModel.currentEmotion.first()
        Assert.assertEquals(Emotion.Unrecognized, emotion.dominantEmotion)
        Assert.assertFalse(emotion.isSleepy)
    }

    @Test
    fun `updateEmotionFromFaceMesh should show warning dialog after 5 seconds of sleepy state`() = testScope.runTest {
        // Arrange: FaceMesh with sleepy characteristics
        val sleepyFaceMesh = createFakeFaceMesh(
            mapOf(
                61 to Pair(50f, 110f),   // leftMouth
                291 to Pair(150f, 110f), // rightMouth (wide mouth)
                13 to Pair(100f, 90f),   // mouthUpper
                14 to Pair(100f, 130f),  // mouthLower (large mouth opening - yawn)
                234 to Pair(0f, 100f),   // leftCheek
                454 to Pair(200f, 100f), // rightCheek
                10 to Pair(100f, 0f),    // forehead
                152 to Pair(100f, 200f), // chin
                159 to Pair(60f, 40f),   // leftEyeUpper1
                145 to Pair(60f, 42f),   // leftEyeLower1 (eyes closed)
                386 to Pair(140f, 40f),  // rightEyeUpper1
                374 to Pair(140f, 42f),  // rightEyeLower1 (eyes closed)
                285 to Pair(80f, 20f),   // leftEyebrowInner
                55 to Pair(120f, 20f)    // rightEyebrowInner
            )
        )

        // Access sleepyStartTime via reflection to control the time difference
        val sleepyStartTimeField: Field = FaceEmotionViewModel::class.java.getDeclaredField("sleepyStartTime")
        sleepyStartTimeField.isAccessible = true

        // Act: First call (start sleepy state)
        viewModel.updateEmotionFromFaceMesh(sleepyFaceMesh)

        // Set sleepyStartTime to simulate 5 seconds ago
        sleepyStartTimeField.set(viewModel, System.currentTimeMillis() - 5001L)

        // Act: Second call (after 5 seconds)
        viewModel.updateEmotionFromFaceMesh(sleepyFaceMesh)

        // Assert: After 5 seconds, dialog should be shown
        Assert.assertTrue(viewModel.showWarningDialog.first())
    }

    @Test
    fun `updateEmotionFromFaceMesh should reset sleepy state when not sleepy`() = testScope.runTest {
        // Arrange: FaceMesh with non-sleepy characteristics (Calm)
        val calmFaceMesh = createFakeFaceMesh(
            mapOf(
                61 to Pair(50f, 100f),   // leftMouth
                291 to Pair(150f, 100f), // rightMouth (neutral mouth)
                13 to Pair(100f, 100f),  // mouthUpper
                14 to Pair(100f, 105f),  // mouthLower
                234 to Pair(0f, 100f),   // leftCheek
                454 to Pair(200f, 100f), // rightCheek
                10 to Pair(100f, 0f),    // forehead
                152 to Pair(100f, 200f), // chin
                159 to Pair(60f, 40f),   // leftEyeUpper1
                145 to Pair(60f, 50f),   // leftEyeLower1 (eyes moderately open)
                386 to Pair(140f, 40f),  // rightEyeUpper1
                374 to Pair(140f, 50f),  // rightEyeLower1 (eyes moderately open)
                285 to Pair(80f, 20f),   // leftEyebrowInner
                55 to Pair(120f, 20f)    // rightEyebrowInner (eyebrows relaxed)
            )
        )

        // Act
        viewModel.updateEmotionFromFaceMesh(calmFaceMesh)

        // Assert
        Assert.assertFalse(viewModel.showWarningDialog.first())
        Assert.assertFalse(viewModel.currentEmotion.first().isSleepy)
    }

    @Test
    fun `updateFaceMeshes should update faceMeshes state`() = testScope.runTest {
        // Arrange
        val faceMesh = createFakeFaceMesh(emptyMap())
        val meshList = listOf(faceMesh)

        // Act
        viewModel.updateFaceMeshes(meshList)

        // Assert
        val updatedMeshes = viewModel.faceMeshes.first()
        Assert.assertEquals(meshList, updatedMeshes)
    }

    @Test
    fun `onYawnDetected should increment warningCount`() = testScope.runTest {
        // Arrange: Initial warningCount is 0
        Assert.assertEquals(0, viewModel.warningCount.first())

        // Act
        viewModel.onYawnDetected()

        // Assert
        Assert.assertEquals(1, viewModel.warningCount.first())

        // Act: Call again to ensure increment
        viewModel.onYawnDetected()
        Assert.assertEquals(2, viewModel.warningCount.first())
    }
}
