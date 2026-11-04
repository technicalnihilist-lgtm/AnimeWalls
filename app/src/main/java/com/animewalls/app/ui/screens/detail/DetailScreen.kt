package com.animewalls.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.animewalls.app.data.wallpaper.WallpaperTarget
import com.animewalls.app.domain.model.Wallpaper
import com.animewalls.app.ui.components.GlassCard
import com.animewalls.app.ui.components.LicenseBadge
import com.animewalls.app.ui.components.MetaRow
import com.animewalls.app.ui.theme.AccentPink
import com.animewalls.app.ui.theme.AccentRed
import com.animewalls.app.ui.theme.AmoledBlack
import com.animewalls.app.ui.theme.Hairline
import com.animewalls.app.ui.theme.SuccessGreen
import com.animewalls.app.ui.theme.SurfaceHigh
import com.animewalls.app.ui.theme.SurfaceMid
import com.animewalls.app.ui.theme.TextPrimary
import com.animewalls.app.ui.theme.TextSecondary
import com.animewalls.app.ui.theme.TextTertiary

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel(),
) {
    val wallpaper by viewModel.wallpaper.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val progressMap by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val isSetting by viewModel.isSetting.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val notFound by viewModel.notFound.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2_200)
            viewModel.consumeMessage()
        }
    }

    Box(Modifier.fillMaxSize().background(AmoledBlack)) {
        val current = wallpaper
        if (current != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(current.fullUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = current.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.55f),
                        0.30f to Color.Transparent,
                        0.52f to Color.Black.copy(alpha = 0.35f),
                        1f to Color.Black.copy(alpha = 0.96f),
                    )
                )
            )

            TopBar(
                isFavorite = isFavorite,
                onBack = onBack,
                onToggleFavorite = viewModel::toggleFavorite,
                onShare = { viewModel.share(context) },
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 180.dp),
            ) {
                BottomPanel(
                    wallpaper = current,
                    progress = progressMap[current.id],
                    isSetting = isSetting,
                    onDownload = viewModel::download,
                    onSetHome = { viewModel.setWallpaper(WallpaperTarget.HOME) },
                    onSetLock = { viewModel.setWallpaper(WallpaperTarget.LOCK) },
                    onSetBoth = { viewModel.setWallpaper(WallpaperTarget.BOTH) },
                    onOpenSource = { viewModel.openSource(context, current.sourceUrl) },
                    onOpenLicense = { viewModel.openSource(context, current.licenseUrl) },
                )
            }

            if (message != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 62.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SurfaceHigh)
                        .border(1.dp, Hairline, RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                ) {
                    Text(message!!, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
        } else if (notFound) {
            com.animewalls.app.ui.components.EmptyState(
                icon = androidx.compose.material.icons.Icons.Filled.Info,
                title = "Wallpaper unavailable",
                message = "This wallpaper is no longer in the live feed. Go back and pick another one.",
                actionLabel = "Go back",
                onAction = onBack,
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentRed, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            }
            TopBar(isFavorite = false, onBack = onBack, onToggleFavorite = {}, onShare = {})
        }
    }
}

@Composable
private fun TopBar(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundIcon(Icons.Filled.ArrowBack, "Back", onBack)
        Spacer(Modifier.weight(1f))
        RoundIcon(
            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            "Favourite",
            onToggleFavorite,
            tint = if (isFavorite) AccentRed else TextPrimary,
        )
        Spacer(Modifier.width(10.dp))
        RoundIcon(Icons.Filled.Share, "Share", onShare)
    }
}

@Composable
private fun RoundIcon(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color = TextPrimary,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Hairline, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun BottomPanel(
    wallpaper: Wallpaper,
    progress: Float?,
    isSetting: Boolean,
    onDownload: () -> Unit,
    onSetHome: () -> Unit,
    onSetLock: () -> Unit,
    onSetBoth: () -> Unit,
    onOpenSource: () -> Unit,
    onOpenLicense: () -> Unit,
) {
    Column(Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = wallpaper.title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LicenseBadge(wallpaper.licenseShortName)
            Spacer(Modifier.width(8.dp))
            Text(
                text = wallpaper.resolution,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "·",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = wallpaper.mime.substringAfter('/').uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
            )
        }

        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            label = if (progress != null) "Downloading… ${(progress * 100).toInt()}%" else "Download",
            icon = if (progress != null && progress >= 1f) Icons.Filled.Check else Icons.Filled.KeyboardArrowDown,
            onClick = onDownload,
            enabled = progress == null,
        )

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton(
                label = "Home",
                icon = Icons.Filled.Home,
                onClick = onSetHome,
                enabled = !isSetting,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                label = "Lock",
                icon = Icons.Filled.Lock,
                onClick = onSetLock,
                enabled = !isSetting,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                label = "Both",
                icon = Icons.Filled.Phone,
                onClick = onSetBoth,
                enabled = !isSetting,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "CREDITS & LICENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentPink,
                )
                Spacer(Modifier.height(6.dp))
                MetaRow("Artist", wallpaper.artist)
                MetaRow("Licence", wallpaper.licenseName.ifBlank { wallpaper.licenseShortName })
                MetaRow("Resolution", wallpaper.resolution)
                if (wallpaper.dateCreated.isNotBlank()) MetaRow("Date", wallpaper.dateCreated)
                MetaRow(
                    "Modifications",
                    "Displayed unmodified. Wikimedia generates the thumbnail; the app crops to fit your screen.",
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LinkPill("Source page", Icons.Filled.ArrowForward, onOpenSource)
                    if (wallpaper.licenseUrl.isNotBlank()) {
                        LinkPill("Licence deed", Icons.Filled.ArrowForward, onOpenLicense)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PrimaryButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) AccentRed else AccentRed.copy(alpha = 0.4f))
            .clickable(enabled = enabled) { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(9.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Composable
private fun SecondaryButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceMid.copy(alpha = 0.85f))
            .border(1.dp, Hairline, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (enabled) AccentPink else TextTertiary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) TextPrimary else TextTertiary,
        )
    }
}

@Composable
private fun LinkPill(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceHigh)
            .border(1.dp, Hairline, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AccentPink)
        Spacer(Modifier.width(5.dp))
        Icon(icon, null, tint = AccentPink, modifier = Modifier.size(12.dp))
    }
}
