package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingPage
import dev.feature.discounts.domain.model.ListingStats
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.Redemption
import dev.feature.discounts.domain.model.RedemptionCheck
import dev.feature.discounts.domain.model.RedemptionPage

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

    override suspend fun list(
        business: Business,
        status: ListingStatus?,
        categoryKey: String?,
        page: Int,
        size: Int,
    ): Resource<ListingPage> {
        val result = api.list(business, status, categoryKey, page, size)
        return if (result.isUnreachable()) local.list(business, status, categoryKey, page, size) else result
    }

    override suspend fun publish(listing: Listing): Resource<Listing> {
        val result = api.publish(listing)
        return if (result.isUnreachable()) local.publish(listing) else result
    }

    override suspend fun update(listing: Listing): Resource<Listing> {
        val result = api.update(listing)
        return if (result.isUnreachable()) local.update(listing) else result
    }

    override suspend fun archive(id: String): Resource<Unit> {
        val result = api.archive(id)
        return if (result.isUnreachable()) local.archive(id) else result
    }

    /**
     * Holat o'zgarishi zaxiraga tushadi: e'lonni to'xtatish/yoqish local bazada ham mazmunli
     * (ro'yxat darrov yangilanadi), tarmoq tiklanganda esa serverdagi holat baribir ustun
     * bo'ladi — ro'yxat har ochilganda serverdan o'qiladi.
     */
    override suspend fun changeStatus(
        id: String,
        transition: ListingTransition,
    ): Resource<ListingStatus> {
        val result = api.changeStatus(id, transition)
        return if (result.isUnreachable()) local.changeStatus(id, transition) else result
    }

    // Quyidagi to'rttasi uchun zaxira YO'Q — ular sof server amallari (nusxa, statistika,
    // foydalanishlar, kassir kodi) va local javob soxta bo'lardi. `local` ularга ochiq xato
    // qaytaradi, shuning uchun uni chaqirish ham mumkin, lekin bevosita `api` javobini
    // qaytarish xatoni aniqroq qiladi: "internet yo'q" o'z holicha yetib boradi.
    override suspend fun duplicate(id: String, business: Business): Resource<Listing> =
        api.duplicate(id, business)

    override suspend fun stats(id: String): Resource<ListingStats> = api.stats(id)

    override suspend fun redemptions(id: String, page: Int, size: Int): Resource<RedemptionPage> =
        api.redemptions(id, page, size)

    override suspend fun verifyRedemption(id: String, code: String): Resource<RedemptionCheck> =
        api.verifyRedemption(id, code)

    override suspend fun confirmRedemption(
        id: String,
        code: String,
        branchId: String?,
        amount: Long?,
    ): Resource<Redemption> = api.confirmRedemption(id, code, branchId, amount)

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
