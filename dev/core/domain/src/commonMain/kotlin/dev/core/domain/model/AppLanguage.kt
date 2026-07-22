package dev.core.domain.model

/**
 * Ilova tili — foydalanuvchi tanlovi (Sozlamalar).
 *
 * [tag] — BCP-47 til kodi; `composeResources/values-<tag>` papkasi nomiga mos keladi.
 * [SYSTEM] da `tag` `null`: qurilma tili ishlatiladi va tanlangan til saqlanmaydi.
 */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    UZ("uz"),
    RU("ru"),
    EN("en");

    companion object {
        /** Saqlangan qiymatdan tiklaydi; noma'lum bo'lsa [SYSTEM]. */
        fun fromName(value: String?): AppLanguage =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}
