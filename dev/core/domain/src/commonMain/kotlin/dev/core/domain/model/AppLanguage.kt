package dev.core.domain.model

/**
 * Ilova tili — foydalanuvchi tanlovi (Sozlamalar).
 *
 * [tag] — BCP-47 kodi; `composeResources/values-<tag>` papkasi nomiga mos keladi.
 *
 * Dizayn handoff'ida "Tizim" varianti YO'Q: standart til — inglizcha. Qurilma tili qanday
 * bo'lishidan qat'i nazar ilova inglizchadan boshlanadi, foydalanuvchi Sozlamalarda o'zgartiradi.
 */
enum class AppLanguage(val tag: String) {
    UZ("uz"),
    RU("ru"),
    EN("en");

    companion object {
        val Default = EN

        /** Saqlangan qiymatdan tiklaydi; noma'lum (yoki eski "SYSTEM") bo'lsa — [Default]. */
        fun fromName(value: String?): AppLanguage =
            entries.firstOrNull { it.name == value } ?: Default
    }
}
