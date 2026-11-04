package com.animewalls.app.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animewalls.app.domain.model.Wallpaper
import com.animewalls.app.ui.components.EmptyState
import com.animewalls.app.ui.components.ErrorState
import com.animewalls.app.ui.components.GridPlaceholder
import com.animewalls.app.ui.components.WallpaperGrid
import com.animewalls.app.ui.theme.AccentPink
import com.animewalls.app.ui.theme.AmoledBlack
import com.animewalls.app.ui.theme.Hairline
import com.animewalls.app.ui.theme.SurfaceMid
import com.animewalls.app.ui.theme.TextPrimary
import com.animewalls.app.ui.theme.TextSecondary
import com.animewalls.app.ui.theme.TextTertiary

@Composable
fun CategoryScreen(
    onBack: () -> Unit,
    onOpenWallpaper: (Wallpaper) -> Unit,
    viewModel: CategoryViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val category = viewModel.category

    Column(Modifier.fillMaxSize().background(AmoledBlack)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceMid)
                    .padding(0.dp)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = category?.name ?: "Collection",
                    style = MaterialTheme.typography.headlineSmall.copy(letterSpacing = (-0.2).sp),
                    color = TextPrimary,
                )
                Text(
                    text = category?.blurb ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    maxLines = 2,
                )
            }
        }

        when {
            state.isLoading && state.items.isEmpty() -> GridPlaceholder()
            state.error != null && state.items.isEmpty() ->
                ErrorState(state.error!!, onRetry = { viewModel.load(reset = true) })
            state.items.isEmpty() && state.loadedOnce ->
                EmptyState(
                    icon = Icons.Filled.Star,
                    title = viewModel.emptyTitle(),
                    message = viewModel.emptyMessage(),
                    actionLabel = "Back to browse",
                    onAction = onBack,
                )
            else -> WallpaperGrid(
                items = state.items,
                onOpen = onOpenWallpaper,
                favoriteIds = favorites,
                onToggleFavorite = viewModel::toggleFavorite,
                hasMore = state.hasMore,
                isLoadingMore = state.isLoadingMore,
                onLoadMore = { viewModel.load() },
            )
        }
    }
}
