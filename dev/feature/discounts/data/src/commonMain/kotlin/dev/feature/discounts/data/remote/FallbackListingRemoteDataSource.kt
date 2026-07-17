package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.feature.discounts.domain.model.Listing

/**
 * Backend + zaxira: avval [api] ga boradi, u xato bersa [local] ga tushadi.
 *
 * Maqsad — backend hali yozilmagan/o'chgan bo'lsa ham e'lon qo'yish oqimi to'xtamasin:
 * e'lon local bazada faol bo'ladi, rasm `data:` URI sifatida saqlanadi va foydalanuvchi
 * `Success` ko'radi. Backend ko'tarilishi bilan bu yerда hech narsa o'zgartirilmaydi —
 * javob kelsa, o'zi ishlatiladi.
 *
 * Repository qaysi manba ishlaganini bilmaydi ([ListingRemoteDataSource] shartnomasi bir xil).
 */
class FallbackListingRemoteDataSource(
    private val api: ApiListingRemoteDataSource,
    private val local: LocalListingRemoteDataSource,
) : ListingRemoteDataSource {

    override suspend fun publish(listing: Listing): Resource<Listing> =
        runCatching { api.publish(listing) }.getOrNull() as? Resource.Success
            ?: local.publish(listing)

    override suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String> =
        runCatching { api.uploadImage(bytes, fileName) }.getOrNull() as? Resource.Success
            ?: local.uploadImage(bytes, fileName)
}
