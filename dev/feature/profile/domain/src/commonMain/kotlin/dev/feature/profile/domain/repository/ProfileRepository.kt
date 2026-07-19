package dev.feature.profile.domain.repository

import dev.core.common.Resource
import dev.feature.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Profil mavjudligining uch holati — OTP dan keyingi yo'nalishni ajratadi:
 * - [EXISTS]  — profil bor (local kesh yoki backend javobi) → HOME.
 * - [MISSING] — backend aniq "yo'q" dedi (404) → SignUp (yangi foydalanuvchi).
 * - [ERROR]   — tekshirib bo'lmadi (backend o'chiq / timeout / token) → OTP ekranida qolib
 *   qayta urinish; foydalanuvchini noto'g'ri SignUp'ga tushirmaydi.
 */
enum class ProfileExistence { EXISTS, MISSING, ERROR }

/**
 * Profil ma'lumotiga egalik qiluvchi repository (offline-first).
 *
 * UI **har doim** [observeProfile] (local DB) ni kuzatadi; [refresh] va [saveProfile]
 * masofaviy manbadan olib/unga yozib, local keshni yangilaydi. Shu sabab tarmoq
 * bo'lmasa ham profil ko'rinadi.
 */
interface ProfileRepository {

    /** Local keshdagi joriy profil (sessiya yo'q bo'lsa `null`). */
    fun observeProfile(): Flow<UserProfile?>

    /** Masofaviy manbadan profilni olib local keshga yozadi. */
    suspend fun refresh(): Resource<Unit>

    /** Profilni masofaviy manbaga va local keshga saqlaydi. */
    suspend fun saveProfile(profile: UserProfile): Resource<Unit>

    /**
     * Profil rasmini yuklaydi va uning URL manzilini profilga yozib, keshni yangilaydi.
     * Qaytaradi: yuklangan rasmning URL manzili.
     *
     * @param bytes rasm fayli (JPEG/PNG)
     * @param fileName original fayl nomi (kengaytmani aniqlash uchun)
     */
    suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String>

    /**
     * Joriy sessiyada profil holati. Telefon OTP oqimida login (EXISTS → HOME),
     * register (MISSING → SignUp) va xato (ERROR → OTP'da qolib qayta urinish) ni ajratadi.
     */
    suspend fun profileExistence(): ProfileExistence
}
