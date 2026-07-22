package dev.core.uikit.locale

import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Qurilmaning "boshlang'ich" tili — ilova ishga tushganda bir marta eslab qolinadi.
 * [applyAppLanguage] `null` bilan chaqirilganda (SYSTEM tanlansa) shunga qaytamiz;
 * aks holda avval o'rnatilgan til qurilma tili sifatida qolib ketardi.
 */
private val deviceLocale: Locale = Locale.getDefault()

actual fun applyAppLanguage(tag: String?) {
    val locale = tag?.let { Locale.forLanguageTag(it) } ?: deviceLocale

    Locale.setDefault(locale)
    // Compose `Locale.current` API 24+ da aynan `LocaleList.getDefault()` ni o'qiydi,
    // shuning uchun uni ham yangilaymiz — faqat `Locale.setDefault` yetarli bo'lmasligi mumkin.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.setDefault(LocaleList(locale))
    }
}
