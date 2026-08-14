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
 * Testlar uchun [ListingRemoteDataSource] ning bo'sh asosi — har bir metod "chaqirilmasligi
 * kerak edi" degan xato qaytaradi.
 *
 * Nega kerak: interfeys 11 ta metodli va har bir test ularning faqat bittasi-ikkitasi bilan
 * qiziqadi. Bu asossiz bo'lsa har test 9 ta ma'nosiz `override` yozardi va yangi metod
 * qo'shilganda hammasi bir vaqtda sinardi.
 *
 * Xato qaytarish — `TODO()` yoki bo'sh `Success` dan ustun: test tasodifan ishlatmagan yo'lga
 * tushib qolsa, u jimgina "muvaffaqiyat" ko'rmaydi, balki aniq sabab bilan yiqiladi.
 */
internal abstract class FakeListingRemoteDataSource : ListingRemoteDataSource {

    override suspend fun list(
        business: Business,
        status: ListingStatus?,
        categoryKey: String?,
        page: Int,
        size: Int,
    ): Resource<ListingPage> = unused()

    override suspend fun publish(listing: Listing): Resource<Listing> = unused()

    override suspend fun update(listing: Listing): Resource<Listing> = unused()

    override suspend fun submitExisting(id: String): Resource<ListingStatus> = unused()

    override suspend fun archive(id: String): Resource<Unit> = unused()

    override suspend fun changeStatus(
        id: String,
        transition: ListingTransition,
    ): Resource<ListingStatus> = unused()

    override suspend fun duplicate(id: String, business: Business): Resource<Listing> = unused()

    override suspend fun stats(id: String): Resource<ListingStats> = unused()

    override suspend fun redemptions(id: String, page: Int, size: Int): Resource<RedemptionPage> = unused()

    override suspend fun verifyRedemption(id: String, code: String): Resource<RedemptionCheck> = unused()

    override suspend fun confirmRedemption(
        id: String,
        code: String,
        branchId: String?,
        amount: Long?,
    ): Resource<Redemption> = unused()

    override suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String> = unused()

    private fun <T> unused(): Resource<T> =
        Resource.Error("Bu metod testda ishlatilmaydi — kutilmagan chaqiruv")
}
