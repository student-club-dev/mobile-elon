package dev.feature.profile.data.remote

import dev.core.common.Resource
import dev.feature.profile.domain.model.UserProfile

/**
 * Profilning masofaviy manbasi. Ikkita implementatsiyasi bor va DI qaysi birini
 * ulashni `REMOTE_SYNC_ENABLED` bayrog'iga qarab hal qiladi:
 *
 * - [ApiProfileRemoteDataSource] — real backend (`/v1/profile/me`, OpenAPI'dan generatsiya qilingan klient),
 * - [FirestoreProfileRemoteDataSource] — backendsiz rejim (Firestore `users/{uid}`).
 *
 * Repository qaysi manba ulanganini bilmaydi — u faqat shu interfeys bilan ishlaydi.
 */
interface ProfileRemoteDataSource {

    /** Profilni olib keladi. Profil hali yaratilmagan bo'lsa `Success(null)`. */
    suspend fun fetch(): Resource<UserProfile?>

    /** Profilni saqlaydi (upsert) va saqlangan holatini qaytaradi. */
    suspend fun save(profile: UserProfile): Resource<UserProfile>

    /** Masofaviy manbada profil mavjudmi (tarmoq xatosida `false`). */
    suspend fun exists(): Boolean

    /**
     * Rasm faylini yuklab, uning ochiq URL manzilini qaytaradi.
     * Rasm saqlash serverni talab qiladi — backendsiz rejimda qo'llab-quvvatlanmaydi.
     */
    suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String>
}
