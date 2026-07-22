package dev.core.uikit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Ilovaning dizayn tokenlari (`design_handoff_studentclub_elonuz`).
 *
 * Vizual til: **oq kartalar och ko'k-kulrang fon ustida**, TBC ko'k aksent, yumshoq va uzoq
 * soyalar. Ekranlarda faqat shu tokenlar ishlatiladi — `Color(0xFF...)` yozilmaydi. Yangi rang
 * kerak bo'lsa avval shu yerga token qo'shiladi.
 */
@Immutable
data class AppPalette(
    val dark: Boolean,
    // Matn
    val ink: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val inactive: Color,
    // Brend
    val primary: Color,
    val primaryGradient: List<Color>,
    val headerGradient: List<Color>,
    // Yuzalar
    val screenBg: Color,
    val bgGradient: List<Color>,
    val card: Color,
    /** Ikonka nishoni va chip foni — ochiq ko'k. */
    val accentBg: Color,
    val divider: Color,
    /** Karta ustidagi maydon foni (input). */
    val fieldBg: Color,
    /** OQ panel ichidagi maydon yo'lakchasi — chat kiritish paneli kabi. */
    val fieldTrack: Color,
    // Holatlar
    val success: Color,
    val danger: Color,
    val dangerBg: Color,
    val warning: Color,
    val successBg: Color,
    val warningBg: Color,
    // Kontent
    val onPrimary: Color,
    /** Modal ostidagi qoraytiruvchi qatlam. */
    val scrim: Color,
    // Kategoriya aksentlari
    val accentFood: Color,
    val accentGame: Color,
    val accentClothing: Color,
    val accentStudy: Color,
    val accentCinema: Color,
    val accentBeauty: Color,
    val accentBarber: Color,
) {
    /** 135° brend gradienti — CTA tugmalar, FAB, faol tab. */
    val primaryBrush: Brush get() = Brush.linearGradient(primaryGradient)

    /** Ekran foni — yuqori chapdan ochiq ko'k yorug'lik. */
    val bgBrush: Brush get() = Brush.linearGradient(bgGradient)

    /** Ekran tepasidagi gradientli sarlavha bloki. */
    val headerBrush: Brush get() = Brush.linearGradient(headerGradient)
}

private val LightAppPalette = AppPalette(
    dark = false,
    ink = Ink,
    inkMuted = InkMuted,
    inkFaint = InkFaint,
    inactive = Inactive,
    primary = Primary,
    primaryGradient = listOf(PrimaryLight, PrimaryDeep),
    headerGradient = listOf(PrimaryLight, Primary, PrimaryDeep),
    screenBg = ScreenBg,
    bgGradient = listOf(BgTopLight, BgMidLight, BgBottomLight),
    card = CardBg,
    accentBg = AccentBg,
    divider = Divider,
    fieldBg = CardBg,
    fieldTrack = FieldTrack,
    success = Success,
    danger = Danger,
    dangerBg = DangerBg,
    warning = Warning,
    successBg = Success.copy(alpha = 0.10f),
    warningBg = Warning.copy(alpha = 0.14f),
    onPrimary = Color.White,
    scrim = Obsidian.copy(alpha = 0.45f),
    accentFood = AccentFood,
    accentGame = AccentGame,
    accentClothing = AccentClothing,
    accentStudy = AccentStudy,
    accentCinema = AccentCinema,
    accentBeauty = AccentBeauty,
    accentBarber = AccentBarber,
)

private val DarkAppPalette = AppPalette(
    dark = true,
    ink = InkDark,
    inkMuted = InkMutedDark,
    inkFaint = InkFaintDark,
    inactive = InkFaintDark,
    // Quyuq fonda #00ADEE xiralashadi — bir pog'ona yorqinroq ko'k olinadi.
    primary = PrimaryOnDark,
    primaryGradient = listOf(PrimaryLight, Primary),
    headerGradient = listOf(Color(0xFF0E6A9B), Color(0xFF0A5A85), Color(0xFF083F5E)),
    screenBg = ScreenBgDark,
    bgGradient = listOf(BgTopDark, BgMidDark, BgBottomDark),
    card = CardBgDark,
    accentBg = AccentBgDark,
    divider = DividerDark,
    fieldBg = CardBgDark,
    fieldTrack = FieldTrackDark,
    success = Success,
    danger = Color(0xFFF06A82),
    dangerBg = DangerBgDark,
    warning = Warning,
    successBg = SuccessBgDark,
    warningBg = WarningBgDark,
    onPrimary = Color.White,
    scrim = Color.Black.copy(alpha = 0.60f),
    accentFood = AccentFood,
    accentGame = AccentGame,
    accentClothing = AccentClothing,
    accentStudy = AccentStudy,
    accentCinema = AccentCinema,
    accentBeauty = AccentBeauty,
    accentBarber = AccentBarber,
)

/** Joriy rejimga mos palitra (foydalanuvchi mavzu tanloviga ergashadi, aks holda tizim). */
val appPalette: AppPalette
    @Composable
    @ReadOnlyComposable
    get() {
        val dark = LocalDarkTheme.current ?: isSystemInDarkTheme()
        return if (dark) DarkAppPalette else LightAppPalette
    }
