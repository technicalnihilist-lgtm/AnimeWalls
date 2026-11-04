package com.animewalls.app.ui.screens.detail

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animewalls.app.data.wallpaper.WallpaperTarget
import com.animewalls.app.di.ServiceLocator
import com.animewalls.app.domain.model.Wallpaper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = ServiceLocator.repository
    private val downloader = ServiceLocator.downloader
    private val wallpaperSetter = ServiceLocator.wallpaperSetter

    val wallpaperId: String = savedStateHandle.get<String>("wallpaperId").orEmpty()

    private val _wallpaper = MutableStateFlow<Wallpaper?>(null)
    val wallpaper: StateFlow<Wallpaper?> = _wallpaper.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _notFound = MutableStateFlow(false)
    val notFound: StateFlow<Boolean> = _notFound.asStateFlow()

    private val _isSetting = MutableStateFlow(false)
    val isSetting: StateFlow<Boolean> = _isSetting.asStateFlow()

    val downloadProgress: StateFlow<Map<String, Float>> = downloader.progress

    val isFavorite: StateFlow<Boolean> = repository.observeIsFavorite(wallpaperId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        viewModelScope.launch {
            val found = repository.findById(wallpaperId)
            _notFound.value = found == null
            _wallpaper.value = found
            found?.let { repository.recordView(it) }
        }
    }

    fun toggleFavorite() {
        val wallpaper = _wallpaper.value ?: return
        viewModelScope.launch {
            val added = repository.toggleFavorite(wallpaper)
            _message.value = if (added) "Added to favourites" else "Removed from favourites"
        }
    }

    fun download() {
        val wallpaper = _wallpaper.value ?: return
        downloader.enqueue(wallpaper)
        _message.value = "Downloading to your device…"
    }

    fun setWallpaper(target: WallpaperTarget) {
        val wallpaper = _wallpaper.value ?: return
        if (_isSetting.value) return
        viewModelScope.launch {
            _isSetting.value = true
            val result = wallpaperSetter.set(wallpaper, target)
            _isSetting.value = false
            _message.value = result.fold(
                onSuccess = {
                    when (target) {
                        WallpaperTarget.HOME -> "Home screen wallpaper set"
                        WallpaperTarget.LOCK -> "Lock screen wallpaper set"
                        WallpaperTarget.BOTH -> "Home and lock screen set"
                    }
                },
                onFailure = { "Could not set wallpaper: ${it.message ?: "unknown error"}" },
            )
        }
    }

    fun share(context: Context) {
        val wallpaper = _wallpaper.value ?: return
        val file = downloader.localFile(wallpaper)
        val intent = if (file != null) {
            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file,
            )
            Intent(Intent.ACTION_SEND).apply {
                type = wallpaper.mime.ifBlank { "image/*" }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "${wallpaper.title} — ${wallpaper.sourceUrl} (${wallpaper.licenseShortName})",
                )
            }
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "Share wallpaper"))
        }
    }

    fun openSource(context: Context, url: String) {
        if (url.isBlank()) return
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))) }
    }

    fun consumeMessage() { _message.value = null }
}
