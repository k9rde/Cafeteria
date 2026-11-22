package com.example.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Immutable
data class GlassStyleConfig(
    val opacity: Float = 0.40f,
    val blurIntensity: Float = 16f,
    val isDarkMode: Boolean = true
)

val LocalGlassStyle = staticCompositionLocalOf { GlassStyleConfig() }

private val CafeteriaDarkColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = Color(0xFF1A1104),
    primaryContainer = Color(0xFF4A2E00),
    onPrimaryContainer = Color(0xFFFFDDA9),
    secondary = AccentCyan,
    onSecondary = Color(0xFF002433),
    tertiary = AccentPurple,
    background = WarmEspresso,
    onBackground = DarkTextPrimary,
    surface = DarkGlassTint,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF2C2C34),
    onSurfaceVariant = DarkTextSecondary,
    error = StatusBillRed,
    onError = Color.White,
    outline = Color.White.copy(alpha = 0.24f)
)

private val CafeteriaLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECCE),
    onPrimaryContainer = Color(0xFF3E2200),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    tertiary = Color(0xFF9333EA),
    background = CreamSurface,
    onBackground = LightTextPrimary,
    surface = LightGlassTint,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFEAE7E0),
    onSurfaceVariant = LightTextSecondary,
    error = Color(0xFFDC2626),
    onError = Color.White,
    outline = Color.Black.copy(alpha = 0.16f)
)

@Composable
fun CafeteriaTheme(
    darkTheme: Boolean = true,
    glassOpacity: Float = 0.40f,
    blurIntensity: Float = 16f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CafeteriaDarkColorScheme else CafeteriaLightColorScheme
    val glassConfig = GlassStyleConfig(
        opacity = glassOpacity,
        blurIntensity = blurIntensity,
        isDarkMode = darkTheme
    )

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalGlassStyle provides glassConfig,
        LocalContentColor provides colorScheme.onSurface
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CafeteriaTypography,
            content = content
        )
    }
}
