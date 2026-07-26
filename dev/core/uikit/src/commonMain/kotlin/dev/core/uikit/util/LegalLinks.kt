package dev.core.uikit.util

/**
 * Huquqiy hujjatlarning **ochiq veb** manzillari.
 *
 * Ilova ichida matn brauzersiz ko'rsatiladi (`LegalSheet` + `composeResources/files/legal/`),
 * shuning uchun bu manzillar kodda ishlatilmaydi. Ular Google Play ilova ro'yxati va Google
 * OAuth tekshiruvi uchun kerak: ikkalasi ham Maxfiylik siyosatiga ochiq URL talab qiladi.
 * Veb-sahifani `files/legal` katalogidagi Markdown matnlardan tayyorlab, quyidagi
 * manzillarni yangilang.
 */
object LegalLinks {
    // TODO: hujjatlar veb-sahifada e'lon qilingach, haqiqiy manzillarga almashtirilsin.
    const val TERMS = "https://qsbusiness.uz/terms"
    const val PRIVACY = "https://qsbusiness.uz/privacy"
}
