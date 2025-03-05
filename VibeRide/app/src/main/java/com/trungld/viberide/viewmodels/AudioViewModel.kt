package com.trungld.viberide.viewmodels

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
import androidx.media3.exoplayer.ExoPlayer
import com.trungld.viberide.data.entity.Media
import com.trungld.viberide.data.repository.MediaRepository
import com.trungld.viberide.player.service.AudioState
import com.trungld.viberide.player.service.PlayerEvent
import com.trungld.viberide.player.service.VibeRideAudioServiceHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    val audioServiceHandler: VibeRideAudioServiceHandler,
    private val mediaRepository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val mediaDummy = Media("", "", "", "", "", "", "")

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
    val recommendedMediaList: StateFlow<List<Media>> = _recommendedMediaList // Public read-only state

    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val _playerState = MutableStateFlow<ExoPlayer?>(null)
    val playerState: StateFlow<ExoPlayer?> = _playerState

    init {
        loadMediaData()
    }

    init {
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest { mediaState ->
                when (mediaState) {
                    AudioState.Initial -> _uiState.value = UIState.Initial
                    is AudioState.Buffering -> calculateProgressValue(mediaState.progress)
                    is AudioState.Playing -> isPlaying = mediaState.isPlaying
                    is AudioState.Progress -> calculateProgressValue(mediaState.progress)
                    is AudioState.CurrentPlaying -> {
                        currentSelectedAudio = _recommendedMediaList.value[mediaState.mediaItemIndex]
                    }

                    is AudioState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready
                    }
                }

            }
        }
    }

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

    private fun loadMediaData() {
        // Fetch from server in a separate coroutine
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mediaRepository.fetchAndCacheMedia()
                Log.d("AudioViewModel", "Server fetch completed")
            } catch (e: Exception) {
                Log.e("AudioViewModel", "Server fetch failed: ${e.message}", e)
            }
        }
    }

    fun suggestMediaByEmotion(emotion: String){
        // Collect Room data live
        viewModelScope.launch {
            mediaRepository.getLocalMedia().collect { localMedia ->
                Log.d("AudioViewModel", "Collected ${localMedia.size} items from Room")
                _recommendedMediaList.value = localMedia.filter { media ->
                    media.genre.contains(emotion, ignoreCase = true)
                }.also {
                    Log.d("AudioViewModel", "Suggested ${it.size} items for emotion: $emotion")
                    setMediaItems(it)
                }
            }
        }
        _recommendedMediaList.value
    }

    private fun setMediaItems(mediaItems: List<Media>) {
        val mediaItemsList = mediaItems.map { media ->
            MediaItem.Builder()
                .setUri(media.file_url)
                .setMediaMetadata(createMediaMetadata(media))
                .build()
        }
        audioServiceHandler.setMediaItemList(mediaItemsList)
    }

    private fun createMediaMetadata(media: Media): MediaMetadata {
        val metadata = MediaMetadata.Builder()
            .setArtist(media.artist)
            .setDisplayTitle(media.title)
            .setGenre(media.genre)
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

    fun formatDuration(duration: Long): String {
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (minute) - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
        return String.format("%02d:%02d", minute, seconds)
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
    object Ready : UIState()
}