package com.animewalls.app.data.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.animewalls.app.data.download.WallpaperDownloader
import com.animewalls.app.domain.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

enum class WallpaperTarget { HOME, LOCK, BOTH }

class WallpaperSetter(
    private val context: Context,
    private val client: OkHttpClient,
    private val downloader: WallpaperDownloader,
) {

    suspend fun set(wallpaper: Wallpaper, target: WallpaperTarget): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val bitmap = obtainBitmap(wallpaper)
                val manager = WallpaperManager.getInstance(context)
                when (target) {
                    WallpaperTarget.HOME ->
                        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    WallpaperTarget.LOCK ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        } else {
                            manager.setBitmap(bitmap)
                        }
                    WallpaperTarget.BOTH -> {
                        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        }
                    }
                }
                bitmap.recycle()
            }
        }

    /**
     * Decodes at (at most) screen resolution so a 12000 px Commons original never
     * blows the heap. Downloaded files are preferred over a second network fetch.
     */
    private suspend fun obtainBitmap(wallpaper: Wallpaper): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val local = downloader.localFile(wallpaper)
        if (local != null) {
            BitmapFactory.decodeFile(local.absolutePath, bounds)
            return decodeFile(local.absolutePath, bounds, targetPixels())
        }
        val request = Request.Builder()
            .url(wallpaper.fullUrl)
            .header("User-Agent", WallpaperDownloader.USER_AGENT)
            .build()
        val bytes = client.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "HTTP ${response.code}" }
            response.body?.bytes() ?: error("Empty image")
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        return decodeBytes(bytes, bounds, targetPixels())
    }

    private fun targetPixels(): Int {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        manager.defaultDisplay.getRealMetrics(metrics)
        return maxOf(metrics.widthPixels, metrics.heightPixels).coerceAtLeast(1080)
    }

    private fun decodeFile(path: String, bounds: BitmapFactory.Options, target: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, target)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(path, options) ?: error("Could not decode image")
    }

    private fun decodeBytes(bytes: ByteArray, bounds: BitmapFactory.Options, target: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, target)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: error("Could not decode image")
    }

    private fun sampleSize(width: Int, height: Int, target: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= target && h / 2 >= target) {
            w /= 2; h /= 2; sample *= 2
        }
        return sample
    }
}
