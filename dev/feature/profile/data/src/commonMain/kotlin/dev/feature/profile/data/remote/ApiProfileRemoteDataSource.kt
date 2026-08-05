package dev.feature.profile.data.remote

import dev.core.common.Resource
import dev.core.common.error.toAppException
import dev.core.common.errorOf
import dev.core.common.map
import dev.core.network.generated.api.ProfileApi
import dev.core.network.media.MediaPurpose
import dev.core.network.media.MediaUploader
import dev.core.network.response.safeCall
import dev.core.network.response.toAppExceptionWithFields
import dev.feature.profile.data.mapper.toDomain
import dev.feature.profile.data.mapper.toUpdateRequest
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.repository.ProfileExistence
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

/**
 * Real backend (`GET/PUT /v1/profile/me`) — spetsifikatsiya:
 * `dev/api-client-generator/elon-uz.json`, klient shundan generatsiya qilingan.
 *
 * API klientlariga ilovaning umumiy Ktor klienti uzatiladi, shuning uchun har so'rovga sessiya
 * tokeni `Authorization: Bearer ...` sifatida avtomatik qo'shiladi.
 */
class ApiProfileRemoteDataSource(
    private val api: ProfileApi,
    private val media: MediaUploader,
) : ProfileRemoteDataSource {

    override suspend fun fetch(): Resource<UserProfile?> = try {
        Resource.Success(api.getMe().body().toDomain())
    } catch (e: ClientRequestException) {
        // 404 — profil hali yaratilmagan; bu xato emas, shunchaki bo'sh profil.
        if (e.response.status == HttpStatusCode.NotFound) {
            Resource.Success(null)
        } else {
            // Qolgan 4xx — javob tanasidagi backend matni bilan (typed). `e.message` bo'lsa
            // foydalanuvchi Ktor'ning inglizcha matnini ko'rardi.
            errorOf(e.toAppExceptionWithFields())
        }
    } catch (e: Exception) {
        errorOf(e.toAppException())
    }

    /**
     * `safeCall` orqali — u 422 javob tanasini o'qib `AppException.Validation.fields` ni
     * to'ldiradi. O'z `try/catch` imiz bo'lsa maydon xatolari shu yerda yo'qolardi
     * (`Resource.Error(e.message)` typed xatoni tashlab yuboradi).
     */
    override suspend fun save(profile: UserProfile): Resource<UserProfile> =
        safeCall { api.updateMe(profile.toUpdateRequest()).body() }.map { it.toDomain() }

    override suspend fun checkExistence(): ProfileExistence = when (val res = fetch()) {
        // fetch() 404 ni Success(null) ga aylantiradi — demak MISSING; boshqa xatolar ERROR.
        is Resource.Success -> if (res.data != null) ProfileExistence.EXISTS else ProfileExistence.MISSING
        is Resource.Error -> ProfileExistence.ERROR
        Resource.Loading -> ProfileExistence.ERROR
    }

    override suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String> =
        safeCall {
            // Profil rasmi uchun alohida endpoint yo'q — umumiy `POST /v1/media/upload` ishlatiladi,
            // qaytgan URL esa `PUT /profile/me` orqali `avatarUrl` ga yoziladi (repository qiladi).
            // Spec'da `purpose` uchun faqat LOGO | COVER | LISTING bor, avatar uchun eng yaqini — LOGO.
            media.upload(bytes, fileName, MediaPurpose.LOGO).url
        }
}
