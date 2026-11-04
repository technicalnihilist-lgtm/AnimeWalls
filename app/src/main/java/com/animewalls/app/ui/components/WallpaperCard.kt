package com.animewalls.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.animewalls.app.domain.model.Wallpaper
import com.animewalls.app.ui.theme.AccentPink
import com.animewalls.app.ui.theme.AccentRed
import com.animewalls.app.ui.theme.Hairline
import com.animewalls.app.ui.theme.SurfaceHigh
import com.animewalls.app.ui.theme.SurfaceMid
import com.animewalls.app.ui.theme.TextPrimary
import com.animewalls.app.ui.theme.TextSecondary

@Composable
fun WallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 0.72f,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    showMeta: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.975f else 1f, label = "cardScale")

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .scale(scale)
            .clip(CardShape)
            .background(SurfaceMid)
            .border(1.dp, Hairline, CardShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(wallpaper.thumbUrl)
                .crossfade(true)
                .build(),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(SurfaceHigh),
            error = ColorPainter(SurfaceMid),
            modifier = Modifier.fillMaxSize(),
        )

        // Cinematic bottom scrim so text always sits on a readable base.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        0.78f to Color.Black.copy(alpha = 0.55f),
                        1f to Color.Black.copy(alpha = 0.92f),
                    )
                )
        )

        if (showMeta) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
            ) {
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LicenseBadge(wallpaper.licenseShortName)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = wallpaper.resolution,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.8f),
                        maxLines = 1,
                    )
                }
            }
        }

        if (onToggleFavorite != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { onToggleFavorite() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
                    tint = if (isFavorite) AccentRed else TextPrimary,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

@Composable
fun WallpaperRail(
    title: String,
    subtitle: String?,
    wallpapers: List<Wallpaper>,
    onOpen: (Wallpaper) -> Unit,
    onSeeAll: (() -> Unit)? = null,
    cardWidth: Dp = 158.dp,
    favoriteIds: Set<String> = emptySet(),
    onToggleFavorite: ((Wallpaper) -> Unit)? = null,
    isLoading: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth()) {
        SectionHeader(title = title, subtitle = subtitle, action = if (onSeeAll != null) "See all" else null, onAction = onSeeAll)
        Spacer(Modifier.height(10.dp))
        if (wallpapers.isEmpty()) {
            when {
                trailing != null -> trailing()
                isLoading -> RailPlaceholder()
                else -> RailEmptyNote()
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(wallpapers, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpen(wallpaper) },
                        modifier = Modifier.width(cardWidth),
                        isFavorite = favoriteIds.contains(wallpaper.id),
                        onToggleFavorite = onToggleFavorite?.let { toggle -> { toggle(wallpaper) } },
                    )
                }
            }
        }
    }
}

@Composable
fun RailEmptyNote() {
    Text(
        text = "No freely licensed images passed the checks for this collection right now.",
        style = MaterialTheme.typography.bodySmall,
        color = com.animewalls.app.ui.theme.TextTertiary,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    )
}

@Composable
fun RailPlaceholder(count: Int = 3, cardWidth: Dp = 158.dp) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items(count) {
            ShimmerBox(modifier = Modifier.width(cardWidth).aspectRatio(0.72f))
        }
    }
}

@Composable
fun GridPlaceholder() {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShimmerBox(modifier = Modifier.weight(1f).aspectRatio(0.72f))
                ShimmerBox(modifier = Modifier.weight(1f).aspectRatio(0.72f))
            }
        }
    }
}

