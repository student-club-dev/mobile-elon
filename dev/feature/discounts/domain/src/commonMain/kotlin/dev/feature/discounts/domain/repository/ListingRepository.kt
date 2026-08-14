package dev.feature.discounts.domain.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingPage
import dev.feature.discounts.domain.model.ListingStats
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.Redemption
import dev.feature.discounts.domain.model.RedemptionCheck
import dev.feature.discounts.domain.model.RedemptionPage
import kotlinx.coroutines.flow.Flow

/**
 * Chegirma e'lonlariga egalik qiluvchi repository (offline-first).
 *
 * UI **har doim** local DB'ni kuzatadi ([observeMyListings] / [observeActive]) — shu sabab
 * tarmoq bo'lmasa ham e'lonlar ko'rinadi. [save] va [submit] masofaviy manba bilan
 * gaplashib (agar u yoqilgan bo'lsa) local keshni yangilaydi.
 */
interface ListingRepository {

    /** Joriy foydalanuvchi yuklagan e'lonlar (barcha statuslar). */
    fun observeMyListings(ownerId: String): Flow<List<Listing>>

    /**
     * Biznesning e'lonlarini **serverdan** paginatsiyalab oladi
     * (`GET /business/{id}/listings`, yangi birinchi).
     *
     * [business] — nomi/turi va filiallar shundan olinadi: server javobida (`ListingDto`)
     * ular yo'q, faqat `branchIds` bor, shuning uchun to'liq [Listing] ni yig'ish uchun
     * ochilgan biznes konteksti kerak.
     */
    suspend fun listForBusiness(
        business: Business,
        status: ListingStatus? = null,
        categoryKey: String? = null,
        page: Int = 1,
        size: Int = 20,
    ): Resource<ListingPage>

    /** Talabaga ko'rinadigan e'lonlar — faqat ACTIVE va muddati o'tmaganlar. */
    fun observeActive(): Flow<List<Listing>>

    suspend fun byId(id: String): Listing?

    /** Qoralama sifatida saqlaydi (moderatsiyaga yubormaydi). */
    suspend fun save(listing: Listing): Resource<Listing>

    /**
     * Moderatsiyaga yuboradi. Backend yoqilganda `PENDING_REVIEW`, offline rejimda
     * darrov `ACTIVE` bo'ladi (moderator yo'q).
     */
    suspend fun submit(listing: Listing): Resource<Listing>

    /** Mavjud e'lonni tahrirlaydi (`PUT /listings/{id}`) va local keshni yangilaydi. */
    suspend fun update(listing: Listing): Resource<Listing>

    /**
     * MAVJUD e'lonni moderatsiyaga yuboradi (`POST /listings/{id}/submit`) va serverdagi
     * yangi holatni qaytaradi.
     *
     * [submit] dan farqi: u e'lonni avval YARATADI. Qoralamani (masalan nusxa olingan
     * e'lonni) tahrirlab "E'lon qilish" bosilganда esa e'lon allaqachon serverда bor —
     * uni faqat yuborish kerak edi. Busiz `PUT` dan keyin e'lon DRAFT bo'lib qolaverardi
     * va uni faol qilishning umuman yo'li yo'q edi.
     */
    suspend fun submitExisting(id: String): Resource<ListingStatus>

    /**
     * E'lonni to'xtatadi / qayta yoqadi (`POST /listings/{id}/pause` · `/activate`) va
     * **serverdagi yangi holatni** qaytaradi.
     *
     * Qaytgan holat so'ralganidan farq qilishi mumkin: `activate` boshlanish sanasi
     * kelajakda bo'lgan e'lonni `SCHEDULED` qiladi. Shu sabab metod `Unit` emas, holat
     * qaytaradi — ekran kartani aynan shu qiymat bilan yangilaydi.
     */
    suspend fun setPaused(id: String, paused: Boolean): Resource<ListingStatus>

    /**
     * Moderatsiyadagi e'lonni qaytarib oladi (`POST /listings/{id}/withdraw`) — u qoralamaga
     * tushadi va tahrirlash mumkin bo'ladi.
     */
    suspend fun withdraw(id: String): Resource<ListingStatus>

    /**
     * E'londan nusxa oladi (`POST /listings/{id}/duplicate`) — server yangi qoralama yaratadi.
     * [business] qaytgan e'lonni to'liq yig'ish uchun (server javobida biznes nomi/turi yo'q).
     */
    suspend fun duplicate(id: String, business: Business): Resource<Listing>

    /** E'lon statistikasi (`GET /listings/{id}/stats`). */
    suspend fun stats(id: String): Resource<ListingStats>

    /** Foydalanishlar tarixi (`GET /listings/{id}/redemptions`). */
    suspend fun redemptions(id: String, page: Int = 1, size: Int = 20): Resource<RedemptionPage>

    /** Kassir: talaba kodini tekshiradi — hech narsani o'zgartirmaydi. */
    suspend fun verifyRedemption(id: String, code: String): Resource<RedemptionCheck>

    /** Kassir: chegirmani qo'llaydi va foydalanishni hisobga oladi. */
    suspend fun confirmRedemption(
        id: String,
        code: String,
        branchId: String? = null,
        amount: Long? = null,
    ): Resource<Redemption>

    suspend fun delete(id: String): Resource<Unit>

    /**
     * Rasmni yuklaydi va uning manzilini qaytaradi.
     * Backend bilan — CDN havolasi (`POST /media/upload`), offline rejimda — `data:` URI.
     */
    suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String>
}
