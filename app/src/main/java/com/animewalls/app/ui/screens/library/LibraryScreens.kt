package com.animewalls.app.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animewalls.app.domain.model.Wallpaper
import com.animewalls.app.ui.components.EmptyState
import com.animewalls.app.ui.components.SimpleWallpaperGrid
import com.animewalls.app.ui.theme.AmoledBlack
import com.animewalls.app.ui.theme.Hairline
import com.animewalls.app.ui.theme.SurfaceHigh
import com.animewalls.app.ui.theme.TextPrimary
import com.animewalls.app.ui.theme.TextTertiary

@Composable
fun FavoritesScreen(
    onOpenWallpaper: (Wallpaper) -> Unit,
    onBrowse: () -> Unit,
    viewModel: LibraryViewModel = viewModel(),
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(AmoledBlack)) {
        LibraryHeader("Favourites", "${favorites.size} saved")
        if (favorites.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.FavoriteBorder,
                title = "Nothing saved yet",
                message = "Tap the heart on any wallpaper to keep it here. Favourites survive app restarts.",
                actionLabel = "Find wallpapers",
                onAction = onBrowse,
            )
        } else {
            SimpleWallpaperGrid(
                items = favorites,
                onOpen = onOpenWallpaper,
                favoriteIds = favoriteIds,
                onToggleFavorite = viewModel::toggleFavorite,
            )
        }
    }
}

@Composable
fun DownloadsScreen(
    onOpenWallpaper: (Wallpaper) -> Unit,
    onBrowse: () -> Unit,
    viewModel: LibraryViewModel = viewModel(),
) {
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(AmoledBlack)) {
        LibraryHeader("Downloads", "${downloads.size} offline")
        if (downloads.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.KeyboardArrowDown,
                title = "No downloads yet",
                message = "Downloaded wallpapers are stored on this device and stay available without a connection.",
                actionLabel = "Browse live feed",
                onAction = onBrowse,
            )
        } else {
            SimpleWallpaperGrid(
                items = downloads,
                onOpen = onOpenWallpaper,
                favoriteIds = favoriteIds,
                onToggleFavorite = viewModel::toggleFavorite,
                trailing = { wallpaper ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceHigh)
                            .border(1.dp, Hairline, RoundedCornerShape(10.dp))
                            .clickable { viewModel.removeDownload(wallpaper) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Delete, "Delete download",
                            tint = TextTertiary,
                            modifier = Modifier.width(14.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Remove", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                    }
                },
            )
        }
    }
}

@Composable
private fun LibraryHeader(title: String, subtitle: String) {
    Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.displaySmall, color = TextPrimary)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
    }
}
