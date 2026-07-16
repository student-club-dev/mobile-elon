package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.feature.discounts.domain.model.Listing

/**
 * E'lonning masofaviy manbasi. Ikkita implementatsiya bor:
 *
 * - [LocalListingRemoteDataSource] — backendsiz rejim (hozirgi holat): rasm `data:` URI'ga
 *   aylanadi, e'lon darrov faol bo'ladi (moderator yo'q).
 * - [ApiListingRemoteDataSource] — real backend: `POST /v1/business/{id}/listings` +
 *   `/submit`, rasm `POST /v1/media/upload` orqali.
 *
 * Tanlov `CoreModules.REMOTE_SYNC_ENABLED` ga qarab DI'da qilinadi — repository qaysi
 * manba ishlayotganini bilmaydi.
 */
interface ListingRemoteDataSource {

    /** E'lonni yaratib, moderatsiyaga yuboradi. Qaytgan e'londa server bergan id/status bo'ladi. */
    suspend fun publish(listing: Listing): Resource<Listing>

    /** Rasmni yuklaydi va uning manzilini qaytaradi. */
    suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String>
}
