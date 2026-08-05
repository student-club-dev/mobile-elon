package dev.feature.profile.data.remote

import dev.core.common.Resource
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.repository.ProfileExistence

/**
 * Profilning masofaviy manbasi.
 *
 * DI [ApiProfileRemoteDataSource] ni ulaydi (`/profile/me`, OpenAPI'dan generatsiya qilingan
 * klient). Zaxira yo'q: backend javob bermasa xato yuqoriga uzatiladi.
 *
 * Repository qaysi manba ulanganini bilmaydi — u faqat shu interfeys bilan ishlaydi.
 */
interface ProfileRemoteDataSource {

    /** Profilni olib keladi. Profil hali yaratilmagan bo'lsa `Success(null)`. */
    suspend fun fetch(): Resource<UserProfile?>

    /** Profilni saqlaydi (upsert) va saqlangan holatini qaytaradi. */
    suspend fun save(profile: UserProfile): Resource<UserProfile>

    /**
     * Masofaviy manbada profil holati: EXISTS / MISSING (404) / ERROR (tarmoq/boshqa xato).
     * Xatoni MISSING'dan ajratish muhim — aks holda mavjud foydalanuvchi SignUp'ga tushadi.
     */
    suspend fun checkExistence(): ProfileExistence

    /**
     * Rasm faylini yuklab, uning ochiq URL manzilini qaytaradi.
     * Rasm saqlash serverni talab qiladi — backendsiz rejimda qo'llab-quvvatlanmaydi.
     */
    suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String>
}
