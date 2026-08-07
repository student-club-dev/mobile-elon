package dev.core.uikit.util

/**
 * Huquqiy hujjatlarning **ochiq veb** manzillari.
 *
 * Ilova ichida matn brauzersiz ko'rsatiladi (`LegalSheet` + `composeResources/files/legal/`),
 * shuning uchun bu manzillar kodda ishlatilmaydi. Ular Google Play ilova ro'yxati va Google
 * OAuth tekshiruvi uchun kerak: ikkalasi ham Maxfiylik siyosatiga ochiq URL talab qiladi.
 *
 * Maxfiylik siyosati va Xavfsizlik qoidalari **bitta PDF** ichida, uch tilda alohida
 * fayl sifatida joylashtirilgan.
 */
object LegalLinks {
    /** Maxfiylik siyosati + Xavfsizlik qoidalari (PDF) — o'zbekcha. */
    const val PRIVACY_UZ = "https://pixeldrain.com/u/PZT5gcfz"

    /** Maxfiylik siyosati + Xavfsizlik qoidalari (PDF) — ruscha. */
    const val PRIVACY_RU = "https://pixeldrain.com/u/DWSoZ5x5"

    /** Maxfiylik siyosati + Xavfsizlik qoidalari (PDF) — inglizcha. */
    const val PRIVACY_EN = "https://pixeldrain.com/u/TvXA2ayq"

    /** Play Console va OAuth tekshiruvi uchun asosiy manzil — o'zbekcha nusxa. */
    const val PRIVACY = PRIVACY_UZ

    /** Til kodiga mos nusxa; noma'lum til uchun o'zbekchasi ([LegalSheet] bilan bir xil mantiq). */
    fun privacy(language: String): String = when (language.lowercase()) {
        "ru" -> PRIVACY_RU
        "en" -> PRIVACY_EN
        else -> PRIVACY_UZ
    }

    // TODO: Foydalanish shartlari alohida hujjat — hali veb-manzilga joylanmagan.
    const val TERMS = "https://qsbusiness.uz/terms"
}
