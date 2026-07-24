package dev.feature.discounts.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.core.common.AppDispatchers
import dev.core.common.Resource
import dev.core.database.sql.ElonUzDatabase
import dev.feature.discounts.data.mapper.toDomain
import dev.feature.discounts.data.mapper.toEntity
import dev.feature.discounts.data.remote.ListingRemoteDataSource
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingPage
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.repository.ListingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Offline-first e'lon repository'si.
 *
 * UI **faqat** local DB'ni kuzatadi — shu sabab e'lon tarmoqsiz ham darrov ro'yxatda paydo
 * bo'ladi. [submit] avval masofaviy manbaga yuboradi (yoqilgan bo'lsa), keyin qaytgan
 * natijani (server bergan id va status) keshga yozadi.
 */
class ListingRepositoryImpl(
    private val db: ElonUzDatabase,
    private val dispatchers: AppDispatchers,
    private val remote: ListingRemoteDataSource,
) : ListingRepository {

    private val q get() = db.listingQueries

    override fun observeMyListings(ownerId: String): Flow<List<Listing>> =
        q.selectByOwner(ownerId)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows -> rows.map { it.toDomain() } }

    // Serverdan paginatsiyalab oladi — local kesh o'rniga to'g'ridan-to'g'ri masofaviy manba
    // (biznes ochilganда to'liq ro'yxat, "Mening e'lonlarim"). Mapping data source ichida.
    override suspend fun listForBusiness(
        business: Business,
        status: ListingStatus?,
        categoryKey: String?,
        page: Int,
        size: Int,
    ): Resource<ListingPage> {
        val res = remote.list(business, status, categoryKey, page, size)
        // Serverdan kelgan e'lonlarni local bazaga yozamiz: tahrirlashда [byId] ularni topadi
        // va offline'да ham ko'rinadi (server ro'yxati bir martalik, o'zi saqlanmaydi).
        if (res is Resource.Success) res.data.items.forEach { cache(it) }
        return res
    }

    override fun observeActive(): Flow<List<Listing>> =
        q.selectActive(now = Clock.System.now().toEpochMilliseconds())
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun byId(id: String): Listing? = withContext(dispatchers.io) {
        q.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(listing: Listing): Resource<Listing> {
        val draft = listing.copy(updatedAt = now())
        cache(draft)
        return Resource.Success(draft)
    }

    override suspend fun submit(listing: Listing): Resource<Listing> {
        // Avval masofaviy manba — u id/status'ni o'zgartirishi mumkin (server DRAFT ni
        // PENDING_REVIEW ga o'tkazadi va o'z id'sini beradi). Keshga o'shani yozamiz,
        // aks holda local va serverdagi e'lon ikkiga bo'linib ketadi.
        return when (val res = remote.publish(listing.copy(updatedAt = now()))) {
            is Resource.Success -> {
                val published = res.data.copy(updatedAt = now())
                cache(published)
                Resource.Success(published)
            }
            is Resource.Error -> res
            Resource.Loading -> Resource.Error("E'lonni yuborib bo'lmadi")
        }
    }

    override suspend fun update(listing: Listing): Resource<Listing> {
        // Avval server (`PUT /listings/{id}`) — u statusni o'zgartirishi mumkin; qaytgan e'lonni
        // keshga yozamiz. Serverга yetib bo'lmasa Fallback local'ni ishlatadi.
        return when (val res = remote.update(listing.copy(updatedAt = now()))) {
            is Resource.Success -> {
                val updated = res.data.copy(updatedAt = now())
                cache(updated)
                Resource.Success(updated)
            }
            is Resource.Error -> res
            Resource.Loading -> Resource.Error("E'lonni tahrirlab bo'lmadi")
        }
    }

    override suspend fun updateStatus(id: String, status: ListingStatus): Resource<Unit> =
        withContext(dispatchers.io) {
            q.updateStatus(status = status.name, updatedAt = now(), id = id)
            Resource.Success(Unit)
        }

    override suspend fun delete(id: String): Resource<Unit> {
        // Serverда arxivlaymiz (`DELETE /listings/{id}`). Muvaffaqiyatli bo'lса — local'дан ham
        // o'chiramiz; server rad etса (403/404) local'да qoldiramiz va xatoni qaytaramiz.
        return when (val res = remote.archive(id)) {
            is Resource.Success -> withContext(dispatchers.io) {
                q.deleteById(id)
                Resource.Success(Unit)
            }
            is Resource.Error -> res
            Resource.Loading -> Resource.Error("E'lonni o'chirib bo'lmadi")
        }
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String> =
        remote.uploadImage(bytes, fileName)

    private suspend fun cache(listing: Listing) = withContext(dispatchers.io) {
        val e = listing.toEntity()
        q.upsert(
            id = e.id,
            ownerId = e.ownerId,
            businessId = e.businessId,
            businessType = e.businessType,
            businessName = e.businessName,
            categoryKey = e.categoryKey,
            customCategoryName = e.customCategoryName,
            title = e.title,
            description = e.description,
            imagesJson = e.imagesJson,
            priceUnit = e.priceUnit,
            originalPrice = e.originalPrice,
            currency = e.currency,
            discountType = e.discountType,
            discountValue = e.discountValue,
            finalPrice = e.finalPrice,
            discountConditions = e.discountConditions,
            redemptionMethod = e.redemptionMethod,
            promoCode = e.promoCode,
            redemptionUrl = e.redemptionUrl,
            perUserLimit = e.perUserLimit,
            perUserPeriod = e.perUserPeriod,
            totalLimit = e.totalLimit,
            usedCount = e.usedCount,
            branchesJson = e.branchesJson,
            validFrom = e.validFrom,
            validTo = e.validTo,
            attributesJson = e.attributesJson,
            optionGroupsJson = e.optionGroupsJson,
            status = e.status,
            rejectionReason = e.rejectionReason,
            viewsCount = e.viewsCount,
            createdAt = e.createdAt,
            updatedAt = e.updatedAt,
        )
    }

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()
}
