package dev.feature.profile.data.remote

import dev.core.common.Resource
import dev.feature.profile.domain.model.UserProfile
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Backend + zaxira: avval [api] ga boradi, u xato bersa profil **local keshда** ishlaydi.
 *
 * Repository offline-first — profilning local nusxasi baribir bazada saqlanadi. Shuning uchun
 * zaxira "xatoni `Success` ga aylantirish"dan iborat: backend o'chgan bo'lsa ham foydalanuvchi
 * profilini tahrirlay oladi va natijani ko'radi. Backend ko'tarilishi bilan bu yerда hech narsa
 * o'zgartirilmaydi — javob kelsa, o'zi ishlatiladi.
 */
class FallbackProfileRemoteDataSource(
    private val api: ApiProfileRemoteDataSource,
) : ProfileRemoteDataSource {

    /**
     * Xatoда `Success(null)` — "masofada profil yo'q" degani, repository esa keshni
     * o'zgartirmaydi. Shu sabab backend o'chganда mavjud profil o'chib ketmaydi.
     */
    override suspend fun fetch(): Resource<UserProfile?> =
        runCatching { api.fetch() }.getOrNull() as? Resource.Success ?: Resource.Success(null)

    /** Xatoда kiritilgan profil qaytadi — repository uni keshga yozadi va forma saqlanadi. */
    override suspend fun save(profile: UserProfile): Resource<UserProfile> =
        runCatching { api.save(profile) }.getOrNull() as? Resource.Success
            ?: Resource.Success(profile)

    /** Tarmoq xatosida `false` — repository avval keshni tekshiradi. */
    override suspend fun exists(): Boolean = runCatching { api.exists() }.getOrDefault(false)

    /** Backendsiz rasm hech qayerga yuklanmaydi — u `data:` URI sifatida profil bilan saqlanadi. */
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String> {
        val uploaded = runCatching { api.uploadAvatar(bytes, fileName) }.getOrNull()
        if (uploaded is Resource.Success) return uploaded

        val mime = if (fileName.endsWith(".png", ignoreCase = true)) "image/png" else "image/jpeg"
        return Resource.Success("data:$mime;base64,${Base64.encode(bytes)}")
    }
}
