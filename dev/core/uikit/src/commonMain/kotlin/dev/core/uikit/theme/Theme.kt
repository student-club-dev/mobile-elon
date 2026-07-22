package dev.core.uikit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Joriy dark rejim — foydalanuvchi tanlovi bilan boshqariladi (Sozlamalar → mavzu).
 * [AppTheme] o'rnatadi; [appPalette] shu qiymatga ergashadi.
 */
val LocalDarkTheme = staticCompositionLocalOf<Boolean?> { null }

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PrimaryDeep,
    background = ScreenBg,
    surface = CardBg,
    onSurface = Ink,
    onBackground = Ink,
    error = Danger,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryOnDark,
    onPrimary = Color.White,
    secondary = PrimaryLight,
    background = ScreenBgDark,
    surface = CardBgDark,
    onSurface = InkDark,
    onBackground = InkDark,
    error = Color(0xFFF06A82),
    onError = Color.White,
)

/**
 * Ilova mavzusi.
 *
 * Shrift shu yerda bir marta yuklanadi va [AppTypography] ga kiritiladi. Uslub tokenlari
 * `LocalTextStyle` orqali meros olinmaydi: `Text(style = ...)` ga aniq uslub berilganda
 * Material uni butunlay almashtiradi va oila yo'qoladi. Shuning uchun oila har bir tokenning
 * ichida bo'ladi.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val fontFamily = appFontFamily()
    val typography = remember(fontFamily) { AppTypography(fontFamily) }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppTypography provides typography,
    ) {
        MaterialTheme(colorScheme = colors) {
            // Uslub berilmagan `Text` ham brend shriftida chiqsin.
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = fontFamily),
                content = content,
            )
        }
    }
}
