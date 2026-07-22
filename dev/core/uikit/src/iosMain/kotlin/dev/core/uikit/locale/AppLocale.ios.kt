package dev.core.uikit.locale

import platform.Foundation.NSUserDefaults

/**
 * iOS'da "afzal ko'rilgan tillar" ro'yxati `NSUserDefaults` dagi `AppleLanguages` kalitida
 * turadi va `NSLocale.preferredLanguages` shundan o'qiydi — Compose ham aynan shuni ishlatadi.
 *
 * Eslatma: tizimning o'z komponentlari (klaviatura, tizim dialoglari, `Bundle.main` tarjimalari)
 * bu qiymatni ilova ishga tushganda o'qiydi, shuning uchun ular faqat qayta ishga tushirilgach
 * yangilanadi. Ilovaning O'Z matnlari `compose-resources` dan keladi va darrov almashadi.
 */
private const val APPLE_LANGUAGES = "AppleLanguages"

actual fun applyAppLanguage(tag: String?) {
    val defaults = NSUserDefaults.standardUserDefaults
    if (tag == null) {
        // Qurilma tiliga qaytish — o'z qiymatimizni olib tashlaymiz.
        defaults.removeObjectForKey(APPLE_LANGUAGES)
    } else {
        defaults.setObject(listOf(tag), forKey = APPLE_LANGUAGES)
    }
    defaults.synchronize()
}
