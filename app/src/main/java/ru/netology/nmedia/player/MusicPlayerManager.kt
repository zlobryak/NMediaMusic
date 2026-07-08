package ru.netology.nmedia.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import ru.netology.nmedia.data.dto.Track

class MusicPlayerManager(private val context: Context) {
    private var exoPlayer: ExoPlayer? = null
    private var currentTrackIndex = 0
    private var tracks: List<Track> = emptyList()

    var onPlaybackStateChanged: ((isPlaying: Boolean) -> Unit)? = null
    var onDurationReceived: ((trackId: Int, durationMs: Long) -> Unit)? = null
    var onTrackEnded: (() -> Unit)? = null

    val isPlaying: Boolean
        get() = exoPlayer?.isPlaying == true

    init {
        exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    // Длительность стала доступна
                    exoPlayer?.duration?.let { duration ->
                        val trackId = tracks.getOrNull(currentTrackIndex)?.id ?: return
                        onDurationReceived?.invoke(trackId, duration)
                    }
                }

                if (state == Player.STATE_ENDED) {
                    onTrackEnded?.invoke()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Дополнительный callback для кнопки play/pause
                onPlaybackStateChanged?.invoke(isPlaying)
            }
        })
    }

    fun setTracks(tracks: List<Track>) {
        this.tracks = tracks
    }

    fun playTrack(index: Int) {
        if (index < 0 || index >= tracks.size) return

        currentTrackIndex = index
        val mediaItem = MediaItem.fromUri(tracks[index].getAudioUrl())
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    /**
     * Получить длительность трека без воспроизведения
     */
    fun getTrackDuration(track: Track, callback: (Long) -> Unit) {
        val tempPlayer = ExoPlayer.Builder(context.applicationContext).build()
        val mediaItem = MediaItem.fromUri(track.getAudioUrl())

        var durationReceived = false

        tempPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        if (!durationReceived) {
                            durationReceived = true
                            val duration = tempPlayer.duration
                            if (duration > 0) {
                                callback(duration)
                            }
                            tempPlayer.release()
                        }
                    }
                    Player.STATE_IDLE, Player.STATE_ENDED -> {
                        if (!durationReceived) {
                            durationReceived = true
                            tempPlayer.release()
                        }
                    }
                    Player.STATE_BUFFERING -> {
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (!durationReceived) {
                    durationReceived = true
                    tempPlayer.release()
                }
            }
        })

        tempPlayer.setMediaItem(mediaItem)
        tempPlayer.prepare()
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}