package dev.core.uikit.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Barcha ekranlar uchun umumiy shrift oilasi (Figtree/Google Sans o'rniga). */
val AppFontFamily = FontFamily.Default

/**
 * Matn uslublari — bitta manba.
 *
 * Ilgari har bir ekran `TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, ...)` ni
 * qo'lda yozardi; natijada bir xil ko'rinishdagi matn 13 xil o'lchamga ega bo'lgan edi.
 * Endi `AppType.body` kabi token olinadi, kerak bo'lsa `.copy(color = ...)` qilinadi.
 *
 * Ranglar bu yerda YO'Q — ular [AppPalette] dan keladi, chunki rejimga bog'liq.
 */
@Immutable
object AppType {
    private fun base(
        size: Float,
        weight: FontWeight,
        lineHeight: Float = size * 1.4f,
        letterSpacing: Float = 0f,
    ) = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = size.sp,
        fontWeight = weight,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp,
    )

    /** Ekran sarlavhasi — "Xush kelibsiz", "Profil". */
    val screenTitle = base(24f, FontWeight.Black, lineHeight = 30f, letterSpacing = -0.5f)

    /** Kichikroq sarlavha — dialog, bo'lim boshi. */
    val sectionTitle = base(17f, FontWeight.ExtraBold, lineHeight = 23f)

    /** Karta sarlavhasi. */
    val cardTitle = base(15f, FontWeight.Bold, lineHeight = 20f)

    /** Ekran ostidagi izoh matni. */
    val subtitle = base(13f, FontWeight.Normal, lineHeight = 19f)

    /** Asosiy tana matni. */
    val body = base(14f, FontWeight.Medium, lineHeight = 20f)

    /** Kuchaytirilgan tana matni (qiymatlar, summalar). */
    val bodyStrong = base(14f, FontWeight.SemiBold, lineHeight = 20f)

    /** Maydon tepasidagi yorliq — "Telefon raqami". */
    val fieldLabel = base(12f, FontWeight.Bold, lineHeight = 16f)

    /** Maydon ostidagi ko'rsatma / yordam matni. */
    val hint = base(11.5f, FontWeight.Normal, lineHeight = 15f)

    /** Xato xabari. */
    val error = base(12.5f, FontWeight.Medium, lineHeight = 17f)

    /** Asosiy tugma matni. */
    val button = base(15f, FontWeight.ExtraBold, lineHeight = 20f)

    /** Ikkilamchi / outline tugma matni. */
    val buttonSecondary = base(14f, FontWeight.ExtraBold, lineHeight = 19f)

    /** Tab, chip, kichik harakat matni. */
    val label = base(13f, FontWeight.Bold, lineHeight = 17f)

    /** Eng kichik matn — sana, hisoblagich, meta. */
    val caption = base(11f, FontWeight.Medium, lineHeight = 15f)

    /** Ekran ostidagi havola matni. */
    val link = base(12.5f, FontWeight.Normal, lineHeight = 17f)

    /** Havolaning bosiladigan qismi. */
    val linkAction = base(12.5f, FontWeight.ExtraBold, lineHeight = 17f)

    /** Pastki navigatsiya yorlig'i. */
    val navLabel = base(10f, FontWeight.Bold, lineHeight = 13f)
}

/**
 * Uslub + joriy rejim rangi birga. `AppType.body.on(appPalette.inkMuted)` o'rniga
 * qisqaroq yozuv beradi va rangni unutib qo'yishning oldini oladi.
 */
fun TextStyle.on(color: androidx.compose.ui.graphics.Color): TextStyle = copy(color = color)

/** Joriy palitraning asosiy matn rangi bilan uslub. */
@Composable
@ReadOnlyComposable
fun TextStyle.onInk(): TextStyle = copy(color = appPalette.ink)
