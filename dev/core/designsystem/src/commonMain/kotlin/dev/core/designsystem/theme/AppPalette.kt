package dev.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dev.core.designsystem.theme.LocalDarkTheme
import dev.core.designsystem.theme.BgBottomDark
import dev.core.designsystem.theme.BgBottomLight
import dev.core.designsystem.theme.BgMidDark
import dev.core.designsystem.theme.BgMidLight
import dev.core.designsystem.theme.BgTopDark
import dev.core.designsystem.theme.BgTopLight
import dev.core.designsystem.theme.Ink
import dev.core.designsystem.theme.InkFaint
import dev.core.designsystem.theme.InkMuted
import dev.core.designsystem.theme.LabelInk
import dev.core.designsystem.theme.Primary
import dev.core.designsystem.theme.PrimaryAccent
import dev.core.designsystem.theme.PrimaryDark
import dev.core.designsystem.theme.PrimaryGradientEnd
import dev.core.designsystem.theme.Success
import dev.core.designsystem.theme.SuccessDeep
import dev.core.designsystem.theme.TextDark
import dev.core.designsystem.theme.TextFaintDark
import dev.core.designsystem.theme.TextMutedDark

/**
 * Auth ekranlariga xos "liquid glass" tokenlar — Material sxemasi ushlab turmaydigan
 * gradient, shisha (glass) yuza va blob ranglar. Yorug'/qorong'i rejim bo'yicha tanlanadi.
 */
@Immutable
data class AppPalette(
    val dark: Boolean,
    val ink: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val label: Color,
    val primary: Color,
    val primaryGradient: List<Color>,
    val bgGradient: List<Color>,
    val blobPrimary: Color,
    val blobCyan: Color,
    val glass: Color,
    val glassStrong: Color,
    val border: Color,
    val borderStrong: Color,
    val tabTrack: Color,
    val fieldBg: Color,
    val fieldFocusGlow: Color,
    val chevron: Color,
    val success: Color,
    val successDeep: Color,
    val successBg: Color,
    val onPrimary: Color,
) {
    /** 135° primary gradient — tugmalar va logo uchun. */
    val primaryBrush: Brush get() = Brush.linearGradient(primaryGradient)

    /** 168° fon gradienti. */
    val bgBrush: Brush get() = Brush.linearGradient(bgGradient)
}

private val LightAppPalette = AppPalette(
    dark = false,
    ink = Ink,
    inkMuted = InkMuted,
    inkFaint = InkFaint,
    label = LabelInk,
    primary = Primary,
    primaryGradient = listOf(Primary, PrimaryGradientEnd),
    bgGradient = listOf(BgTopLight, BgMidLight, BgBottomLight),
    blobPrimary = Primary.copy(alpha = 0.28f),
    blobCyan = Color(0xFF22D3EE).copy(alpha = 0.22f),
    glass = Color.White.copy(alpha = 0.82f),
    glassStrong = Color.White.copy(alpha = 0.90f),
    border = Primary.copy(alpha = 0.16f),
    borderStrong = Primary,
    tabTrack = Primary.copy(alpha = 0.09f),
    fieldBg = Color.White.copy(alpha = 0.85f),
    fieldFocusGlow = Primary.copy(alpha = 0.10f),
    chevron = InkFaint,
    success = Success,
    successDeep = SuccessDeep,
    successBg = SuccessDeep.copy(alpha = 0.06f),
    onPrimary = Color.White,
)

private val DarkAppPalette = AppPalette(
    dark = true,
    ink = TextDark,
    inkMuted = TextMutedDark,
    inkFaint = TextFaintDark,
    label = Color(0xFFB4AECD),
    primary = PrimaryDark,
    primaryGradient = listOf(PrimaryDark, PrimaryAccent),
    bgGradient = listOf(BgTopDark, BgMidDark, BgBottomDark),
    blobPrimary = PrimaryDark.copy(alpha = 0.40f),
    blobCyan = Color(0xFF22D3EE).copy(alpha = 0.20f),
    glass = Color.White.copy(alpha = 0.06f),
    glassStrong = Color.White.copy(alpha = 0.08f),
    border = Color.White.copy(alpha = 0.10f),
    borderStrong = PrimaryDark,
    tabTrack = Color.White.copy(alpha = 0.06f),
    fieldBg = Color.White.copy(alpha = 0.06f),
    fieldFocusGlow = PrimaryDark.copy(alpha = 0.18f),
    chevron = TextFaintDark,
    success = Success,
    successDeep = Success,
    successBg = Success.copy(alpha = 0.10f),
    onPrimary = Color.White,
)

/** Joriy rejimga mos auth palitrasi (foydalanuvchi mavzu tanloviga ergashadi, aks holda tizim). */
val appPalette: AppPalette
    @Composable
    @ReadOnlyComposable
    get() {
        val dark = LocalDarkTheme.current ?: isSystemInDarkTheme()
        return if (dark) DarkAppPalette else LightAppPalette
    }
