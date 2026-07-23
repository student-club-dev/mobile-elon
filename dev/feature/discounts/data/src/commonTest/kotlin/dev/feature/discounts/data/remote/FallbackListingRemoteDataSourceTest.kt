package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.core.common.errorOf
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingDiscount
import dev.feature.discounts.domain.model.ListingRedemption
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.RedemptionMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Zaxira **qachon** ishlashi kerakligi haqidagi shartnoma.
 *
 * Muhimi ijobiy holat emas, salbiysi: server e'lonni rad etganда zaxira ishga tushmasligi
 * SHART. Aks holda foydalanuvchi "e'lon joylandi" degan xabarni ko'radi, e'lon esa faqat
 * telefonda qoladi — serverда yo'q.
 */
class FallbackListingRemoteDataSourceTest {

    private class FakeApi(
        private val publishResult: Resource<Listing>,
        private val uploadResult: Resource<String> = Resource.Success("https://cdn/x.jpg"),
    ) : ListingRemoteDataSource {
        override suspend fun publish(listing: Listing) = publishResult
        override suspend fun uploadImage(bytes: ByteArray, fileName: String) = uploadResult
    }

    private fun sourceWith(
        publishResult: Resource<Listing>,
        uploadResult: Resource<String> = Resource.Success("https://cdn/x.jpg"),
    ) = FallbackListingRemoteDataSource(
        api = FakeApi(publishResult, uploadResult),
        local = LocalListingRemoteDataSource(),
    )

    // -----------------------------------------------------------------------
    // publish
    // -----------------------------------------------------------------------

    @Test
    fun serverValidationErrorIsNotSwallowed() = runTest {
        val rejected = errorOf(AppException.Validation("Kategoriya noto'g'ri"))
        val result = sourceWith(rejected).publish(listing())

        assertTrue(result is Resource.Error, "Server rad etdi, lekin zaxira uni Success qildi")
        assertEquals("Kategoriya noto'g'ri", result.message)
    }

    @Test
    fun serverFailureIsNotSwallowed() = runTest {
        val result = sourceWith(errorOf(AppException.Server(500))).publish(listing())
        assertTrue(result is Resource.Error, "5xx zaxira bilan yashirildi")
    }

    @Test
    fun expiredSessionIsNotSwallowed() = runTest {
        val result = sourceWith(errorOf(AppException.Unauthorized())).publish(listing())
        assertTrue(result is Resource.Error, "401 zaxira bilan yashirildi — token yangilanmaydi")
    }

    @Test
    fun offlineFallsBackToLocal() = runTest {
        val result = sourceWith(errorOf(AppException.NoInternet())).publish(listing())

        assertTrue(result is Resource.Success, "Internetsiz e'lon local bazada saqlanishi kerak")
        assertEquals(ListingStatus.ACTIVE, result.data.status)
    }

    @Test
    fun successPassesThrough() = runTest {
        val published = listing().copy(id = "srv-1", status = ListingStatus.PENDING_REVIEW)
        val result = sourceWith(Resource.Success(published)).publish(listing())

        assertTrue(result is Resource.Success)
        assertEquals("srv-1", result.data.id)
    }

    // -----------------------------------------------------------------------
    // uploadImage
    // -----------------------------------------------------------------------

    /**
     * Rad etilgan rasm `data:` URI'ga aylanib qolmasligi kerak — u keyin e'lonning `images`
     * ro'yxatiga tushib, yaratish so'rovini ham buzardi.
     */
    @Test
    fun rejectedImageIsNotTurnedIntoDataUri() = runTest {
        val source = sourceWith(
            publishResult = Resource.Success(listing()),
            uploadResult = errorOf(AppException.Validation("Fayl turi qo'llab-quvvatlanmaydi")),
        )

        val result = source.uploadImage(byteArrayOf(1, 2, 3), "a.jpg")

        assertTrue(result is Resource.Error, "Server rasmni rad etdi, zaxira uni yashirdi")
    }

    @Test
    fun offlineImageBecomesDataUri() = runTest {
        val source = sourceWith(
            publishResult = Resource.Success(listing()),
            uploadResult = errorOf(AppException.NoInternet()),
        )

        val result = source.uploadImage(byteArrayOf(1, 2, 3), "a.jpg")

        assertTrue(result is Resource.Success)
        assertTrue(result.data.startsWith("data:image/jpeg;base64,"), result.data)
    }
}

private fun listing() = Listing(
    id = "lst-1",
    ownerId = "1",
    businessId = "biz-1",
    businessType = BusinessType("NATIONAL_FOOD"),
    businessName = "Chaykhana Navruz",
    categoryKey = "coffee",
    title = "Kofe",
    priceUnit = PriceUnit.PER_ITEM,
    originalPrice = 20_000,
    discount = ListingDiscount(type = DiscountType.PERCENT, value = 20),
    redemption = ListingRedemption(method = RedemptionMethod.STUDENT_ID),
    validFrom = 0,
    validTo = 1,
    createdAt = 0,
    updatedAt = 0,
)
