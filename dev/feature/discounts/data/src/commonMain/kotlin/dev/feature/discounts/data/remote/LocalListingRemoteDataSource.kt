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
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Backendsiz rejim — hozirgi holat (`REMOTE_SYNC_ENABLED = false`).
 *
 * - Rasm hech qayerga yuklanmaydi: baytlar `data:image/...;base64,...` URI'ga aylanadi va
 *   e'lon bilan birga local bazada saqlanadi. Shu sabab ilova internetsiz ham to'liq ishlaydi.
 * - Moderator yo'q, shuning uchun e'lon `PENDING_REVIEW` da qotib qolmasdan darrov [ListingStatus.ACTIVE]
 *   bo'ladi — biznes egasi natijani darrov ko'radi.
 *
 * Backend ulanganda [ApiListingRemoteDataSource] shu interfeysning o'rniga qo'yiladi va
 * e'lon haqiqiy moderatsiyaga tushadi.
 */
class LocalListingRemoteDataSource : ListingRemoteDataSource {

    // Backendsiz rejimda server ro'yxati yo'q — ekran e'lonlarni local bazadan (repository
    // `observeMyListings`) ko'radi, bu zaxira bo'sh sahifa qaytaradi.
    override suspend fun list(
        business: Business,
        status: ListingStatus?,
        categoryKey: String?,
        page: Int,
        size: Int,
    ): Resource<ListingPage> = Resource.Success(ListingPage.EMPTY)

    override suspend fun publish(listing: Listing): Resource<Listing> =
        Resource.Success(listing.copy(status = ListingStatus.ACTIVE))

    // Backendsiz rejimda tahrirlash/o'chirish local bazada bajariladi (repository qiladi),
    // shuning uchun bu yerda e'lonning o'zi muvaffaqiyat sifatida qaytadi.
    override suspend fun update(listing: Listing): Resource<Listing> = Resource.Success(listing)

    override suspend fun archive(id: String): Resource<Unit> = Resource.Success(Unit)

    /**
     * Backendsiz rejimda holat local bazada o'zgaradi (repository qiladi), shuning uchun bu
     * yerда shunchaki o'tishning **mo'ljallangan** natijasi qaytariladi. Serverdagidan farqli
     * o'laroq bu yerда `SCHEDULED` yo'q: local e'lon muddat tekshiruvisiz darrov faol bo'ladi.
     */
    override suspend fun changeStatus(
        id: String,
        transition: ListingTransition,
    ): Resource<ListingStatus> = Resource.Success(
        when (transition) {
            ListingTransition.PAUSE -> ListingStatus.PAUSED
            ListingTransition.ACTIVATE -> ListingStatus.ACTIVE
            ListingTransition.WITHDRAW -> ListingStatus.DRAFT
        },
    )

    // Quyidagilar SERVER amallari — local ekvivalenti yo'q va uni o'ylab topish zarar
    // qilardi: nusxa faqat telefonda paydo bo'lardi, statistika esa soxta nol ko'rsatib
    // "e'loningizni hech kim ko'rmadi" degan xulosaga olib kelardi. Shuning uchun ular
    // ochiq xato qaytaradi va ekran "internetni tekshiring" deydi.
    override suspend fun duplicate(id: String, business: Business): Resource<Listing> =
        Resource.Error("Nusxa olish uchun internet kerak")

    override suspend fun stats(id: String): Resource<ListingStats> =
        Resource.Error("Statistika uchun internet kerak")

    override suspend fun redemptions(id: String, page: Int, size: Int): Resource<RedemptionPage> =
        Resource.Error("Foydalanishlar tarixi uchun internet kerak")

    override suspend fun verifyRedemption(id: String, code: String): Resource<RedemptionCheck> =
        Resource.Error("Kodni tekshirish uchun internet kerak")

    override suspend fun confirmRedemption(
        id: String,
        code: String,
        branchId: String?,
        amount: Long?,
    ): Resource<Redemption> = Resource.Error("Chegirmani tasdiqlash uchun internet kerak")

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String> = try {
        val mime = if (fileName.endsWith(".png", ignoreCase = true)) "image/png" else "image/jpeg"
        Resource.Success("data:$mime;base64,${Base64.encode(bytes)}")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Rasmni o'qib bo'lmadi", e)
    }
}
