package dev.feature.profile.data.remote

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.repository.ProfileExistence
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

    /**
     * Xatoда kiritilgan profil qaytadi — repository uni keshga yozadi va forma saqlanadi.
     *
     * **Istisno — validatsiya xatolari.** Backend "bu telefon raqami noto'g'ri" desa, buni
     * yutib yuborib `Success` qaytarish yolg'on bo'lardi: forma yopilar, ma'lumot esa serverga
     * yozilmagan bo'lardi. Zaxira faqat backend **yetib bo'lmaydigan** holat uchun.
     */
    override suspend fun save(profile: UserProfile): Resource<UserProfile> {
        val result = runCatching { api.save(profile) }.getOrNull()
        if (result is Resource.Success) return result
        if (result is Resource.Error && result.error is AppException.Validation) return result
        return Resource.Success(profile)
    }

    /**
     * Bu yerда xatoni yutmaymiz: EXISTS/MISSING/ERROR shundaygina uzatiladi. Login yo'nalishida
     * ERROR ni MISSING'dan ajratish shart — aks holda mavjud foydalanuvchi SignUp'ga tushadi.
     * Kutilmagan istisno bo'lsa ham ERROR (MISSING emas).
     */
    override suspend fun checkExistence(): ProfileExistence =
        runCatching { api.checkExistence() }.getOrDefault(ProfileExistence.ERROR)

    /** Backendsiz rasm hech qayerga yuklanmaydi — u `data:` URI sifatida profil bilan saqlanadi. */
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String> {
        val uploaded = runCatching { api.uploadAvatar(bytes, fileName) }.getOrNull()
        if (uploaded is Resource.Success) return uploaded

        val mime = if (fileName.endsWith(".png", ignoreCase = true)) "image/png" else "image/jpeg"
        return Resource.Success("data:$mime;base64,${Base64.encode(bytes)}")
    }
}
