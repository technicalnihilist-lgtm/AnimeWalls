package com.animewalls.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.animewalls.app.R
import com.animewalls.app.domain.catalog.CategoryCatalog
import com.animewalls.app.domain.model.Wallpaper
import com.animewalls.app.ui.components.CardShape
import com.animewalls.app.ui.components.EmptyState
import com.animewalls.app.ui.components.ErrorState
import com.animewalls.app.ui.components.GlassCard
import com.animewalls.app.ui.components.LicenseBadge
import com.animewalls.app.ui.components.PillChip
import com.animewalls.app.ui.components.RailPlaceholder
import com.animewalls.app.ui.components.SectionHeader
import com.animewalls.app.ui.components.ShimmerBox
import com.animewalls.app.ui.components.WallpaperRail
import com.animewalls.app.ui.theme.AccentPink
import com.animewalls.app.ui.theme.AccentRed
import com.animewalls.app.ui.theme.AmoledBlack
import com.animewalls.app.ui.theme.Hairline
import com.animewalls.app.ui.theme.SurfaceHigh
import com.animewalls.app.ui.theme.SurfaceMid
import com.animewalls.app.ui.theme.TextPrimary
import com.animewalls.app.ui.theme.TextSecondary
import com.animewalls.app.ui.theme.TextTertiary

@Composable
fun HomeScreen(
    onOpenWallpaper: (Wallpaper) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCredits: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val recentlyViewed by viewModel.recentlyViewed.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AmoledBlack),
        contentPadding = PaddingValues(bottom = 120.dp),
    ) {
        item { HomeHeader(onOpenCredits = onOpenCredits, onRefresh = viewModel::refresh) }
        item { SearchEntry(onClick = onOpenSearch) }

        if (state.error != null && state.isEmpty) {
            item { ErrorState(state.error!!, onRetry = viewModel::refresh) }
        }

        // Daily wallpaper hero
        item {
            if (state.isLoading && state.daily == null) {
                Box(Modifier.padding(20.dp)) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth().aspectRatio(1.35f))
                }
            } else {
                state.daily?.let { daily ->
                    DailyHero(daily, onOpen = { onOpenWallpaper(daily) })
                }
            }
        }

        item { Spacer(Modifier.height(26.dp)) }

        item {
            WallpaperRail(
                title = "Featured",
                subtitle = "Hand-checked, freely licensed artwork",
                wallpapers = state.featured,
                onOpen = onOpenWallpaper,
                favoriteIds = favorites,
                onToggleFavorite = viewModel::toggleFavorite,
                onSeeAll = { onOpenCategory("sc_sakura") },
                isLoading = state.isLoading,
            )
        }
        item { Spacer(Modifier.height(26.dp)) }

        item {
            WallpaperRail(
                title = "Trending now",
                subtitle = "Most viewed in the last 24 hours",
                wallpapers = state.trending,
                onOpen = onOpenWallpaper,
                favoriteIds = favorites,
                onToggleFavorite = viewModel::toggleFavorite,
                onSeeAll = { onOpenCategory("col_fantasy") },
                isLoading = state.isLoading,
            )
        }
        item { Spacer(Modifier.height(26.dp)) }

        // Popular characters
        item {
            Column {
                SectionHeader(
                    title = "Popular characters",
                    subtitle = "Character art is only shown when its licence is verifiable",
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(CategoryCatalog.characters) { category ->
                        PillChip(text = category.name, onClick = { onOpenCategory(category.id) })
                    }
                }
            }
        }
        item { Spacer(Modifier.height(26.dp)) }

        item {
            WallpaperRail(
                title = "Anime scenery",
                subtitle = "Streets, sakura and mountain air",
                wallpapers = state.scenery,
                onOpen = onOpenWallpaper,
                favoriteIds = favorites,
                onToggleFavorite = viewModel::toggleFavorite,
                onSeeAll = { onOpenCategory("sc_streets") },
                isLoading = state.isLoading,
            )
        }
        item { Spacer(Modifier.height(26.dp)) }

        item {
            WallpaperRail(
                title = "Dark anime collection",
                subtitle = "Built for AMOLED screens",
                wallpapers = state.dark,
                onOpen = onOpenWallpaper,
                favoriteIds = favorites,
                onToggleFavorite = viewModel::toggleFavorite,
                onSeeAll = { onOpenCategory("col_dark") },
                isLoading = state.isLoading,
            )
        }
        item { Spacer(Modifier.height(26.dp)) }

        item {
            WallpaperRail(
                title = "Aesthetic collection",
                subtitle = "Quiet compositions, soft light",
                wallpapers = state.aesthetic,
                onOpen = onOpenWallpaper,
                favoriteIds = favorites,
                onToggleFavorite = viewModel::toggleFavorite,
                onSeeAll = { onOpenCategory("col_aesthetic") },
                isLoading = state.isLoading,
            )
        }
        item { Spacer(Modifier.height(26.dp)) }

        item {
            WallpaperRail(
                title = "Freely licensed anime art",
                subtitle = "Original illustrations released under CC0 / CC BY",
                wallpapers = state.characters,
                onOpen = onOpenWallpaper,
                favoriteIds = favorites,
                onToggleFavorite = viewModel::toggleFavorite,
                onSeeAll = { onOpenCategory("col_girls") },
                isLoading = state.isLoading,
            )
        }
        item { Spacer(Modifier.height(26.dp)) }

        item {
            WallpaperRail(
                title = "Recently added",
                subtitle = "Fresh uploads from Wikimedia Commons",
                wallpapers = state.recentlyAdded,
                onOpen = onOpenWallpaper,
                favoriteIds = favorites,
                onToggleFavorite = viewModel::toggleFavorite,
                onSeeAll = { onOpenCategory("sc_night") },
                isLoading = state.isLoading,
            )
        }
        item { Spacer(Modifier.height(26.dp)) }

        if (recentlyViewed.isNotEmpty()) {
            item {
                WallpaperRail(
                    title = "Recently viewed",
                    subtitle = "Picked up where you left off",
                    wallpapers = recentlyViewed,
                    onOpen = onOpenWallpaper,
                    favoriteIds = favorites,
                    onToggleFavorite = viewModel::toggleFavorite,
                )
            }
            item { Spacer(Modifier.height(26.dp)) }
        }

        item { PopularAnimeStrip(onOpenCategory = onOpenCategory) }
        item { Spacer(Modifier.height(30.dp)) }
        item { LicenceNote() }
    }
}

@Composable
private fun HomeHeader(onOpenCredits: () -> Unit, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(AccentRed.copy(alpha = 0.14f))
                .border(1.dp, AccentRed.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "ANIMEWALLS",
                style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 3.2.sp, fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
            Text(
                text = "LIVE · WIKIMEDIA COMMONS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                color = TextTertiary,
            )
        }
        IconButtonSmall(Icons.Filled.Refresh, "Refresh live feed", onRefresh)
        Spacer(Modifier.width(6.dp))
        IconButtonSmall(Icons.Filled.Info, "Credits and licences", onOpenCredits)
    }
}

@Composable
private fun IconButtonSmall(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SurfaceMid)
            .border(1.dp, Hairline, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = TextSecondary, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun SearchEntry(onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Search anime, characters, scenery…",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DailyHero(wallpaper: Wallpaper, onOpen: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "WALLPAPER OF THE DAY",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                color = AccentPink,
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(4.dp).clip(CircleShape).background(AccentRed))
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.35f)
                .clip(CardShape)
                .background(SurfaceMid)
                .border(1.dp, Hairline, CardShape)
                .clickable { onOpen() },
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(wallpaper.thumbUrl).crossfade(true).build(),
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(SurfaceHigh),
                error = ColorPainter(SurfaceMid),
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0.4f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.9f),
                    )
                )
            )
            Column(
                Modifier.align(Alignment.BottomStart).padding(16.dp),
            ) {
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LicenseBadge(wallpaper.licenseShortName)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${wallpaper.resolution}  ·  ${wallpaper.artist}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularAnimeStrip(onOpenCategory: (String) -> Unit) {
    Column {
        SectionHeader(
            title = "Popular anime",
            subtitle = "Series pages — shown only where Commons has verifiable art",
        )
        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(CategoryCatalog.popularAnime) { category ->
                PillChip(text = category.name, onClick = { onOpenCategory(category.id) })
            }
        }
    }
}

@Composable
private fun LicenceNote() {
    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Every image here is served live from Wikimedia Commons and checked against a licence allow-list (CC0, public domain, CC BY, CC BY-SA). Character artwork whose rights cannot be verified is withheld — that is why some series pages are empty.",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
            )
        }
    }
}
