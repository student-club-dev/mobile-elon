package dev.core.domain.model

/**
 * Ilova mavzusi — foydalanuvchi tanlovi (Sozlamalar).
 *
 * Dizayn handoff'ida "Tizim" varianti YO'Q: faqat Yorug' va Tungi, standart — Yorug'.
 * Sabab: ilova o'z brend ko'rinishiga ega va qurilma rejimiga ergashib rangi kutilmaganda
 * o'zgarib turishi ko'zlanmagan.
 */
enum class ThemeMode {
    LIGHT,
    DARK;

    companion object {
        val Default = LIGHT

        /** Saqlangan qiymatdan tiklaydi; noma'lum (yoki eski "SYSTEM") bo'lsa — [Default]. */
        fun fromName(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: Default
    }
}
