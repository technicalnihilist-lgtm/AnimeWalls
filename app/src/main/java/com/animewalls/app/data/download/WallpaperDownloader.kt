package com.animewalls.app.data.download

import android.content.Context
import com.animewalls.app.data.repository.WallpaperRepository
import com.animewalls.app.domain.model.Wallpaper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * Application-scoped downloader: transfers keep running when the user leaves the
 * detail screen, and progress is exposed as state so any screen can observe it.
 */
class WallpaperDownloader(
    private val context: Context,
    private val client: OkHttpClient,
    private val repository: WallpaperRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress: StateFlow<Map<String, Float>> = _progress.asStateFlow()

    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors: StateFlow<Map<String, String>> = _errors.asStateFlow()

    fun directory(): File = File(context.filesDir, "wallpapers").apply { if (!exists()) mkdirs() }

    fun fileFor(wallpaper: Wallpaper): File {
        val ext = when (wallpaper.mime.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        return File(directory(), "${wallpaper.id}.$ext")
    }

    fun localFile(wallpaper: Wallpaper): File? = fileFor(wallpaper).takeIf { it.exists() && it.length() > 0 }

    fun enqueue(wallpaper: Wallpaper) {
        if (_progress.value.containsKey(wallpaper.id)) return
        scope.launch {
            try {
                setProgress(wallpaper.id, 0f)
                val target = fileFor(wallpaper)
                if (target.exists() && target.length() > 0) {
                    repository.registerDownload(wallpaper, target.absolutePath)
                    setProgress(wallpaper.id, 1f)
                    return@launch
                }
                val request = Request.Builder()
                    .url(wallpaper.fullUrl)
                    .header("User-Agent", USER_AGENT)
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) error("HTTP ${response.code}")
                    val body = response.body ?: error("Empty response")
                    val total = body.contentLength()
                    target.outputStream().use { output ->
                        body.byteStream().use { input ->
                            val buffer = ByteArray(64 * 1024)
                            var written = 0L
                            while (true) {
                                val read = input.read(buffer)
                                if (read <= 0) break
                                output.write(buffer, 0, read)
                                written += read
                                if (total > 0) setProgress(wallpaper.id, written.toFloat() / total)
                            }
                        }
                    }
                }
                repository.registerDownload(wallpaper, target.absolutePath)
                setProgress(wallpaper.id, 1f)
            } catch (t: Throwable) {
                _errors.value = _errors.value + (wallpaper.id to (t.message ?: "Download failed"))
                _progress.value = _progress.value - wallpaper.id
            }
        }
    }

    suspend fun remove(wallpaper: Wallpaper) {
        fileFor(wallpaper).delete()
        repository.removeDownload(wallpaper.id)
    }

    private fun setProgress(id: String, value: Float) {
        _progress.value = _progress.value + (id to value.coerceIn(0f, 1f))
        if (value >= 1f) {
            scope.launch {
                kotlinx.coroutines.delay(1_200)
                _progress.value = _progress.value - id
            }
        }
    }

    companion object {
        const val USER_AGENT = "AnimeWalls/1.0 (Android; wallpaper app)"
    }
}
