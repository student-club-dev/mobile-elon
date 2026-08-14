package dev.core.uikit.locale

/**
 * Til almashganda foydalanuvchini **o'sha ekranда** qoldirish uchun kichik ko'prik.
 *
 * Nega kerak: `stringResource` tanlangan tilni kompozitsiya ichida keshlaydi va uni
 * bekor qilishning yagona yo'li — butun daraxtni `key(language) { ... }` bilan qayta
 * yaratish (batafsil: [applyAppLanguage] izohi). Qayta yaratilganда esa `NavHost` o'zining
 * boshlang'ich ekraniga tushadi: foydalanuvchi Sozlamalarда tilni almashtirsa, ilova uni
 * "Bizneslarim" ro'yxatiga tashlab yuborardi va qolgan sozlamalarni o'zgartirish uchun
 * qaytadan kirishga to'g'ri kelardi.
 *
 * Ishlash tartibi:
 * 1. Karkas (`BusinessShell`) joriy marshrutni [lastRoute] ga yozib boradi.
 * 2. Til o'zgarganда ildiz (`dev.shared.App`) [armed] ni yoqadi.
 * 3. Karkas qayta yaratilganда [consumeRoute] bilan marshrutni oladi va o'sha yerga qaytadi.
 *
 * Oddiy `object` — holat kompozitsiyadan TASHQARIDA turishi kerak, aks holda u ham
 * `key()` bilan birga yo'q bo'lib ketardi.
 */
object LocaleRestore {

    /** Karkasdagi joriy marshrut. */
    var lastRoute: String? = null

    /** Til hozirgina almashdimi — faqat shunda marshrut tiklanadi. */
    private var armed: Boolean = false

    /** Ildiz til o'zgarganini bildiradi. */
    fun armForLanguageChange() {
        armed = true
    }

    /**
     * Tiklanishi kerak bo'lgan marshrutni qaytaradi va bayroqni o'chiradi.
     *
     * `null` — tiklash kerak emas: bu odatiy ochilish (ilova ishga tushdi yoki foydalanuvchi
     * chiqib qayta kirdi), va unda boshlang'ich ekran to'g'ri.
     */
    fun consumeRoute(): String? {
        if (!armed) return null
        armed = false
        return lastRoute
    }
}
