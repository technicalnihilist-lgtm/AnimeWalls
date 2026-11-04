package com.animewalls.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.animewalls.app.di.ServiceLocator

class AnimeWallsApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }

    /**
     * Coil gets the app's own OkHttp client, not its default one.
     *
     * This matters: OkHttp sends `User-Agent: okhttp/x.y.z` by default, and
     * Wikimedia's image CDN answers that with 403 Forbidden. The shared client
     * injects a descriptive User-Agent (see ServiceLocator), which is what the
     * API calls already use — without this, every thumbnail fails silently.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient(ServiceLocator.okHttpClient)
            .crossfade(220)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(256L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
}
