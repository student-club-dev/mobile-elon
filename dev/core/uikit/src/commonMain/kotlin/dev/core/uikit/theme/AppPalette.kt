package dev.core.uikit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Ilovaning dizayn tokenlari — Material sxemasi ushlab turmaydigan gradient, shisha (glass)
 * yuza, blob va holat (danger/warning) ranglari. Har bir token yorug' va qorong'i rejim
 * uchun ALOHIDA qiymatga ega.
 *
 * Ekranlarda faqat shu tokenlar ishlatiladi — `Color(0xFF...)` yozilmaydi. Yangi rang kerak
 * bo'lsa avval shu yerga token qo'shiladi, keyin ekranda ishlatiladi.
 */
@Immutable
data class AppPalette(
    val dark: Boolean,
    // Matn
    val ink: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val label: Color,
    // Brend
    val primary: Color,
    val primaryGradient: List<Color>,
    val headerGradient: List<Color>,
    // Fon va yuzalar
    val bgGradient: List<Color>,
    val blobPrimary: Color,
    val blobCyan: Color,
    val glass: Color,
    val glassStrong: Color,
    val barSurface: Color,
    val border: Color,
    val borderStrong: Color,
    val tabTrack: Color,
    val chipTrack: Color,
    val fieldBg: Color,
    val fieldFocusGlow: Color,
    val chevron: Color,
    // Holatlar
    val success: Color,
    val successDeep: Color,
    val successBg: Color,
    val danger: Color,
    val dangerBg: Color,
    val warning: Color,
    val warningBg: Color,
    val amber: Color,
    val badge: Color,
    // Kontent
    val onPrimary: Color,
    val onGlass: Color,
    /**
     * Rasm ustidagi quyuq qoplama — thumbnail burchagidagi "o'chirish" tugmasi kabi.
     * Fon rasm bo'lgani uchun qiymat ikkala rejimda ham qora qoladi, faqat quyuqligi farq qiladi.
     */
    val scrim: Color,
    // Modul aksentlari (bildirishnoma turlari, rol kartalari)
    val moduleFood: Color,
    val moduleStudy: Color,
    val moduleEmployer: Color,
    val moduleHousing: Color,
    val moduleMedical: Color,
) {
    /** 135° primary gradient — tugmalar va logo uchun. */
    val primaryBrush: Brush get() = Brush.linearGradient(primaryGradient)

    /** 168° fon gradienti. */
    val bgBrush: Brush get() = Brush.linearGradient(bgGradient)

    /** Ekran yuqorisidagi sarlavha bloki gradienti. */
    val headerBrush: Brush get() = Brush.linearGradient(headerGradient)
}

private val LightAppPalette = AppPalette(
    dark = false,
    ink = Ink,
    inkMuted = InkMuted,
    inkFaint = InkFaint,
    label = LabelInk,
    primary = Primary,
    primaryGradient = listOf(Primary, PrimaryGradientEnd),
    headerGradient = listOf(HeaderStart, HeaderMid, HeaderEnd),
    bgGradient = listOf(BgTopLight, BgMidLight, BgBottomLight),
    blobPrimary = Primary.copy(alpha = 0.28f),
    blobCyan = Cyan.copy(alpha = 0.22f),
    glass = Color.White.copy(alpha = 0.82f),
    glassStrong = Color.White.copy(alpha = 0.90f),
    barSurface = BarSurfaceLight,
    border = Primary.copy(alpha = 0.16f),
    borderStrong = Primary,
    tabTrack = Primary.copy(alpha = 0.09f),
    chipTrack = ChipTrackLight,
    fieldBg = Color.White.copy(alpha = 0.85f),
    fieldFocusGlow = Primary.copy(alpha = 0.10f),
    chevron = InkFaint,
    success = Success,
    successDeep = SuccessDeep,
    successBg = SuccessDeep.copy(alpha = 0.06f),
    danger = Danger,
    dangerBg = Danger.copy(alpha = 0.10f),
    warning = Warning,
    warningBg = WarningBgLight,
    amber = Amber,
    badge = BadgeDot,
    onPrimary = Color.White,
    onGlass = OnGlassLight,
    scrim = ScrimBlack.copy(alpha = 0.55f),
    moduleFood = ModuleFood,
    moduleStudy = ModuleStudy,
    moduleEmployer = ModuleEmployer,
    moduleHousing = ModuleHousing,
    moduleMedical = ModuleMedical,
)

private val DarkAppPalette = AppPalette(
    dark = true,
    ink = TextDark,
    inkMuted = TextMutedDark,
    inkFaint = TextFaintDark,
    label = LabelDark,
    primary = PrimaryDark,
    primaryGradient = listOf(PrimaryDark, PrimaryAccent),
    headerGradient = listOf(HeaderStartDark, HeaderMidDark, HeaderEndDark),
    bgGradient = listOf(BgTopDark, BgMidDark, BgBottomDark),
    blobPrimary = PrimaryDark.copy(alpha = 0.40f),
    blobCyan = Cyan.copy(alpha = 0.20f),
    glass = Color.White.copy(alpha = 0.06f),
    glassStrong = Color.White.copy(alpha = 0.08f),
    barSurface = BarSurfaceDark,
    border = Color.White.copy(alpha = 0.10f),
    borderStrong = PrimaryDark,
    tabTrack = Color.White.copy(alpha = 0.06f),
    chipTrack = Color.White.copy(alpha = 0.04f),
    fieldBg = Color.White.copy(alpha = 0.06f),
    fieldFocusGlow = PrimaryDark.copy(alpha = 0.18f),
    chevron = TextFaintDark,
    success = Success,
    successDeep = Success,
    successBg = Success.copy(alpha = 0.10f),
    danger = DangerDark,
    dangerBg = DangerDark.copy(alpha = 0.14f),
    warning = WarningDark,
    warningBg = WarningDark.copy(alpha = 0.14f),
    amber = Amber,
    badge = BadgeDot,
    onPrimary = Color.White,
    // Qorong'ida shisha yuza ustidagi matn oddiy ink bilan bir xil — alohida rang shart emas.
    onGlass = TextDark,
    scrim = ScrimBlack.copy(alpha = 0.62f),
    moduleFood = ModuleFood,
    moduleStudy = ModuleStudy,
    moduleEmployer = ModuleEmployer,
    moduleHousing = ModuleHousing,
    moduleMedical = ModuleMedical,
)

/** Joriy rejimga mos palitra (foydalanuvchi mavzu tanloviga ergashadi, aks holda tizim). */
val appPalette: AppPalette
    @Composable
    @ReadOnlyComposable
    get() {
        val dark = LocalDarkTheme.current ?: isSystemInDarkTheme()
        return if (dark) DarkAppPalette else LightAppPalette
    }
