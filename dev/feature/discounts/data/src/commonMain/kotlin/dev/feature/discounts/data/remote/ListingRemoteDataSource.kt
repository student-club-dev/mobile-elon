package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingPage
import dev.feature.discounts.domain.model.ListingStats
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.Redemption
import dev.feature.discounts.domain.model.RedemptionCheck
import dev.feature.discounts.domain.model.RedemptionPage

/**
 * E'lonning masofaviy manbasi. Yagona implementatsiya — [ApiListingRemoteDataSource]:
 * `POST /v1/business/{id}/listings` + `/submit`, rasm `POST /v1/media/upload` orqali.
 *
 * Local (offline) zaxira olib tashlandi: u e'lonni faqat shu qurilmada "faol" qilib qo'yar,
 * serverда esa undan nom-nishon qolmasdi. Endi backendga yetib bo'lmasa xato ko'rinadi.
 */
interface ListingRemoteDataSource {

    /**
     * Biznesning e'lonlarini paginatsiyalab oladi (`GET /business/{id}/listings`).
     * [business] — server javobida yo'q maydonlarni (nom, tur, filiallar) to'ldirish uchun.
     */
    suspend fun list(
        business: Business,
        status: ListingStatus?,
        categoryKey: String?,
        page: Int,
        size: Int,
    ): Resource<ListingPage>

    /** E'lonni yaratib, moderatsiyaga yuboradi. Qaytgan e'londa server bergan id/status bo'ladi. */
    suspend fun publish(listing: Listing): Resource<Listing>

    /** Mavjud e'lonni tahrirlaydi (`PUT /listings/{id}`). Server yangi status qaytaradi. */
    suspend fun update(listing: Listing): Resource<Listing>

    /** E'lonni arxivlaydi — soft-delete (`DELETE /listings/{id}`). */
    suspend fun archive(id: String): Resource<Unit>

    /**
     * Holatni o'zgartiradi va serverdagi **yangi holatni** qaytaradi.
     *
     * Backendда har o'tish uchun alohida endpoint bor (`/pause`, `/activate`, `/withdraw`),
     * chunki har biri o'z shartlarini tekshiradi — masalan muddati o'tgan e'lonni qayta
     * yoqib bo'lmaydi. Bu yerda ular bitta metodga yig'ilgan: chaqiruvchi uchun bu bitta
     * "holatni o'zgartir" amali, qaysi yo'lga borish esa [transition] bo'yicha aniqlanadi.
     *
     * Qaytgan [ListingStatus] **so'ralganidan farq qilishi mumkin** — masalan `activate`
     * dan keyin e'lon `SCHEDULED` bo'lib qolishi mumkin (boshlanish sanasi kelajakda).
     */
    suspend fun changeStatus(id: String, transition: ListingTransition): Resource<ListingStatus>

    /**
     * E'londan nusxa oladi (`POST /listings/{id}/duplicate`) — server yangi **qoralama**
     * yaratadi. Mavsumiy aksiyani qaytadan qo'yish uchun formani to'liq qayta to'ldirish
     * shart bo'lmasin.
     */
    suspend fun duplicate(id: String, business: Business): Resource<Listing>

    /** E'lon statistikasi (`GET /listings/{id}/stats`). */
    suspend fun stats(id: String): Resource<ListingStats>

    /** Foydalanishlar tarixi (`GET /listings/{id}/redemptions`), paginatsiyalangan. */
    suspend fun redemptions(id: String, page: Int, size: Int): Resource<RedemptionPage>

    /** Kassir: talaba kodini tekshiradi (`POST /listings/{id}/redeem/verify`) — o'zgartirmaydi. */
    suspend fun verifyRedemption(id: String, code: String): Resource<RedemptionCheck>

    /**
     * Kassir: chegirmani qo'llaydi (`POST /listings/{id}/redeem/confirm`) — **shu qadam**
     * foydalanishni hisobga oladi va limitni kamaytiradi.
     */
    suspend fun confirmRedemption(
        id: String,
        code: String,
        branchId: String?,
        amount: Long?,
    ): Resource<Redemption>

    /** Rasmni yuklaydi va uning manzilini qaytaradi. */
    suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String>
}

/**
 * E'lon holatining o'zgarishi — har biri o'z endpoint'iga mos keladi.
 *
 * Nega enum, `ListingStatus` emas: maqsad holat EMAS, **amal**. `PAUSE` va `ACTIVATE`
 * ikkalasi ham natijada `ACTIVE`/`PAUSED` beradi, lekin `WITHDRAW` moderatsiyadan qaytarish
 * bo'lib, natijasi `DRAFT` — ya'ni "qaysi holatni xohlayman" degan savol bir ma'noli javob
 * bermaydi.
 */
enum class ListingTransition { PAUSE, ACTIVATE, WITHDRAW }
