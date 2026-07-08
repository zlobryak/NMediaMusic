package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.data.dto.Album
import ru.netology.nmedia.viewmodel.AlbumViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: AlbumViewModel by viewModels()
    private lateinit var adapter: AlbumAdapter

    private var previousTrackIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()

        viewModel.initPlayerManager(this)
        viewModel.loadAlbum()
    }

    private fun setupRecyclerView() {
        adapter = AlbumAdapter(
            onTrackClick = { index ->
                viewModel.playTrack(index)
            },
            getCurrentTrackIndex = { viewModel.currentTrackIndex.value ?: -1 },
            getIsPlaying = { viewModel.isPlaying.value == true },
            getDurations = { viewModel.trackDurations.value ?: emptyMap() },
            albumArtist = ""
        )

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        binding.btnPlayPause.setOnClickListener {
            viewModel.togglePlayPause()
        }

        binding.btnNext.setOnClickListener {
            viewModel.playNext()
        }

        binding.btnPrevious.setOnClickListener {
            viewModel.playPrevious()
        }
    }

    private fun setupObservers() {
        viewModel.albumData.observe(this) { album ->
            binding.progressBar.visibility = View.VISIBLE

            adapter = AlbumAdapter(
                onTrackClick = { index ->
                    viewModel.playTrack(index)
                },
                getCurrentTrackIndex = { viewModel.currentTrackIndex.value ?: -1 },
                getIsPlaying = { viewModel.isPlaying.value == true },
                getDurations = { viewModel.trackDurations.value ?: emptyMap() },
                albumArtist = album.artist  // Передаём исполнителя
            )

            adapter.submitList(album.tracks)

            binding.recyclerView.adapter = adapter

            updateAlbumInfo(album)
            binding.progressBar.visibility = View.GONE
        }

        viewModel.currentTrackIndex.observe(this) { newIndex ->
            val oldIndex = previousTrackIndex

            // Обновляем старый трек (убираем иконку play/pause)
            if (oldIndex >= 0) {
                adapter.notifyItemChanged(oldIndex)
            }

            // Обновляем новый трек (добавляем иконку play/pause)
            if (newIndex >= 0) {
                adapter.notifyItemChanged(newIndex)
            }

            previousTrackIndex = newIndex
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            updatePlayPauseButton(isPlaying)
            val currentIndex = viewModel.currentTrackIndex.value ?: -1
            if (currentIndex >= 0) {
                adapter.notifyItemChanged(currentIndex)
            }
        }

        viewModel.trackDurations.observe(this) { durations ->
            android.util.Log.d("MainActivity", "Durations updated: ${durations.size} tracks")
            // Получаем текущий список треков
            val tracks = adapter.currentList
            tracks.forEachIndexed { index, track ->
                // Если для этого трека есть длительность, обновляем только его
                if (durations.containsKey(track.id)) {
                    adapter.notifyItemChanged(index)
                }
            }
        }

        viewModel.loadingError.observe(this) { error ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateAlbumInfo(album: Album) {
        binding.albumTitle.text = album.title
        binding.albumArtist.text = album.artist
        binding.albumInfo.text = getString(R.string.update_album_info, album.genre, album.published)
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        if (isPlaying) {
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        } else {
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        }
    }
}