package com.animewalls.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.animewalls.app.R

val Sora = FontFamily(
    Font(R.font.sora_regular, FontWeight.Normal),
    Font(R.font.sora_medium, FontWeight.Medium),
    Font(R.font.sora_semibold, FontWeight.SemiBold),
    Font(R.font.sora_bold, FontWeight.Bold),
)

private val Display = TextStyle(
    fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 30.sp,
    lineHeight = 36.sp, letterSpacing = (-0.6).sp,
)

val AnimeWallsTypography = Typography(
    displayLarge = Display.copy(fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-1).sp),
    displayMedium = Display.copy(fontSize = 34.sp, lineHeight = 40.sp),
    displaySmall = Display,
    headlineLarge = Display.copy(fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
    headlineMedium = Display.copy(fontSize = 22.sp, lineHeight = 28.sp),
    headlineSmall = Display.copy(fontSize = 19.sp, lineHeight = 24.sp, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    titleMedium = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
    titleSmall = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Normal, fontSize = 13.5.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Medium, fontSize = 11.5.sp, lineHeight = 15.sp, letterSpacing = 0.3.sp),
    labelSmall = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 13.sp, letterSpacing = 0.5.sp),
)
