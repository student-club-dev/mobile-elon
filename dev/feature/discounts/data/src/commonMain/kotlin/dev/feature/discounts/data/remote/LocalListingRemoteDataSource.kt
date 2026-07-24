package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingPage
import dev.feature.discounts.domain.model.ListingStatus
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

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String> = try {
        val mime = if (fileName.endsWith(".png", ignoreCase = true)) "image/png" else "image/jpeg"
        Resource.Success("data:$mime;base64,${Base64.encode(bytes)}")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Rasmni o'qib bo'lmadi", e)
    }
}
