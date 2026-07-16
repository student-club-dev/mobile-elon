package dev.feature.profile.domain.repository

import dev.core.common.Resource
import dev.feature.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

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
     * Joriy sessiyada saqlangan profil bormi. Telefon OTP oqimida login
     * (profil bor → HOME) va register (profil yo'q → SignUp) yo'nalishlarini ajratadi.
     */
    suspend fun hasProfile(): Boolean
}
