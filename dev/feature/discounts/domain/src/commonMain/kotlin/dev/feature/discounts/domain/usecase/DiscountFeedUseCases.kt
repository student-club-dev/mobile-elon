package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.feature.discounts.domain.model.DiscountCard
import dev.feature.discounts.domain.model.DiscountQuery
import dev.feature.discounts.domain.model.DiscountSort
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.repository.DiscountFeedRepository
import dev.feature.discounts.domain.repository.ListingRepository
import kotlinx.coroutines.flow.first

/**
 * Talabaga ko'rinadigan chegirmalar — **backend + zaxira** naqshi.
 *
 * Asosiy manba `GET /discounts`: faqat server barcha bizneslarning e'lonlarini biladi va
 * masofa/radius bo'yicha qidira oladi. Backend javob bermasa — shu qurilmadagi local faol
 * e'lonlar ko'rsatiladi (eng yaqin filial va masofa klientда hisoblanadi), shuning uchun
 * ekran hech qachon bo'sh qolmaydi.
 */
class GetNearbyDiscountsUseCase(
    private val feedRepository: DiscountFeedRepository,
    private val listingRepository: ListingRepository,
) {
    suspend operator fun invoke(query: DiscountQuery): List<DiscountCard> {
        val remote = runCatching { feedRepository.nearby(query) }.getOrNull()
        (remote as? Resource.Success)?.data?.let { if (it.isNotEmpty()) return it }
        return fallback(query)
    }

    /**
     * Zaxira — local faol e'lonlar; filtr, saralash va radius klient tomonда qo'llanadi.
     *
     * Saralash ikki bosqichда: `NEWEST`/`POPULAR` e'lonning o'zidagi maydonlarga tayanadi
     * ([DiscountCard] да ular yo'q), `DISTANCE`/`DISCOUNT_DESC` esa kartaga aylangandan
     * keyin hisoblanadi.
     */
    private suspend fun fallback(query: DiscountQuery): List<DiscountCard> {
        val text = query.query?.takeIf { it.isNotBlank() }

        val listings = listingRepository.observeActive().first()
            .filter { query.businessType == null || it.businessType == query.businessType }
            .filter { query.categoryKey == null || it.categoryKey == query.categoryKey }
            .filter { text == null || it.title.contains(text, ignoreCase = true) }
            .sortedByDescending { it.createdAt }

        val cards = listings
            .map { it.toCard(query.lat, query.lng) }
            .filter { card ->
                // Masofa noma'lum bo'lsa kartani tashlab yubormaymiz — joylashuvsiz ham
                // ro'yxat ishlashi kerak.
                val meters = card.nearest?.distanceMeters ?: return@filter true
                meters <= query.radiusMeters
            }

        val sorted = when (query.sort) {
            // Local'да ko'rish soni yo'q — POPULAR uchun ham eng yangi tartib qoladi.
            DiscountSort.NEWEST, DiscountSort.POPULAR -> cards
            DiscountSort.DISTANCE -> cards.sortedBy { it.nearest?.distanceMeters ?: Double.MAX_VALUE }
            DiscountSort.DISCOUNT_DESC -> cards.sortedByDescending { it.originalPrice - it.finalPrice }
        }
        return sorted.take(query.size)
    }

    private fun Listing.toCard(userLat: Double?, userLng: Double?) = DiscountCard(
        id = id,
        title = title,
        businessName = businessName,
        businessType = businessType,
        originalPrice = originalPrice,
        discount = discount,
        imageUrl = images.firstOrNull(),
        nearest = nearestBranch(userLat, userLng),
    )
}
