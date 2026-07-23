package dev.feature.discounts.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.core.common.AppDispatchers
import dev.core.common.Resource
import dev.core.database.sql.ElonUzDatabase
import dev.feature.discounts.data.remote.ListingRemoteDataSource
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingDiscount
import dev.feature.discounts.domain.model.ListingRedemption
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.RedemptionMethod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * "E'lon qo'ydim — u ro'yxatda ko'rinishi kerak" oqimi, **haqiqiy SQLite** ustida.
 *
 * Ekran local bazani kuzatadi ([ListingRepositoryImpl.observeMyListings]), yozish esa
 * [ListingRepositoryImpl.submit] orqali bo'ladi. Ikkisi orasidagi bog'lanish uzilsa
 * (masalan `ownerId` boshqa bo'lib qolsa) foydalanuvchi uchun e'lon "yo'qolgan"dek
 * ko'rinadi — shuning uchun bu yo'l test bilan qulflanadi.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ListingRepositoryFlowTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val dispatchers = object : AppDispatchers {
        override val io: CoroutineDispatcher get() = dispatcher
        override val default: CoroutineDispatcher get() = dispatcher
        override val main: CoroutineDispatcher get() = dispatcher
    }

    /** Serverni taqlid qiladi: o'z id/statusini qaytaradi (haqiqiy backend ham shunday qiladi). */
    private class ServerRemote : ListingRemoteDataSource {
        override suspend fun publish(listing: Listing) = Resource.Success(
            listing.copy(id = "srv-${listing.id}", status = ListingStatus.PENDING_REVIEW),
        )
        override suspend fun uploadImage(bytes: ByteArray, fileName: String) =
            Resource.Success("https://cdn/x.jpg")
    }

    private fun repository(remote: ListingRemoteDataSource = ServerRemote()): ListingRepositoryImpl {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ElonUzDatabase.Schema.create(driver)
        return ListingRepositoryImpl(ElonUzDatabase(driver), dispatchers, remote)
    }

    @Test
    fun `publish qilingan e'lon egasining ro'yxatida darrov ko'rinadi`() = runTest(dispatcher) {
        val repo = repository()

        val result = repo.submit(listing())

        assertTrue(result is Resource.Success, "submit muvaffaqiyatsiz: $result")
        val mine = repo.observeMyListings(OWNER).first()
        assertEquals(1, mine.size, "e'lon egasining ro'yxatiga tushmadi")
        // Server bergan id keshga yozilishi shart, aks holda tahrirlash boshqa e'lonni ochadi.
        assertEquals("srv-lst-1", mine.first().id)
        assertEquals(ListingStatus.PENDING_REVIEW, mine.first().status)
    }

    /** Ekran e'lonlarni biznes bo'yicha filtrlaydi — `businessId` saqlanmasa ro'yxat bo'sh ko'rinadi. */
    @Test
    fun `businessId keshda saqlanadi`() = runTest(dispatcher) {
        val repo = repository()

        repo.submit(listing())

        assertEquals("biz-1", repo.observeMyListings(OWNER).first().first().businessId)
    }

    @Test
    fun `qoralama ham ro'yxatda ko'rinadi`() = runTest(dispatcher) {
        val repo = repository()

        repo.save(listing())

        assertEquals(1, repo.observeMyListings(OWNER).first().size)
    }

    /** Server rad etsa keshga hech narsa yozilmasligi kerak — aks holda "arvoh" e'lon qoladi. */
    @Test
    fun `rad etilgan e'lon keshga tushmaydi`() = runTest(dispatcher) {
        val rejecting = object : ListingRemoteDataSource {
            override suspend fun publish(listing: Listing) = Resource.Error("Rad etildi")
            override suspend fun uploadImage(bytes: ByteArray, fileName: String) =
                Resource.Error("Rad etildi")
        }
        val repo = repository(rejecting)

        val result = repo.submit(listing())

        assertTrue(result is Resource.Error)
        assertTrue(repo.observeMyListings(OWNER).first().isEmpty(), "rad etilgan e'lon keshga yozildi")
    }

    private fun listing() = Listing(
        id = "lst-1",
        ownerId = OWNER,
        businessId = "biz-1",
        businessType = BusinessType("TENNIS"),
        businessName = "Kort",
        categoryKey = "OUTDOOR",
        title = "Kort ijarasi",
        images = listOf("https://cdn/x.jpg"),
        priceUnit = PriceUnit.PER_HOUR,
        originalPrice = 100_000,
        discount = ListingDiscount(DiscountType.PERCENT, 20),
        redemption = ListingRedemption(RedemptionMethod.STUDENT_ID),
        branches = listOf(ListingBranch(id = "br-1", lat = 41.3, lng = 69.2, address = "Amir Temur 1")),
        validFrom = 1_700_000_000_000,
        validTo = 1_702_000_000_000,
        createdAt = 0,
        updatedAt = 0,
    )

    private companion object {
        const val OWNER = "user-42"
    }
}
