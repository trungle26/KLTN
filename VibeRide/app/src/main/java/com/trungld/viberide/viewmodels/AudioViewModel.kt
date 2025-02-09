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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.trungld.viberide.data.entities.Media
import com.trungld.viberide.player.service.AudioState
import com.trungld.viberide.player.service.PlayerEvent
import com.trungld.viberide.player.service.VibeRideAudioServiceHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val mediaDummy = Media("", "", "", "", "", "")


@HiltViewModel
class AudioViewModel @Inject constructor(
    val audioServiceHandler: VibeRideAudioServiceHandler,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

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

    private val _mediaList = MutableStateFlow<List<Media>>(emptyList()) // Backing state
    val mediaList: StateFlow<List<Media>> = _mediaList // Public read-only state

    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val _playerState = MutableStateFlow<ExoPlayer?>(null)

    val playerState: StateFlow<ExoPlayer?> = _playerState
    private var currentPosition: Long = 0L

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
                        currentSelectedAudio = _mediaList.value[mediaState.mediaItemIndex]
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
        db.collection("media")
            .get()
            .addOnSuccessListener { result ->
                val mediaItems = result.documents.mapNotNull { document ->
                    document.toObject(Media::class.java)
                }
                // Process each media item to replace gs:// with download URLs
                viewModelScope.launch {
                    val updatedMediaList = mediaItems.map { media ->
                        try {
                            // Convert file_url
                            val fileRef = storage.getReferenceFromUrl(media.file_url)
                            val fileUrl = fileRef.downloadUrl.await().toString()

                            // Create a new Media object with updated URLs
                            media.copy(
                                file_url = fileUrl
                            )
                        } catch (e: Exception) {
                            // Handle errors (e.g., log, use fallback)
                            e.printStackTrace()
                            media // Return original item if URL fetch fails
                        }
                    }
                    // Update LiveData/media list with URLs
                    _mediaList.value = updatedMediaList
                    setMediaItems(updatedMediaList)
                }
            }
            .addOnFailureListener { exception ->
                // Handle Firestore fetch errors
            }
    }

    private fun setMediaItems(mediaItems: List<Media>) {
        val mediaItemsList = mediaItems.map { media ->
            Log.d("URI", "setMediaItems: uri is :${media.file_url}")
            MediaItem.Builder()
                .setUri(media.file_url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(media.artist)
                        .setDisplayTitle(media.title)
                        .setSubtitle(media.artist)
                        .build()
                )
                .build()
        }
        audioServiceHandler.setMediaItemList(mediaItemsList)
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
        viewModelScope.launch{
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