package ru.netology.nmedia.ui

import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.data.dto.Track
import java.util.Locale
import java.util.concurrent.TimeUnit

class AlbumAdapter(
    private val onTrackClick: (Int) -> Unit,
    private val getCurrentTrackIndex: () -> Int,
    private val getIsPlaying: () -> Boolean,
    private val getDurations: () -> Map<Int, Long>,
    private val albumArtist: String
) : ListAdapter<Track, AlbumAdapter.TrackViewHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNumber: TextView = itemView.findViewById(R.id.trackNumber)
        private val trackPlayIcon: ImageView = itemView.findViewById(R.id.trackPlayIcon)
        private val trackPlayingIndicator: ImageView = itemView.findViewById(R.id.trackPlayingIndicator)
        private val trackTitle: TextView = itemView.findViewById(R.id.trackTitle)
        private val trackArtist: TextView = itemView.findViewById(R.id.trackArtist)
        private val trackDuration: TextView = itemView.findViewById(R.id.trackDuration)

        fun bind(track: Track, position: Int) {
            trackTitle.text = track.file.removeSuffix(".mp3")
            trackArtist.text = albumArtist


            // Получаем длительность
            val durations = getDurations()
            val durationMs = durations[track.id]
            trackDuration.text = if (durationMs != null) {
                formatDuration(durationMs)
            } else {
                "—"
            }

            val isCurrentTrack = getCurrentTrackIndex() == position
            val playing = getIsPlaying()

            if (isCurrentTrack) {
                // Показываем иконку play/pause
                trackNumber.visibility = View.GONE
                trackPlayIcon.visibility = View.VISIBLE

                if (playing) {
                    // Играет - показываем pause и анимированный эквалайзер
                    trackPlayIcon.setImageResource(R.drawable.ic_pause)
                    trackPlayingIndicator.visibility = View.VISIBLE

                    // Запускаем анимацию эквалайзера
                    val animationDrawable = itemView.context.getDrawable(R.drawable.anim_equalizer) as? AnimationDrawable
                    trackPlayingIndicator.setImageDrawable(animationDrawable)
                    animationDrawable?.start()

                    // Подсвечиваем название трека
                    trackTitle.setTextColor(itemView.context.getColor(R.color.text_primary))
                } else {
                    // Пауза - показываем play и статичный эквалайзер
                    trackPlayIcon.setImageResource(R.drawable.ic_play)
                    trackPlayingIndicator.visibility = View.VISIBLE
                    trackPlayingIndicator.setImageResource(R.drawable.ic_equalizer)

                    // Приглушаем название трека
                    trackTitle.setTextColor(itemView.context.getColor(R.color.text_secondary))
                }
            } else {
                // Для остальных треков показываем номер
                trackNumber.visibility = View.VISIBLE
                trackPlayIcon.visibility = View.GONE
                trackPlayingIndicator.visibility = View.GONE
                trackNumber.text = (position + 1).toString()

                // Обычный цвет
                trackTitle.setTextColor(itemView.context.getColor(R.color.text_primary))
            }

            itemView.setOnClickListener {
                onTrackClick(position)
            }
        }
    }

    class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Track, newItem: Track) = oldItem == newItem
    }
}

fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}