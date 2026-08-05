package dev.core.domain.repository

import dev.core.domain.model.AppLanguage
import dev.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Ilova sozlamalari — foydalanuvchi sessiyasidan mustaqil, local DB'da (`AppSettingEntity`) saqlanadi.
 * Reaktiv: qiymat o'zgarganda kuzatuvchilar avtomatik yangilanadi.
 */
interface SettingsRepository {
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)

    /** Boolean bayroq (masalan bildirishnoma sozlamalari). */
    fun observeFlag(key: String, default: Boolean): Flow<Boolean>
    suspend fun setFlag(key: String, value: Boolean)

    /** Ixtiyoriy string qiymat (yo'q bo'lsa `null`). */
    fun observeValue(key: String): Flow<String?>

    /** String qiymatni yozadi; `null` bo'lsa o'chiradi. */
    suspend fun setValue(key: String, value: String?)

    companion object {
        const val KEY_NOTIF_PUSH = "notif_push"
        const val KEY_NOTIF_EMAIL = "notif_email"

        /** Foydalanuvchi tanlagan rol (STUDENT/BUSINESS) — logout qilgunча saqlanadi. */
        const val KEY_SELECTED_ROLE = "selected_role"

        /** Foydalanuvchi jinsi ("MALE"/"FEMALE") — biznes turlari/kategoriyalarni moslaydi. */
        const val KEY_GENDER = "gender"

        /**
         * Tugallanmagan ro'yxatdan o'tish bosqichi — `"OTP"` yoki `"PROFILE"` (yo'q bo'lsa
         * oqim tugagan).
         *
         * Nega local saqlanadi: backend `register` javobida hisobni DARHOL ochadi va sessiya
         * beradi, raqam tasdig'i esa alohida (`/otp/verify`) — ya'ni "hisob bor, lekin oqim
         * tugamagan" holati faqat ilova tomonda ma'lum (profil sxemasida `phoneVerified` yo'q).
         * Bayroqsiz ilova qayta ochilganда foydalanuvchi to'g'ridan-to'g'ri bosh ekranga
         * tushib, tasdiqlashni ham, ism-familiyani ham berib ketmasdi.
         */
        const val KEY_SIGNUP_STAGE = "signup_stage"
    }
}
