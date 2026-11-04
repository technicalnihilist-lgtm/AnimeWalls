package com.animewalls.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AnimeWallsColors = darkColorScheme(
    primary = AccentRed,
    onPrimary = Color.White,
    primaryContainer = AccentRedDeep,
    onPrimaryContainer = TextPrimary,
    secondary = AccentPink,
    onSecondary = AmoledBlack,
    secondaryContainer = SurfaceHigh,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentPinkMuted,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = AmoledBlack,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMid,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceMid,
    surfaceContainerHigh = SurfaceHigh,
    outline = HairlineStrong,
    outlineVariant = Hairline,
    error = Color(0xFFFF5C6C),
)

@Composable
fun AnimeWallsTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgbCompat()
            window.navigationBarColor = Color.Transparent.toArgbCompat()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AnimeWallsColors,
        typography = AnimeWallsTypography,
        content = content,
    )
}

private fun Color.toArgbCompat(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt()
)
