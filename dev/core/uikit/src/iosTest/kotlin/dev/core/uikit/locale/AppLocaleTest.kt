package dev.core.uikit.locale

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [applyAppLanguage] iOS'da HAQIQATAN ishlaydimi — ya'ni `NSUserDefaults` ga yozilgan
 * `AppleLanguages` qiymatini `NSLocale.preferredLanguages` **o'sha zahoti** qaytaradimi.
 *
 * Bu shunchaki mayda tafsilot emas: Compose'ning `Locale.current` iOS'da aynan
 * `NSLocale.preferredLanguages` ni o'qiydi, `stringResource` esa undan tilni oladi. Agar
 * qiymat jarayon boshida keshlansa, ilova ichida til almashtirish faqat qayta ishga
 * tushirilgandan keyin ishlaydi — va buni foydalanuvchiga aytish kerak bo'ladi.
 */
class AppLocaleTest {

    @AfterTest
    fun reset() {
        applyAppLanguage(null)
    }

    @Test
    fun applyingLanguageUpdatesPreferredLanguagesImmediately() {
        applyAppLanguage("ru")
        assertEquals("ru", NSLocale.preferredLanguages.firstOrNull())

        applyAppLanguage("en")
        assertEquals("en", NSLocale.preferredLanguages.firstOrNull())
    }

    @Test
    fun clearingLanguageRestoresDeviceLanguage() {
        applyAppLanguage("ru")
        applyAppLanguage(null)

        // Qurilma tili qaytdi — bizning "ru" qiymatimiz endi birinchi o'rinda emas.
        val preferred = NSLocale.preferredLanguages.firstOrNull()
        assertTrue(preferred != "ru", "Qurilma tiliga qaytmadi, hali ham: $preferred")
    }
}
