package dev.core.uikit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Joriy dark rejim — foydalanuvchi tanlovi bilan boshqariladi (Sozlamalar → mavzu).
 * [AppTheme] o'rnatadi; palitralar (masalan auth `appPalette`) shu qiymatga ergashadi.
 */
val LocalDarkTheme = staticCompositionLocalOf<Boolean?> { null }

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PrimaryAccent,
    background = SurfaceLight,
    surface = SurfaceLight,
    onSurface = Ink,
    onBackground = Ink,
    // Material komponentlarining xato rangi ilovaning `danger` tokeni bilan bir xil bo'lishi
    // kerak — ilgari bu yerda tibbiyot moduli aksenti (pushti) turgan edi.
    error = Danger,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    secondary = PrimaryAccent,
    background = SurfaceDark,
    surface = SurfaceDark,
    onSurface = TextDark,
    onBackground = TextDark,
    error = DangerDark,
    onError = Color.White,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            content = content,
        )
    }
}
