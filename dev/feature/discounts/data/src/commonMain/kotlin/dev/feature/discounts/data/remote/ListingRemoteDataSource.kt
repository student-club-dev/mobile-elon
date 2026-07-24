package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingPage
import dev.feature.discounts.domain.model.ListingStatus

/**
 * E'lonning masofaviy manbasi. Ikkita implementatsiya bor:
 *
 * - [ApiListingRemoteDataSource] — real backend: `POST /v1/business/{id}/listings` +
 *   `/submit`, rasm `POST /v1/media/upload` orqali.
 * - [LocalListingRemoteDataSource] — backendsiz (offline) zaxira: rasm `data:` URI'ga
 *   aylanadi, e'lon darrov faol bo'ladi (moderator yo'q).
 *
 * DI'da har doim [FallbackListingRemoteDataSource] o'rnatiladi: u avval backendga boradi va
 * faqat unga **yetib bo'lmaganda** local zaxiraga tushadi. Repository qaysi manba ishlaganini
 * bilmaydi.
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

    /** Rasmni yuklaydi va uning manzilini qaytaradi. */
    suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String>
}
