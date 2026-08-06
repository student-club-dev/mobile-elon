package dev.feature.profile.domain.model

/**
 * Foydalanuvchining profil ma'lumotlari (`GET/PUT /v1/profile/me`).
 *
 * Manba (offline-first):
 * - local kesh: SQLDelight `ProfileEntity` (yagona haqiqat UI uchun),
 * - masofaviy: REST `/profile/me` (backend javob bermasa profil local keshда qoladi).
 */
data class UserProfile(
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,   // E.164, masalan "+998901234567"
    val gender: String? = null,        // "MALE" | "FEMALE" — biznes turlari/kategoriyalarni moslaydi
    val role: String? = null,          // "STUDENT" | "BUSINESS" | "EMPLOYER" | "UNIVERSITY"
    val universityId: String? = null,
    val universityEmail: String? = null,
    val birthYear: Int? = null,
    val courseYear: String? = null,    // "1".."4" | "MASTER"
    /** Profil rasmi — `POST /v1/profile/me/avatar` qaytargan ochiq URL. */
    val avatarUrl: String? = null,
    // Biznes egasi (rol == "BUSINESS") — universitet/kurs o'rniga shu maydonlar to'ldiriladi.
    val businessName: String? = null,
    val businessType: String? = null,
    /** Aloqa emaili (gmail) — profilда tahrirlanadi. */
    val email: String? = null,
) {
    /** Ism + familiya (bo'sh bo'lsa `null`) — sarlavhalarda ko'rsatish uchun. */
    val displayName: String?
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { null }

    /**
     * Profil to'ldirilgan hisoblanadimi — **ism bor-yo'qligiga** qarab.
     *
     * Ilgari universitet ham hisobga olinardi: biznes egasida u hech qachon to'lmaydi
     * (forma uni so'ramaydi, u talaba maydoni), lekin eski talaba profilida saqlanib
     * qolgan qiymat ismsiz profilni ham "to'liq" deb ko'rsatib, ro'yxatdan o'tishdagi
     * hisob qadamini o'tkazib yuborishi mumkin edi.
     */
    val isComplete: Boolean
        get() = !firstName.isNullOrBlank()
}
