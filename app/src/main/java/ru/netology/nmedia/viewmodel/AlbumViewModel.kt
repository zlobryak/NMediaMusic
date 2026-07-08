package ru.netology.nmedia.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.data.dto.Album
import ru.netology.nmedia.data.dto.Track
import ru.netology.nmedia.data.repository.AlbumRepository
import ru.netology.nmedia.player.MusicPlayerManager
import kotlin.coroutines.resume

class AlbumViewModel : ViewModel() {
    private val repository = AlbumRepository()

    private val _albumData = MutableLiveData<Album>()
    val albumData: LiveData<Album> = _albumData

    private val _loadingError = MutableLiveData<Throwable>()
    val loadingError: LiveData<Throwable> = _loadingError

    private val _currentTrackIndex = MutableLiveData(-1)
    val currentTrackIndex: LiveData<Int> = _currentTrackIndex

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _trackDurations = MutableLiveData<Map<Int, Long>>(emptyMap())
    val trackDurations: LiveData<Map<Int, Long>> = _trackDurations

    private lateinit var playerManager: MusicPlayerManager

    fun initPlayerManager(context: Context) {
        playerManager = MusicPlayerManager(context)

        playerManager.onPlaybackStateChanged = { playing ->
            _isPlaying.postValue(playing)
        }
        playerManager.onDurationReceived = { trackId, durationMs ->
            val currentDurations = _trackDurations.value.orEmpty().toMutableMap()
            currentDurations[trackId] = durationMs
            _trackDurations.postValue(currentDurations)
        }
        playerManager.onTrackEnded = {
            playNext()
        }
    }

    fun loadAlbum() {
        viewModelScope.launch {
            try {
                val album = repository.getAlbum()
                _albumData.value = album
                playerManager.setTracks(album.tracks)

                // Загружаем длительности всех треков
                loadAllDurations(album.tracks)
            } catch (e: Exception) {
                _loadingError.value = e
            }
        }
    }

    /**
     * Загружаем длительности всех треков последовательно
     */
    private fun loadAllDurations(tracks: List<Track>) {
        viewModelScope.launch(Dispatchers.Main) {
            for (track in tracks) {
                try {
                    val duration = getDurationForTrack(track)
                    if (duration != null) {
                        updateDuration(track.id, duration)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Получаем длительность для одного трека
     */
    private suspend fun getDurationForTrack(track: Track): Long? {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            playerManager.getTrackDuration(track) { duration ->
                if (continuation.isActive) {
                    continuation.resume(duration)
                }
            }
        }
    }

    fun playTrack(index: Int) {
        _currentTrackIndex.value = index
        playerManager.playTrack(index)
    }

    fun togglePlayPause() {
        if (playerManager.isPlaying) {
            playerManager.pause()
        } else {
            // Если трек не выбран, запускаем первый
            if (_currentTrackIndex.value == -1 && (_albumData.value?.tracks?.isNotEmpty() == true)) {
                playTrack(0)
            } else {
                playerManager.play()
            }
        }
    }


    fun playNext() {
        val currentIndex = _currentTrackIndex.value ?: -1
        val tracksSize = _albumData.value?.tracks?.size ?: 0
        if (tracksSize > 0) {
            val nextIndex = (currentIndex + 1) % tracksSize
            playTrack(nextIndex)
        }
    }

    fun playPrevious() {
        val currentIndex = _currentTrackIndex.value ?: -1
        val tracksSize = _albumData.value?.tracks?.size ?: 0
        if (tracksSize > 0) {
            val prevIndex = if (currentIndex == 0) tracksSize - 1 else currentIndex - 1
            playTrack(prevIndex)
        }
    }

    private fun updateDuration(trackId: Int, durationMs: Long) {
        val currentDurations = _trackDurations.value.orEmpty().toMutableMap()
        currentDurations[trackId] = durationMs
        _trackDurations.postValue(currentDurations)
    }


    override fun onCleared() {
        playerManager.release()
    }
}