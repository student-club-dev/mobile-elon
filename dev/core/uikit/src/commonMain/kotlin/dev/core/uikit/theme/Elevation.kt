package dev.core.uikit.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Soyalar (`design_handoff_studentclub_elonuz`).
 *
 * Handoff soyalari CSS'da uzun va yumshoq: `0 14px 30px -22px rgba(15,42,67,0.35)` — ya'ni
 * katta blur va katta MANFIY spread, natijada soya elementning tagida yig'ilib, yonlariga
 * yoyilmaydi. Compose'da spread yo'q, shuning uchun blur o'rniga kichikroq elevation va
 * bo'yalgan `spotColor` ishlatiladi — vizual natija shunga yaqin bo'ladi.
 *
 * Qorong'i rejimda soya deyarli ko'rinmaydi (fon allaqachon quyuq), shuning uchun u yerda
 * elevation pasaytiriladi — aks holda kartalar atrofida iflos halqa paydo bo'ladi.
 */
private val ShadowTint = Color(0xFF0F2A43)

/** Asosiy karta soyasi — oq karta och fon ustida. */
@Composable
fun Modifier.cardShadow(shape: RoundedCornerShape = AppRadius.card): Modifier {
    val palette = appPalette
    if (palette.dark) return this
    return shadow(
        elevation = 14.dp,
        shape = shape,
        spotColor = ShadowTint.copy(alpha = 0.35f),
        ambientColor = ShadowTint.copy(alpha = 0.20f),
    )
}

/** Yengilroq soya — ro'yxat qatorlari va kichik tugmalar. */
@Composable
fun Modifier.rowShadow(shape: RoundedCornerShape = AppRadius.row): Modifier {
    val palette = appPalette
    if (palette.dark) return this
    return shadow(
        elevation = 8.dp,
        shape = shape,
        spotColor = ShadowTint.copy(alpha = 0.30f),
        ambientColor = ShadowTint.copy(alpha = 0.16f),
    )
}

/**
 * CTA soyasi — brend rangida yorqin "yorug'lik". Qorong'ida ham qoladi, chunki bu
 * dekorativ emas: tugmani fondan ajratib turadi.
 */
@Composable
fun Modifier.ctaShadow(shape: RoundedCornerShape = AppRadius.button): Modifier {
    val palette = appPalette
    return shadow(
        elevation = 16.dp,
        shape = shape,
        spotColor = palette.primary.copy(alpha = 0.65f),
        ambientColor = palette.primary.copy(alpha = 0.40f),
    )
}
