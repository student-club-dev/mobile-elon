package dev.core.uikit.locale

/**
 * Ilova tilini platformaning "joriy til"iga yozadi.
 *
 * Nega shunday, `compose-resources` ga to'g'ridan-to'g'ri aytish o'rniga: `stringResource`
 * tilni `LocalComposeEnvironment` orqali aniqlaydi, lekin u kutubxona ichida `internal`
 * (CMP 1.7 dan 1.11 gacha tekshirildi) — ilova kodidan almashtirib bo'lmaydi. Uning
 * standart implementatsiyasi esa `androidx.compose.ui.text.intl.Locale.current` ni o'qiydi,
 * ya'ni Android'da `LocaleList.getDefault()`, iOS'da `NSLocale.preferredLanguages`. Ikkalasi
 * ham dasturdan yoziladi — biz shularni o'zgartiramiz.
 *
 * MUHIM: bu funksiya o'zi qayta chizishni keltirib chiqarmaydi. Chaqirgandan keyin butun
 * kontentni `key(language) { ... }` ichida qayta yaratish kerak (qarang `dev.shared.App`),
 * aks holda `stringResource` eski qiymatni keshda ushlab qoladi.
 *
 * @param tag BCP-47 kodi ("uz", "ru", "en") yoki `null` — qurilma tiliga qaytish
 */
expect fun applyAppLanguage(tag: String?)
