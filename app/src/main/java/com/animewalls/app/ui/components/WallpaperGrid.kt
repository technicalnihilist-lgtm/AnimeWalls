package com.animewalls.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.animewalls.app.domain.model.Wallpaper
import com.animewalls.app.ui.theme.TextTertiary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Shared two-column grid with infinite scroll. Pagination is triggered when the
 * last row comes into view, so a slow connection never blocks the first paint.
 */
@Composable
fun WallpaperGrid(
    items: List<Wallpaper>,
    onOpen: (Wallpaper) -> Unit,
    favoriteIds: Set<String>,
    onToggleFavorite: ((Wallpaper) -> Unit)?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    hasMore: Boolean = false,
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState, hasMore, isLoadingMore) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .filter { it >= items.size - 4 && hasMore && !isLoadingMore }
            .collect { onLoadMore() }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (header != null) {
            item(span = { GridItemSpan(maxLineSpan) }) { header() }
        }
        items(items, key = { it.id }) { wallpaper ->
            WallpaperCard(
                wallpaper = wallpaper,
                onClick = { onOpen(wallpaper) },
                isFavorite = favoriteIds.contains(wallpaper.id),
                onToggleFavorite = onToggleFavorite?.let { toggle -> { toggle(wallpaper) } },
            )
        }
        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LoadingState(message = "Loading more…")
            }
        }
        if (footer != null) {
            item(span = { GridItemSpan(maxLineSpan) }) { footer() }
        }
    }
}

/** Simple two-column grid for locally stored items (favourites, downloads). */
@Composable
fun SimpleWallpaperGrid(
    items: List<Wallpaper>,
    onOpen: (Wallpaper) -> Unit,
    favoriteIds: Set<String>,
    onToggleFavorite: ((Wallpaper) -> Unit)?,
    modifier: Modifier = Modifier,
    trailing: (@Composable (Wallpaper) -> Unit)? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items, key = { it.id }) { wallpaper ->
            androidx.compose.foundation.layout.Column {
                WallpaperCard(
                    wallpaper = wallpaper,
                    onClick = { onOpen(wallpaper) },
                    isFavorite = favoriteIds.contains(wallpaper.id),
                    onToggleFavorite = onToggleFavorite?.let { toggle -> { toggle(wallpaper) } },
                )
                if (trailing != null) {
                    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        trailing(wallpaper)
                    }
                }
            }
        }
    }
}



