package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.feature.discounts.domain.model.Listing

/**
 * Backend + zaxira: avval [api] ga boradi, unga **yetib bo'lmasa** [local] ga tushadi.
 *
 * Maqsad — internet yo'q bo'lganda e'lon qo'yish oqimi to'xtamasin: e'lon local bazada faol
 * bo'ladi, rasm `data:` URI sifatida saqlanadi.
 *
 * ⚠️ **Zaxira faqat backendga yetib bo'lmagan holat uchun** (qarang [isUnreachable]). Server
 * javob berib **rad etgan** bo'lsa (validatsiya, 401, 403, 404, 5xx) xato foydalanuvchiga
 * aynan shundayligicha yetib boradi. Ilgari bu yerда har qanday xato yutilardi va oqibati
 * og'ir edi: foydalanuvchi "e'lon joylandi" degan xabarni ko'rardi, e'lon esa faqat telefonda
 * qolib ketardi — serverда undan nom-nishon yo'q edi.
 *
 * Repository qaysi manba ishlaganini bilmaydi ([ListingRemoteDataSource] shartnomasi bir xil).
 */
class FallbackListingRemoteDataSource(
    /** Odatда [ApiListingRemoteDataSource] — testда soxta manba qo'yiladi. */
    private val api: ListingRemoteDataSource,
    /** Odatда [LocalListingRemoteDataSource]. */
    private val local: ListingRemoteDataSource,
) : ListingRemoteDataSource {

    override suspend fun publish(listing: Listing): Resource<Listing> {
        val result = api.publish(listing)
        return if (result.isUnreachable()) local.publish(listing) else result
    }

    /**
     * Rasm ham xuddi shu qoida bilan: server "bu fayl yaramaydi" desa, uni jimgina `data:`
     * URI'ga aylantirib qo'yish yolg'on bo'lardi — bunday "manzil" keyin e'lonning `images`
     * ro'yxatiga tushib, yaratish so'rovini ham buzardi.
     */
    override suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String> {
        val result = api.uploadImage(bytes, fileName)
        return if (result.isUnreachable()) local.uploadImage(bytes, fileName) else result
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
