package dev.core.uikit.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.plusjakartasans_400
import dev.core.uikit.resources.plusjakartasans_500
import dev.core.uikit.resources.plusjakartasans_600
import dev.core.uikit.resources.plusjakartasans_700
import dev.core.uikit.resources.plusjakartasans_800
import org.jetbrains.compose.resources.Font

/**
 * Ilova shrifti — **Plus Jakarta Sans** (handoff talabi).
 *
 * Shrift ilova ichiga joylangan (`composeResources/font/`), tizimdan olinmaydi: na Android'da,
 * na iOS'da bu shrift oldindan mavjud emas. Fayllar variable shriftdan ajratib olingan statik
 * og'irliklar — variable o'q Compose iOS'da ishonchli ishlamaydi.
 */
@Composable
fun appFontFamily(): FontFamily = FontFamily(
    Font(Res.font.plusjakartasans_400, weight = FontWeight.Normal),
    Font(Res.font.plusjakartasans_500, weight = FontWeight.Medium),
    Font(Res.font.plusjakartasans_600, weight = FontWeight.SemiBold),
    Font(Res.font.plusjakartasans_700, weight = FontWeight.Bold),
    Font(Res.font.plusjakartasans_800, weight = FontWeight.ExtraBold),
)

/**
 * Matn uslublari — bitta manba (handoff "Tipografiya" bo'limi).
 *
 * Nega obyekt emas, klass: uslub `Text(style = ...)` ga uzatilganda Material `LocalTextStyle`
 * ni E'TIBORSIZ qoldiradi va shrift oilasi yo'qoladi. Shuning uchun oila har bir uslubga
 * qurilish paytida kiritiladi, tayyor to'plam esa [LocalAppTypography] orqali beriladi.
 *
 * Rang tokenlarda YO'Q — u [AppPalette] dan keladi (`.copy(color = palette.ink)`).
 */
@Immutable
class AppTypography(private val fontFamily: FontFamily) {

    private fun base(
        size: Float,
        weight: FontWeight,
        lineHeight: Float = size * 1.35f,
        letterSpacing: Float = 0f,
    ) = TextStyle(
        fontFamily = fontFamily,
        fontSize = size.sp,
        fontWeight = weight,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp,
    )

    // ---- Sarlavhalar ----

    /** Ekran sarlavhasi — "Bizneslarim". */
    val screenTitle = base(30f, FontWeight.ExtraBold, lineHeight = 36f, letterSpacing = -0.6f)

    /** Ichki ekran topbar sarlavhasi. */
    val topBarTitle = base(21f, FontWeight.ExtraBold, lineHeight = 27f, letterSpacing = -0.3f)

    /** Bo'lim sarlavhasi. */
    val sectionTitle = base(19f, FontWeight.ExtraBold, lineHeight = 25f)

    /** Modal sheet sarlavhasi. */
    val sheetTitle = base(18f, FontWeight.ExtraBold, lineHeight = 24f)

    /** Karta sarlavhasi. */
    val cardTitle = base(17f, FontWeight.ExtraBold, lineHeight = 22f)

    /** Ro'yxat qatori sarlavhasi. */
    val rowTitle = base(15.5f, FontWeight.Bold, lineHeight = 21f)

    // ---- Tana matni ----

    /** Asosiy matn / input qiymati. */
    val body = base(15f, FontWeight.Medium, lineHeight = 21f)

    /** Kuchaytirilgan matn (qiymatlar, summalar). */
    val bodyStrong = base(15f, FontWeight.Bold, lineHeight = 21f)

    /** Ekran ostidagi izoh. */
    val subtitle = base(13.5f, FontWeight.Medium, lineHeight = 19f)

    /** Ikkinchi darajali matn. */
    val secondary = base(13f, FontWeight.SemiBold, lineHeight = 18f)

    /** Maydon ustidagi yorliq. */
    val fieldLabel = base(14f, FontWeight.Bold, lineHeight = 19f)

    /** Bo'lim ustidagi kichik yorliq ("Hisob", "Mavzu"). */
    val groupLabel = base(12.5f, FontWeight.Bold, lineHeight = 17f)

    /** Maydon ostidagi ko'rsatma. */
    val hint = base(12.5f, FontWeight.Medium, lineHeight = 17f)

    /** Xato xabari. */
    val error = base(13f, FontWeight.SemiBold, lineHeight = 18f)

    /** Eng kichik matn — sana, hisoblagich, meta. */
    val caption = base(11.5f, FontWeight.SemiBold, lineHeight = 15f)

    // ---- Harakatlar ----

    /** CTA tugma matni. */
    val button = base(15f, FontWeight.ExtraBold, lineHeight = 20f)

    /** Ikkilamchi tugma matni. */
    val buttonSecondary = base(14.5f, FontWeight.Bold, lineHeight = 20f)

    /** Chip, tab, kichik harakat. */
    val label = base(14f, FontWeight.Bold, lineHeight = 19f)

    /** Segment (Yorug'/Tungi, tillar). */
    val segment = base(14f, FontWeight.ExtraBold, lineHeight = 19f)

    /** Havola matni. */
    val link = base(13f, FontWeight.Medium, lineHeight = 18f)

    /** Havolaning bosiladigan qismi. */
    val linkAction = base(13f, FontWeight.ExtraBold, lineHeight = 18f)

    // ---- Maxsus ----

    /** Katta raqam (statistika kartasi). */
    val statValue = base(20f, FontWeight.ExtraBold, lineHeight = 25f)

    /**
     * Ekran ustidagi katta harfli mikro-yorliq — "BIZNES MARKAZI".
     * Matnni UPPERCASE qilib yozish kerak, Compose o'zi o'zgartirmaydi.
     */
    val microLabel = base(12.5f, FontWeight.ExtraBold, lineHeight = 16f, letterSpacing = 2f)

    /** Pastki navigatsiya yorlig'i. */
    val navLabel = base(10.5f, FontWeight.Bold, lineHeight = 14f)
}

/**
 * Tayyor tipografiya to'plami. [AppTheme] shriftni yuklab, shu yerga qo'yadi — shuning uchun
 * ekranlar `AppType.body` deb yozganda uslub allaqachon to'g'ri oila bilan keladi.
 */
val LocalAppTypography = staticCompositionLocalOf { AppTypography(FontFamily.Default) }

/** Joriy tipografiya — `AppType.cardTitle` ko'rinishida ishlatiladi. */
val AppType: AppTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTypography.current
