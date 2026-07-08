package ru.netology.nmedia.data.dto

data class Album(
    val id: Int,
    val title: String,
    val subtitle: String,
    val artist: String,
    val published: String,
    val genre: String,
    val tracks: List<Track>
)

data class Track(
    val id: Int,
    val file: String
) {
    fun getAudioUrl(): String =
        "https://raw.githubusercontent.com/netology-code/andad-homeworks/master/09_multimedia/data/$file"
}