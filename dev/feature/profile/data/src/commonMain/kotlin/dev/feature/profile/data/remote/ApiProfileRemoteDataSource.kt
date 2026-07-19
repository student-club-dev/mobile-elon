package dev.feature.profile.data.remote

import dev.core.common.Resource
import dev.core.network.generated.api.ProfileApi
import dev.feature.profile.data.mapper.toDomain
import dev.feature.profile.data.mapper.toUpdateRequest
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.repository.ProfileExistence
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.forms.InputProvider
import io.ktor.http.HttpStatusCode
import kotlinx.io.Buffer

/**
 * Real backend (`GET/PUT /v1/profile/me`) — spetsifikatsiya:
 * `dev/api-client-generator/elon-uz.json`, klient shundan generatsiya qilingan.
 *
 * [ProfileApi] ga ilovaning umumiy Ktor klienti uzatiladi, shuning uchun har so'rovga
 * Firebase ID token `Authorization: Bearer ...` sifatida avtomatik qo'shiladi (B3).
 */
class ApiProfileRemoteDataSource(
    private val api: ProfileApi,
) : ProfileRemoteDataSource {

    override suspend fun fetch(): Resource<UserProfile?> = try {
        Resource.Success(api.getMyProfile().body().toDomain())
    } catch (e: ClientRequestException) {
        // 404 — profil hali yaratilmagan; bu xato emas, shunchaki bo'sh profil.
        if (e.response.status == HttpStatusCode.NotFound) Resource.Success(null)
        else Resource.Error(e.message, e)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Profilni yuklab bo'lmadi", e)
    }

    override suspend fun save(profile: UserProfile): Resource<UserProfile> = try {
        Resource.Success(api.updateMyProfile(profile.toUpdateRequest()).body().toDomain())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Profilni saqlab bo'lmadi", e)
    }

    override suspend fun checkExistence(): ProfileExistence = when (val res = fetch()) {
        // fetch() 404 ni Success(null) ga aylantiradi — demak MISSING; boshqa xatolar ERROR.
        is Resource.Success -> if (res.data != null) ProfileExistence.EXISTS else ProfileExistence.MISSING
        is Resource.Error -> ProfileExistence.ERROR
        Resource.Loading -> ProfileExistence.ERROR
    }

    override suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String> = try {
        // `POST /v1/profile/me/avatar` — multipart. Generatsiya qilingan klient faylni
        // `InputProvider` sifatida kutadi, server esa ochiq URL qaytaradi.
        val part = InputProvider(bytes.size.toLong()) { Buffer().apply { write(bytes) } }
        Resource.Success(api.uploadMyAvatar(part).body().avatarUrl)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Rasmni yuklab bo'lmadi", e)
    }
}
