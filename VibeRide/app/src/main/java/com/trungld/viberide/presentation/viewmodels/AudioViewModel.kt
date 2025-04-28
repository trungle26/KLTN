package com.trungld.viberide.presentation.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.trungld.viberide.data.service.AudioState
import com.trungld.viberide.data.service.PlayerEvent
import com.trungld.viberide.data.service.VibeRideAudioServiceHandler
import com.trungld.viberide.domain.entity.Media
import com.trungld.viberide.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioServiceHandler: VibeRideAudioServiceHandler,
    private val mediaRepository: MediaRepository,
    savedStateHandle: SavedStateHandle,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val mediaDummy = Media()

    @OptIn(SavedStateHandleSaveableApi::class)
    var duration by savedStateHandle.saveable { mutableLongStateOf(0L) }

    @OptIn(SavedStateHandleSaveableApi::class)
    var progress: Float by savedStateHandle.saveable { mutableFloatStateOf(0f) }

    @OptIn(SavedStateHandleSaveableApi::class)
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }

    @OptIn(SavedStateHandleSaveableApi::class)
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }

    @OptIn(SavedStateHandleSaveableApi::class)
    var currentSelectedAudio by savedStateHandle.saveable { mutableStateOf(mediaDummy) }

    private val _recommendedMediaList = MutableStateFlow<List<Media>>(emptyList()) // Backing state
    val recommendedMediaList: StateFlow<List<Media>> =
        _recommendedMediaList // Public read-only state

    private val _favoritesMediaList = MutableStateFlow<List<Media>>(emptyList())
    val favoritesMediaList: StateFlow<List<Media>> = _favoritesMediaList

    // State for search results
    private val _searchResultsState = MutableStateFlow<FetchingState>(FetchingState.Initial)
    val searchResultsState: StateFlow<FetchingState> = _searchResultsState

    // queue state
    private val _queueMediaList = MutableStateFlow<List<Media>>(emptyList())
    val queueMediaList: StateFlow<List<Media>> = _queueMediaList

    private val _currentMediaIndex = MutableStateFlow<Int>(-1)
    val currentMediaIndex: StateFlow<Int> = _currentMediaIndex

    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val _fetchingState = MutableStateFlow<FetchingState>(FetchingState.Initial)
    val fetchingState: StateFlow<FetchingState> = _fetchingState.asStateFlow()


    init {
        viewModelScope.launch {
            // Auto-play first item once when queue loads
            recommendedMediaList
                .filter { it.isNotEmpty() } // Wait for non-empty queue
                .take(1) // Only take the first emission
                .collect {
                    setQueueItems(it)
                    onUiEvents(UIEvents.SelectedAudioChange(0))
                }
        }
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest { mediaState ->
                when (mediaState) {
                    AudioState.Initial -> _uiState.value = UIState.Initial
                    is AudioState.Buffering -> {
                        calculateProgressValue(mediaState.progress)
                        _uiState.value = UIState.Buffering
                    }

                    is AudioState.Playing -> {
                        isPlaying = mediaState.isPlaying

                    }

                    is AudioState.Progress -> calculateProgressValue(mediaState.progress)
                    is AudioState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready
                    }
                }
            }
        }
    }

    fun getExoPlayer() = audioServiceHandler.exoPlayer

    fun onUiEvents(uiEvents: UIEvents) = viewModelScope.launch {
        when (uiEvents) {
            UIEvents.Backward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Backward)
            UIEvents.SeekToNext -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
            UIEvents.Forward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Forward)
            is UIEvents.PlayPause -> {
                audioServiceHandler.onPlayerEvents(PlayerEvent.PlayPause)
            }

            is UIEvents.SeekTo -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = ((uiEvents.position * duration) / 100f).toLong()
                )
            }

            is UIEvents.SelectedAudioChange -> {
                _currentMediaIndex.value = uiEvents.index
                currentSelectedAudio =
                    _recommendedMediaList.value[uiEvents.index]
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChange,
                    selectedAudioIndex = uiEvents.index
                )
                Log.d("SelectedAudioChange", "onUiEvents: index is ${uiEvents.index}")
            }

            is UIEvents.UpdateProgress -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.UpdateProgress(uiEvents.newProgress)
                )
                progress = uiEvents.newProgress
            }
        }
    }

    // Add media to queue at position next to current playing media
    fun addToQueueAndPlay(media: Media) {
        _currentMediaIndex.value++
        // add media to queue at current index
        _queueMediaList.value = _queueMediaList.value.subList(
            0,
            _currentMediaIndex.value
        ) + media + _queueMediaList.value.subList(
            _currentMediaIndex.value,
            _queueMediaList.value.size
        )
        // add media to audio service
        val exoMedia = MediaItem.Builder()
            .setUri(media.file_url)
            .setMediaMetadata(createMediaMetadata(media))
            .build()
        audioServiceHandler.addMediaItem(mediaItem = exoMedia, index = _currentMediaIndex.value)
        onUiEvents(UIEvents.SelectedAudioChange(_currentMediaIndex.value))
    }

    fun addToEndOfQueue(media: Media) {
        _queueMediaList.value = _queueMediaList.value + media
        val exoMedia = MediaItem.Builder()
            .setUri(media.file_url)
            .setMediaMetadata(createMediaMetadata(media))
            .build()
        audioServiceHandler.addMediaItem(exoMedia)
    }

    fun deleteFromQueue(index: Int) {
        _queueMediaList.value =
            _queueMediaList.value.subList(0, index) + _queueMediaList.value.subList(
                index + 1,
                _queueMediaList.value.size
            )
        audioServiceHandler.deleteMediaItem(index)
    }

    fun suggestMediaByEmotion(emotion: String) {
        viewModelScope.launch(dispatcher) {
            _fetchingState.value = FetchingState.Loading
            try {
                val recommended = mediaRepository.getRecommendationsByEmotion(emotion)
                _fetchingState.value = FetchingState.Success(recommended)
                _recommendedMediaList.value = recommended
                mediaRepository.cacheMedia(recommended)
                setQueueItems(_recommendedMediaList.value)
            } catch (e: Exception) {
                _fetchingState.value =
                    FetchingState.Error(e.message ?: "Failed to load recommendations for $emotion")
            }
        }
    }

    // Search media by name
    fun searchMedia(query: String) {
        viewModelScope.launch(dispatcher) {
            _searchResultsState.value = FetchingState.Loading
            try {
                val results = mediaRepository.searchMediaFromFirestore(query)
                _searchResultsState.value = FetchingState.Success(results)
//                Log.d("Search Media", "searchMedia: found ${results.size} results for query $query")
                mediaRepository.cacheMedia(results)
            } catch (e: Exception) {
                _searchResultsState.value =
                    FetchingState.Error(e.message ?: "Failed to search media")
            }
        }
    }

    fun loadFavorites(userId: String) {
        viewModelScope.launch(dispatcher) {
            val favorites = mediaRepository.getFavorites(userId)
            _favoritesMediaList.value = favorites
        }
    }

    fun addToFavorites(userId: String?, mediaId: String, onAuthError: () -> Unit) {
        if (userId == null) {
            onAuthError() // Trigger toast for unauthenticated
            return
        }
        viewModelScope.launch(dispatcher) {
            mediaRepository.addToFavorites(userId, mediaId)
            loadFavorites(userId) // Refresh favorites list after adding
        }
    }

    fun removeFromFavorites(userId: String?, mediaId: String, onAuthError: () -> Unit) {
        if (userId == null) {
            onAuthError() // Trigger toast for unauthenticated
            return
        }
        viewModelScope.launch(dispatcher) {
            try {
                mediaRepository.removeFromFavorites(userId, mediaId)
                loadFavorites(userId) // Refresh favorites list
            } catch (e: Exception) {
            }
        }
    }

    fun setQueueItems(newList: List<Media>) {
        val mediaItemsList = newList.map { media ->
            MediaItem.Builder()
                .setUri(media.file_url)
                .setMediaMetadata(createMediaMetadata(media))
                .build()
        }
        audioServiceHandler.setMediaItemList(mediaItemsList)
        _queueMediaList.value = newList
    }

    private fun createMediaMetadata(media: Media): MediaMetadata {
        val metadata = MediaMetadata.Builder()
            .setArtist(media.artist)
            .setDisplayTitle(media.title)
            .setTitle(media.title)
            .setGenre(media.genre.toString())
        if (media.type == "audio") {
            metadata.setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
        } else {
            metadata.setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                .setArtworkUri(media.thumbnail_url.toUri())
        }
        return metadata.build()
    }

    private fun calculateProgressValue(currentProgress: Long) {
        progress =
            if (currentProgress > 0) ((currentProgress.toFloat() / duration.toFloat()) * 100f)
            else 0f
        progressString = formatDuration(currentProgress)

    }

    private fun formatDuration(duration: Long): String {
        val totalSeconds = duration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    fun onSignedOut() {
        _favoritesMediaList.value = emptyList()
    }

    override fun onCleared() {
        viewModelScope.launch {
            audioServiceHandler.onPlayerEvents(PlayerEvent.Stop)
        }
        super.onCleared()
    }
}

sealed class UIEvents {
    object PlayPause : UIEvents()
    data class SelectedAudioChange(val index: Int) : UIEvents()
    data class SeekTo(val position: Float) : UIEvents()
    object Backward : UIEvents()
    object Forward : UIEvents()
    object SeekToNext : UIEvents()
    data class UpdateProgress(val newProgress: Float) : UIEvents()
}

sealed class UIState {
    object Initial : UIState()
    object Buffering : UIState()
    object Ready : UIState()
}

sealed class FetchingState {
    object Initial : FetchingState()
    object Loading : FetchingState()
    data class Success(val mediaList: List<Media>) : FetchingState()
    data class Error(val message: String) : FetchingState()

}