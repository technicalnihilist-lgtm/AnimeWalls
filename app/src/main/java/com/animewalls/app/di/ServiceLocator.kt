package com.animewalls.app.di

import android.content.Context
import com.animewalls.app.BuildConfig
import com.animewalls.app.data.download.WallpaperDownloader
import com.animewalls.app.data.local.AnimeWallsDatabase
import com.animewalls.app.data.remote.WikimediaApi
import com.animewalls.app.data.remote.WikimediaMapper
import com.animewalls.app.data.repository.WallpaperRepository
import com.animewalls.app.data.wallpaper.WallpaperSetter
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/** Minimal hand-rolled DI: one graph, built once, no reflection cost. */
object ServiceLocator {

    lateinit var repository: WallpaperRepository
        private set
    lateinit var downloader: WallpaperDownloader
        private set
    lateinit var wallpaperSetter: WallpaperSetter
        private set
    lateinit var okHttpClient: OkHttpClient
        private set

    @Volatile private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val app = context.applicationContext

            okHttpClient = OkHttpClient.Builder()
                .cache(Cache(File(app.cacheDir, "http"), 32L * 1024 * 1024))
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("User-Agent", WallpaperDownloader.USER_AGENT)
                            .header("Accept", "application/json,image/*")
                            .build()
                    )
                }
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(
                            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
                        )
                    }
                }
                .build()

            val api = Retrofit.Builder()
                .baseUrl("https://commons.wikimedia.org/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WikimediaApi::class.java)

            val database = AnimeWallsDatabase.get(app)
            repository = WallpaperRepository(api, WikimediaMapper(), database)
            downloader = WallpaperDownloader(app, okHttpClient, repository)
            wallpaperSetter = WallpaperSetter(app, okHttpClient, downloader)

            initialized = true
        }
    }
}
