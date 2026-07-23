package dev.core.domain.repository

import dev.core.common.Resource
import dev.core.domain.model.AuthIdentifier
import dev.core.domain.model.DeviceSession
import dev.core.domain.model.OtpChallenge
import dev.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Sessiya (autentifikatsiya) repository'si — backend `/v1/auth/business/…` endpoint'lari.
 *
 * Oqim spec'ga to'liq mos:
 * - **kirish/ro'yxat** — telefon yoki email + parol → access/refresh juftligi;
 * - **SMS kod** — kirish usuli EMAS: u faqat raqamni tasdiqlash ([requestPhoneOtp]) va
 *   parolni tiklash ([forgotPassword] → [resetPassword]) uchun;
 * - **token yangilash** — avtomatik, tarmoq qatlamida (`createHttpClient`).
 *
 * Profil ma'lumoti (universitet, kurs, rol...) bu yerda EMAS — unga
 * `dev.feature.profile.domain.repository.ProfileRepository` egalik qiladi.
 */
interface AuthRepository {

    /** Telefon yoki email + parol bilan kirish (`POST /auth/business/login`). */
    suspend fun login(identifier: AuthIdentifier, password: String): Resource<User>

    /** Yangi biznes hisobi (`POST /auth/business/register`). */
    suspend fun register(identifier: AuthIdentifier, password: String): Resource<User>

    /** Chiqish — refresh token bekor qilinadi, local sessiya va profil keshi tozalanadi. */
    suspend fun logout()

    /** Joriy foydalanuvchi (local keshdan, offline-first; sessiya bo'lmasa `null`). */
    suspend fun currentUser(): User?

    /**
     * Local kesh ustidan joriy foydalanuvchini reaktiv kuzatadi — kesh yangilanganda/
     * tozalanganda avtomatik yangi qiymat chiqadi. Ilova ochilishida avtomatik kirish
     * (session restore) uchun ishlatiladi.
     */
    fun observeCurrentUser(): Flow<User?>

    // --- Telefon raqamini tasdiqlash (kirgan holatda) --------------------------------

    /** Raqamga SMS kod yuboradi (`POST /auth/business/otp/request`). */
    suspend fun requestPhoneOtp(phone: String): Resource<OtpChallenge>

    /** Kodni tekshiradi va raqamni tasdiqlangan deb belgilaydi. */
    suspend fun verifyPhoneOtp(phone: String, code: String): Resource<Unit>

    // --- Parol ----------------------------------------------------------------------

    /** Parolni tiklash uchun SMS kod so'raydi (`POST /auth/business/password/forgot`). */
    suspend fun forgotPassword(phone: String): Resource<Unit>

    /** SMS kod bilan yangi parol o'rnatadi (`POST /auth/business/password/reset`). */
    suspend fun resetPassword(phone: String, code: String, newPassword: String): Resource<Unit>

    /**
     * Kirgan holatda parolni o'rnatadi/almashtiradi (`POST /auth/business/password/set`).
     * [currentPassword] faqat mavjud parolni almashtirishda kerak.
     */
    suspend fun setPassword(currentPassword: String?, newPassword: String): Resource<Unit>

    // --- Faol qurilmalar -------------------------------------------------------------

    /** Hisobning faol sessiyalari (`GET /auth/business/sessions`). */
    suspend fun sessions(): Resource<List<DeviceSession>>

    /** Bitta qurilma sessiyasini bekor qiladi. */
    suspend fun revokeSession(id: String): Resource<Unit>

    /** Barcha qurilmalardan chiqadi (joriysi ham). */
    suspend fun logoutAllDevices(): Resource<Unit>
}
