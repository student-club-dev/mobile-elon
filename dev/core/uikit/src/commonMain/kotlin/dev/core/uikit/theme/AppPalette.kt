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
    // -----------------------------------------------------------------------
    // Eski nomlar — yangi dizaynga moslashtirilgan
    //
    // Ilova binafsha "liquid glass" tilida yozilgan edi va 400 dan ortiq joyda shu nomlar
    // ishlatiladi. Ularni yangi qiymatlarga bog'lab qo'yamiz: ekranlar o'zgarmasdan yangi
    // ko'rinishga o'tadi. Yangi kod to'g'ridan-to'g'ri yuqoridagi tokenlarni ishlatsin.
    // -----------------------------------------------------------------------

    /** Ilgari yarim shaffof "shisha" yuza edi — endi oq karta. */
    val glass: Color get() = card
    val glassStrong: Color get() = card

    /** Ilgari nozik binafsha chegara — endi ochiq kulrang ajratgich. */
    val border: Color get() = divider
    val borderStrong: Color get() = primary

    /** Ilgari ko'tarilgan yuza (pastki panel) — endi oddiy karta. */
    val barSurface: Color get() = card

    /** Tanlanmagan tab/chip foni. */
    val tabTrack: Color get() = accentBg
    val chipTrack: Color get() = accentBg

    /** Shisha yuza ustidagi matn. */
    val onGlass: Color get() = ink

    /** Maydon yorlig'i va chevron — ikkinchi darajali matn rangi. */
    val label: Color get() = inkMuted
    val chevron: Color get() = inkMuted

    /** Fokusdagi maydon yaltirashi. */
    val fieldFocusGlow: Color get() = primary.copy(alpha = 0.15f)

    /** Ilgari to'q yashil edi — yangi palitrada bitta success rangi bor. */
    val successDeep: Color get() = success

    /** Reyting yulduzi va o'qilmagan nuqta. */
    val amber: Color get() = warning
    val badge: Color get() = danger

    /**
     * Eski fon "bloblari" — yangi dizaynda dekorativ dog'lar yo'q, fon toza gradient.
     * Ular hali chizilayotgan joylarda ko'rinmas bo'lib qoladi.
     */
    val blobPrimary: Color get() = Color.Transparent
    val blobCyan: Color get() = Color.Transparent

    /** Eski modul aksentlari — yangi kategoriya ranglariga bog'landi. */
    val moduleFood: Color get() = accentFood
    val moduleStudy: Color get() = accentStudy
    val moduleEmployer: Color get() = warning
    val moduleHousing: Color get() = success
    val moduleMedical: Color get() = accentBeauty

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
