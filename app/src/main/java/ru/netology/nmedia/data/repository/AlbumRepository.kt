package ru.netology.nmedia.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.netology.nmedia.data.dto.Album
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AlbumRepository {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun getAlbum(): Album = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder()
            .url("https://github.com/netology-code/andad-homeworks/raw/master/09_multimedia/data/album.json")
            .build()

        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Проверяем isActive, чтобы избежать IllegalArgumentException,
                // если корутина уже была отменена
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body.string().let { json ->
                    val album = gson.fromJson(json, Album::class.java)
                    if (continuation.isActive) {
                        continuation.resume(album)
                    }
                }
            }
        })

        continuation.invokeOnCancellation {
            call.cancel()
        }
    }
}