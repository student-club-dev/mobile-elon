package dev.feature.discounts.domain.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.Business
import kotlinx.coroutines.flow.Flow

/**
 * Foydalanuvchi bizneslariga egalik qiluvchi repository (backend `/business`).
 *
 * Bir egaga **bir nechta biznes** bo'lishi mumkin. Bosh ekran [observeMine] ro'yxatini
 * ko'rsatadi; bir biznesga bosilгач — o'sha biznesning e'lonlari ochiladi.
 */
interface BusinessRepository {

    /** Joriy foydalanuvchi bizneslarini real-time kuzatadi (ownerId == uid). */
    fun observeMine(): Flow<List<Business>>

    /** Bitta biznesni id bo'yicha oladi (e'lon yuklashda meros olish uchun). */
    suspend fun byId(id: String): Business?

    /** Biznesni yaratadi (id bo'sh bo'lsa yangi) yoki yangilaydi. */
    suspend fun save(business: Business): Resource<Business>

    /**
     * Biznesni moderatsiyaga yuboradi (`POST /business/{id}/submit`) va **serverdan qaytgan**
     * biznesni beradi.
     *
     * ⚠️ Qaytgan `status` ni o'qing, taxmin qilmang: backendда `MODERATION_ENABLED` bayrog'i
     * bor va u hozir **o'chiq**, ya'ni javob darrov `APPROVED` bo'lib keladi; bayroq
     * yoqilganda esa `PENDING_REVIEW` bo'ladi. Ilova kodi ikkala holatda ham o'zgarmasligi
     * kerak (`DISCOUNTS_BUSINESS_API_RESPONSE.md` §3).
     */
    suspend fun submit(id: String): Resource<Business>

    /** Biznesни o'chiradi. */
    suspend fun delete(id: String): Resource<Unit>

    /**
     * Logo rasmini yuklaydi (`POST /v1/media/upload`, purpose `LOGO`) va ochiq URL qaytaradi.
     * URL keyin [save] orqali biznesning `logoUrl` maydoniga yoziladi — yuklashning o'zi
     * biznesni o'zgartirmaydi.
     */
    suspend fun uploadLogo(bytes: ByteArray, fileName: String): Resource<String>
}
