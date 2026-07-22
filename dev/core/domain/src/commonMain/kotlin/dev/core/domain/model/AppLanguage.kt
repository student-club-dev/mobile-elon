package dev.core.domain.model

/**
 * Ilova tili — foydalanuvchi tanlovi (Sozlamalar).
 *
 * [tag] — BCP-47 kodi; `composeResources/values-<tag>` papkasi nomiga mos keladi.
 *
 * Dizayn handoff'ida "Tizim" varianti YO'Q: standart til — o'zbekcha. Ilova O'zbekiston
 * bozori uchun, shuning uchun qurilma tili boshqa bo'lsa ham o'zbekchadan boshlanadi.
 */
enum class AppLanguage(val tag: String) {
    UZ("uz"),
    RU("ru"),
    EN("en");

    companion object {
        val Default = UZ

        /** Saqlangan qiymatdan tiklaydi; noma'lum (yoki eski "SYSTEM") bo'lsa — [Default]. */
        fun fromName(value: String?): AppLanguage =
            entries.firstOrNull { it.name == value } ?: Default
    }
}
