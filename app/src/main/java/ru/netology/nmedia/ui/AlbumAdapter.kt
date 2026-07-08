package ru.netology.nmedia.ui

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
    private val getDurations: () -> Map<Int, Long>
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
        private val trackTitle: TextView = itemView.findViewById(R.id.trackTitle)
        private val trackDuration: TextView = itemView.findViewById(R.id.trackDuration)
        private val playIcon: ImageView = itemView.findViewById(R.id.trackPlayIcon)

        fun bind(track: Track, position: Int) {
            trackNumber.text = (position + 1).toString()
            trackTitle.text = track.file.replace(".mp3", "")
            val durations = getDurations()
            val durationMs = durations[track.id]
            trackDuration.text = if (durationMs != null) {
                formatDuration(durationMs)
            } else {
                "—"
            }

            val isCurrentTrack = getCurrentTrackIndex() == position
            val playing = getIsPlaying()

            if (isCurrentTrack && playing) {
                playIcon.setImageResource(R.drawable.ic_pause)
            } else {
                playIcon.setImageResource(R.drawable.ic_play)
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

/**
 * Форматирование миллисекунд в MM:SS
 */
fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}