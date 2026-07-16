package dev.feature.profile.domain.model

/**
 * Foydalanuvchining profil ma'lumotlari — Firebase Auth bermaydigan maydonlar.
 *
 * Manba (offline-first):
 * - local kesh: SQLDelight `ProfileEntity` (yagona haqiqat UI uchun),
 * - masofaviy: REST `/v1/profile/me` (real API) yoki Firestore `users/{uid}` (backendsiz rejim).
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

    /** Profil to'ldirilgan hisoblanadimi (kamida ism yoki universitet bor). */
    val isComplete: Boolean
        get() = !firstName.isNullOrBlank() || !universityId.isNullOrBlank()
}
