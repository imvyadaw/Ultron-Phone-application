package com.ultron.companion.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ULTRON palette: near-black chassis + signature red accent (with an
// electric-cyan secondary for "online / connected" states), instead of
// the previous unstyled default MaterialTheme.
object UltronColors {
    val Red = Color(0xFFE63946)
    val RedDim = Color(0xFF7A1F27)
    val Cyan = Color(0xFF35E6D8)
    val Background = Color(0xFF0B0C10)
    val Surface = Color(0xFF15171C)
    val SurfaceHigh = Color(0xFF1E2129)
    val OnSurfaceMuted = Color(0xFF9AA1AC)
}

private val UltronDarkScheme = darkColorScheme(
    primary = UltronColors.Red,
    onPrimary = Color.White,
    secondary = UltronColors.Cyan,
    onSecondary = Color.Black,
    background = UltronColors.Background,
    onBackground = Color(0xFFE7E9EC),
    surface = UltronColors.Surface,
    onSurface = Color(0xFFE7E9EC),
    surfaceVariant = UltronColors.SurfaceHigh,
    onSurfaceVariant = UltronColors.OnSurfaceMuted,
    error = Color(0xFFFF6B6B),
    outline = Color(0xFF2A2E37),
)

private val UltronLightScheme = lightColorScheme(
    primary = UltronColors.Red,
    onPrimary = Color.White,
    secondary = Color(0xFF00A99A),
    background = Color(0xFFF6F6F7),
    surface = Color.White,
)

private val UltronTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 0.2.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontSize = 15.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.3.sp),
)

@Composable
fun UltronTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) UltronDarkScheme else UltronLightScheme,
        typography = UltronTypography,
        content = content
    )
}
