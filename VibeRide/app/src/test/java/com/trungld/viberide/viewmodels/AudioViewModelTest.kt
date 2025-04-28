package com.trungld.viberide.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.media3.common.MediaItem
import com.trungld.viberide.data.repository.FakeMediaRepository
import com.trungld.viberide.data.service.AudioState
import com.trungld.viberide.data.service.PlayerEvent
import com.trungld.viberide.data.service.VibeRideAudioServiceHandler
import com.trungld.viberide.domain.entity.Media
import com.trungld.viberide.presentation.viewmodels.AudioViewModel
import com.trungld.viberide.presentation.viewmodels.FetchingState
import com.trungld.viberide.presentation.viewmodels.UIEvents
import com.trungld.viberide.presentation.viewmodels.UIState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.mockito.Mockito.*


@ExperimentalCoroutinesApi
class AudioViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var audioServiceHandler: VibeRideAudioServiceHandler
    private lateinit var savedStateHandle: SavedStateHandle
    private val testDispatcher = StandardTestDispatcher()
    private val audioStateFlow = MutableStateFlow<AudioState>(AudioState.Initial)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        audioServiceHandler = mock(VibeRideAudioServiceHandler::class.java)
        savedStateHandle = SavedStateHandle()

        // Mock the audioState to return our controlled StateFlow
        `when`(audioServiceHandler.audioState).thenReturn(audioStateFlow)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        audioStateFlow.value = AudioState.Initial
    }

    // Helper function to create fake media
    private fun createFakeMedia(
        id: String = "1",
        title: String = "Song",
        fileUrl: String = "url",
        type: String = "audio",
        thumbnailUrl: String = "",
        genre: List<String> = listOf("Genre"),
        artist: String = "Artist"
    ): Media {
        return Media(
            id = id,
            title = title,
            file_url = fileUrl,
            artist = artist,
            genre = genre,
            type = type,
            thumbnail_url = thumbnailUrl
        )
    }

    @Test
    fun `onUiEvents PlayPause calls onPlayerEvents`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)

        // Act
        viewModel.onUiEvents(UIEvents.PlayPause)
        advanceUntilIdle()

        // Assert
        verify(audioServiceHandler).onPlayerEvents(PlayerEvent.PlayPause)
    }

    @Test
    fun `onUiEvents SeekTo calls onPlayerEvents with correct position`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        viewModel.duration = 10000L

        // Act
        viewModel.onUiEvents(UIEvents.SeekTo(50f))
        advanceUntilIdle()

        // Assert
        verify(audioServiceHandler).onPlayerEvents(PlayerEvent.SeekTo, seekPosition = 5000L)
    }

    @Test
    fun `onUiEvents Forward calls onPlayerEvents`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)

        // Act
        viewModel.onUiEvents(UIEvents.Forward)
        advanceUntilIdle()

        // Assert
        verify(audioServiceHandler).onPlayerEvents(PlayerEvent.Forward)
    }

    @Test
    fun `onUiEvents Backward calls onPlayerEvents`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)

        // Act
        viewModel.onUiEvents(UIEvents.Backward)
        advanceUntilIdle()

        // Assert
        verify(audioServiceHandler).onPlayerEvents(PlayerEvent.Backward)
    }

    @Test
    fun `onUiEvents SeekToNext calls onPlayerEvents`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)

        // Act
        viewModel.onUiEvents(UIEvents.SeekToNext)
        advanceUntilIdle()

        // Assert
        verify(audioServiceHandler).onPlayerEvents(PlayerEvent.SeekToNext)
    }

    @Test
    fun `onUiEvents UpdateProgress calls onPlayerEvents and updates progress`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)

        // Act
        viewModel.onUiEvents(UIEvents.UpdateProgress(75f))
        advanceUntilIdle()

        // Assert
        verify(audioServiceHandler).onPlayerEvents(PlayerEvent.UpdateProgress(75f))
        assertEquals(75f, viewModel.progress)
    }


    @Test
    fun `suggestMediaByEmotion sets error state on failure`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        mediaRepository.shouldThrowErrorOnGetRecommendations = true

        // Act
        viewModel.suggestMediaByEmotion("Happy")
        advanceUntilIdle()

        // Assert
        val state = viewModel.fetchingState.value
        assertTrue(state is FetchingState.Error)
        assertEquals("Failed to fetch recommendations", (state as FetchingState.Error).message)
    }

    @Test
    fun `searchMedia updates searchResultsState on success`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        val mediaList = listOf(
            createFakeMedia(id = "1", title = "Happy Song"),
            createFakeMedia(id = "2", title = "Sad Song")
        )
        mediaRepository.addMediaToDatabase(mediaList)

        // Act
        viewModel.searchMedia("Happy")
        advanceUntilIdle()

        // Assert
        val expectedMedia = listOf(mediaList[0]) // Matches "Happy Song"
        assertEquals(FetchingState.Success(expectedMedia), viewModel.searchResultsState.value)
    }

    @Test
    fun `searchMedia sets error state on failure`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        mediaRepository.shouldThrowErrorOnSearch = true

        // Act
        viewModel.searchMedia("query")
        advanceUntilIdle()

        // Assert
        val state = viewModel.searchResultsState.value
        assertTrue(state is FetchingState.Error)
        assertEquals("Failed to search media", (state as FetchingState.Error).message)
    }

    @Test
    fun `loadFavorites updates favoritesMediaList`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        val mediaList = listOf(createFakeMedia(id = "1"), createFakeMedia(id = "2"))
        mediaRepository.addMediaToDatabase(mediaList)
        mediaRepository.addToFavorites("user1", "1")

        // Act
        viewModel.loadFavorites("user1")
        advanceUntilIdle()

        // Assert
        val expectedFavorites = listOf(mediaList[0])
        assertEquals(expectedFavorites, viewModel.favoritesMediaList.value)
    }

    @Test
    fun `loadFavorites handles empty favorites`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)

        // Act
        viewModel.loadFavorites("user1")
        advanceUntilIdle()

        // Assert
        assertEquals(emptyList<Media>(), viewModel.favoritesMediaList.value)
    }


    @Test
    fun `addToFavorites calls onAuthError when userId is null`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        var authErrorCalled = false
        val onAuthError = { authErrorCalled = true }

        // Act
        viewModel.addToFavorites(null, "media1", onAuthError)
        advanceUntilIdle()

        // Assert
        assertTrue(authErrorCalled)
    }

    @Test
    fun `addToFavorites adds media and refreshes favorites`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        val mediaList = listOf(createFakeMedia(id = "1"))
        mediaRepository.addMediaToDatabase(mediaList)

        // Act
        viewModel.addToFavorites("user1", "1") {}
        advanceUntilIdle()

        // Assert
        assertEquals(mediaList, viewModel.favoritesMediaList.value)
    }

    @Test
    fun `removeFromFavorites calls onAuthError when userId is null`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        var authErrorCalled = false
        val onAuthError = { authErrorCalled = true }

        // Act
        viewModel.removeFromFavorites(null, "media1", onAuthError)
        advanceUntilIdle()

        // Assert
        assertTrue(authErrorCalled)
    }

    @Test
    fun `removeFromFavorites removes media and refreshes favorites`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        val mediaList = listOf(createFakeMedia(id = "1"), createFakeMedia(id = "2"))
        mediaRepository.addMediaToDatabase(mediaList)
        mediaRepository.addToFavorites("user1", "1")
        mediaRepository.addToFavorites("user1", "2")
        viewModel.loadFavorites("user1")
        advanceUntilIdle()

        // Act
        viewModel.removeFromFavorites("user1", "1") {}
        advanceUntilIdle()

        // Assert
        val expectedFavorites = listOf(mediaList[1])
        assertEquals(expectedFavorites, viewModel.favoritesMediaList.value)
    }

    @Test
    fun `onSignedOut clears favoritesMediaList`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        val mediaList = listOf(createFakeMedia(id = "1"))
        mediaRepository.addMediaToDatabase(mediaList)
        mediaRepository.addToFavorites("user1", "1")
        viewModel.loadFavorites("user1")
        advanceUntilIdle()

        // Act
        viewModel.onSignedOut()
        advanceUntilIdle()

        // Assert
        assertEquals(emptyList<Media>(), viewModel.favoritesMediaList.value)
    }

    @Test
    fun `calculateProgressValue updates progress and progressString`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        viewModel.duration = 10000L

        // Act: Use reflection to call private method
        val method = AudioViewModel::class.java.getDeclaredMethod("calculateProgressValue", Long::class.java)
        method.isAccessible = true
        method.invoke(viewModel, 5000L)

        // Assert
        assertEquals(50f, viewModel.progress, 0.01f)
        assertEquals("00:05", viewModel.progressString)
    }

    @Test
    fun `formatDuration formats duration correctly`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)

        // Act: Use reflection to call private method
        val method = AudioViewModel::class.java.getDeclaredMethod("formatDuration", Long::class.java)
        method.isAccessible = true
        val result = method.invoke(viewModel, 125000L) as String

        // Assert
        assertEquals("02:05", result)
    }

//    @Test
//    fun `onCleared stops audio service`() = runTest {
//        // Arrange
//        val mediaRepository = FakeMediaRepository()
//        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
//
//        // Act
//        viewModel.onCleared()
//        advanceUntilIdle()
//
//        // Assert
//        verify(audioServiceHandler).onPlayerEvents(PlayerEvent.Stop)
//    }



    @Test
    fun `setQueueItems handles empty list`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)

        // Mock setMediaItemList to avoid Uri.parse
        `when`(audioServiceHandler.setMediaItemList(anyList())).then { /* Do nothing */ }

        // Act
        viewModel.setQueueItems(emptyList())
        advanceUntilIdle()

        // Assert
        assertEquals(emptyList<Media>(), viewModel.queueMediaList.value)
        verify(audioServiceHandler).setMediaItemList(emptyList())
    }

    @Test
    fun `createMediaMetadata sets correct metadata for audio type`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        val media = createFakeMedia(type = "audio")

        // Act: Use reflection to call private method
        val method = AudioViewModel::class.java.getDeclaredMethod("createMediaMetadata", Media::class.java)
        method.isAccessible = true
        val metadata = method.invoke(viewModel, media) as androidx.media3.common.MediaMetadata

        // Assert
        assertEquals(media.title, metadata.title.toString())
        assertEquals(media.artist, metadata.artist.toString())
        assertEquals(androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC, metadata.mediaType)
    }

    @Test
    fun `createMediaMetadata sets correct metadata for video type`() = runTest {
        // Arrange
        val mediaRepository = FakeMediaRepository()
        val viewModel = AudioViewModel(audioServiceHandler, mediaRepository, savedStateHandle, testDispatcher)
        val media = createFakeMedia(type = "video", thumbnailUrl = "thumbnail_url")
        val uriMock = mock(android.net.Uri::class.java)

        // Setup static mock for Uri.parse outside the coroutine scope
        val mockedUri = mockStatic(android.net.Uri::class.java)
        mockedUri.`when`<android.net.Uri> { android.net.Uri.parse(anyString()) }.thenReturn(uriMock)

        try {
            // Act: Use reflection to call private method
            val method = AudioViewModel::class.java.getDeclaredMethod("createMediaMetadata", Media::class.java)
            method.isAccessible = true
            val metadata = method.invoke(viewModel, media) as androidx.media3.common.MediaMetadata

            // Assert
            assertEquals(media.title, metadata.title.toString())
            assertEquals(androidx.media3.common.MediaMetadata.MEDIA_TYPE_VIDEO, metadata.mediaType)
            assertEquals(uriMock, metadata.artworkUri)
        } finally {
            // Clean up the static mock
            mockedUri.close()
        }
    }
}