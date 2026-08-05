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

    /** Faqat shu test qiziqadigan uchta yo'l — qolganini [FakeListingRemoteDataSource] beradi. */
    private class FakeApi(
        private val publishResult: Resource<Listing>,
        private val uploadResult: Resource<String> = Resource.Success("https://cdn/x.jpg"),
        /** Holat o'zgarishi — zaxira shu natijaga qarab ishga tushadi (yoki tushmaydi). */
        private val statusResult: Resource<ListingStatus> = Resource.Success(ListingStatus.PAUSED),
    ) : FakeListingRemoteDataSource() {
        override suspend fun publish(listing: Listing) = publishResult
        override suspend fun update(listing: Listing) = publishResult
        override suspend fun archive(id: String): Resource<Unit> = Resource.Success(Unit)
        override suspend fun changeStatus(id: String, transition: ListingTransition) = statusResult
        override suspend fun uploadImage(bytes: ByteArray, fileName: String) = uploadResult
    }

    private fun sourceWith(
        publishResult: Resource<Listing>,
        uploadResult: Resource<String> = Resource.Success("https://cdn/x.jpg"),
        statusResult: Resource<ListingStatus> = Resource.Success(ListingStatus.PAUSED),
    ) = FallbackListingRemoteDataSource(
        api = FakeApi(publishResult, uploadResult, statusResult),
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
    // changeStatus — server nima desa, o'sha
    // -----------------------------------------------------------------------

    /**
     * `activate` boshlanish sanasi kelajakda bo'lgan e'lonni `ACTIVE` emas, `SCHEDULED`
     * qiladi. Zaxira qatlami bu qiymatni **o'zgartirmasligi** kerak: aks holda karta e'lonni
     * "faol" deb ko'rsatib, talabalar esa uni hali ko'rmayotgan bo'lardi.
     */
    @Test
    fun serverStatusIsPassedThroughUnchanged() = runTest {
        val result = sourceWith(
            publishResult = Resource.Success(listing()),
            statusResult = Resource.Success(ListingStatus.SCHEDULED),
        ).changeStatus("lst-1", ListingTransition.ACTIVATE)

        assertTrue(result is Resource.Success)
        assertEquals(ListingStatus.SCHEDULED, result.data, "Server bergan holat almashtirildi")
    }

    /** Server rad etsa (masalan muddati o'tgan e'lon) — zaxira uni yashirmaydi. */
    @Test
    fun rejectedStatusChangeIsNotSwallowed() = runTest {
        val result = sourceWith(
            publishResult = Resource.Success(listing()),
            statusResult = errorOf(AppException.Validation("Muddati o'tgan e'lonni yoqib bo'lmaydi")),
        ).changeStatus("lst-1", ListingTransition.ACTIVATE)

        assertTrue(result is Resource.Error, "Server rad etdi, zaxira uni Success qildi")
        assertEquals("Muddati o'tgan e'lonni yoqib bo'lmaydi", result.message)
    }

    /** Internet yo'q bo'lsa to'xtatish local bazada bajariladi — ro'yxat darrov yangilanadi. */
    @Test
    fun offlineStatusChangeFallsBackToLocal() = runTest {
        val result = sourceWith(
            publishResult = Resource.Success(listing()),
            statusResult = errorOf(AppException.NoInternet()),
        ).changeStatus("lst-1", ListingTransition.PAUSE)

        assertTrue(result is Resource.Success, "Internetsiz to'xtatish local bajarilishi kerak")
        assertEquals(ListingStatus.PAUSED, result.data)
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
