package dev.feature.profile.data.remote

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.repository.ProfileExistence
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Backend + zaxira: avval [api] ga boradi, u **yetib bo'lmasa** profil local keshда ishlaydi.
 *
 * Repository offline-first — profilning local nusxasi baribir bazada saqlanadi. Shuning uchun
 * zaxira "xatoni `Success` ga aylantirish"dan iborat, LEKIN faqat backend gapirmaganда:
 * internet yo'q yoki so'rov vaqti tugagan.
 *
 * MUHIM — server javob bergan bo'lsa uning so'zi oxirgi. Ilgari validatsiyadan boshqa HAR
 * QANDAY xato (403 `PHONE_NOT_VERIFIED`, 409, 5xx, sessiya) `Success` ga aylantirilardi:
 * forma "saqlandi" deb yopilar, ma'lumot esa serverга yozilmagan bo'lardi va foydalanuvchi
 * backendning sababini umuman ko'rmasdi.
 */
class FallbackProfileRemoteDataSource(
    // Interfeys (konkret klass emas) — DI da [ApiProfileRemoteDataSource] ulanadi, testda esa
    // xatolarni qaytaradigan soxta manba.
    private val api: ProfileRemoteDataSource,
) : ProfileRemoteDataSource {

    /**
     * Xatoда `Success(null)` — "masofada profil yo'q" degani, repository esa keshni
     * o'zgartirmaydi. Shu sabab backend o'chganда mavjud profil o'chib ketmaydi.
     */
    override suspend fun fetch(): Resource<UserProfile?> =
        runCatching { api.fetch() }.getOrNull() as? Resource.Success ?: Resource.Success(null)

    /**
     * Backend yetib bo'lmaganда kiritilgan profil qaytadi — repository uni keshga yozadi va
     * forma saqlanadi. Server javob bergan bo'lsa (validatsiya, ruxsat, ziddiyat, 5xx) xato
     * o'z matni bilan yuqoriga uzatiladi.
     */
    override suspend fun save(profile: UserProfile): Resource<UserProfile> {
        val result = runCatching { api.save(profile) }.getOrNull()
        if (result != null && !result.isUnreachable()) return result
        return Resource.Success(profile)
    }

    /**
     * Bu yerда xatoni yutmaymiz: EXISTS/MISSING/ERROR shundaygina uzatiladi. Login yo'nalishida
     * ERROR ni MISSING'dan ajratish shart — aks holda mavjud foydalanuvchi SignUp'ga tushadi.
     * Kutilmagan istisno bo'lsa ham ERROR (MISSING emas).
     */
    override suspend fun checkExistence(): ProfileExistence =
        runCatching { api.checkExistence() }.getOrDefault(ProfileExistence.ERROR)

    /**
     * Backendsiz rasm hech qayerga yuklanmaydi — u `data:` URI sifatida profil bilan saqlanadi.
     *
     * Server rad etgan bo'lsa (masalan 413 — hajm chegarasi) xato ko'rsatiladi: aks holda
     * foydalanuvchi rasm yuklandi deb o'ylab, uni hech kim ko'rmasdi.
     */
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String> {
        val uploaded = runCatching { api.uploadAvatar(bytes, fileName) }.getOrNull()
        if (uploaded != null && !uploaded.isUnreachable()) return uploaded

        val mime = if (fileName.endsWith(".png", ignoreCase = true)) "image/png" else "image/jpeg"
        return Resource.Success("data:$mime;base64,${Base64.encode(bytes)}")
    }
}

/**
 * Backendning **o'zi** javob bermadimi (internet yo'q / vaqt tugadi)? Faqat shu ikki holatda
 * local zaxira mazmunli — qolganida server gapirgan va uning so'zi oxirgi.
 */
private fun Resource<*>.isUnreachable(): Boolean {
    val error = (this as? Resource.Error)?.error ?: return false
    return error is AppException.NoInternet || error is AppException.Timeout
}
