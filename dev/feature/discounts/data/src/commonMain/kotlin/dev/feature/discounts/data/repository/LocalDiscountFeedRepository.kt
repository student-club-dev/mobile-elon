package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.DiscountCard
import dev.feature.discounts.domain.model.DiscountQuery
import dev.feature.discounts.domain.repository.DiscountFeedRepository

/**
 * Chegirma feed'ining vaqtinchalik o'rni — bu ilovaning spec'ida `GET /discounts` yo'q
 * (talaba tomoni alohida ilova). **Bo'sh** ro'yxat qaytaradi: soxta karta yasamaydi.
 * Endpoint qo'shilgach o'rniga `ApiDiscountFeedRepository` bog'lanadi.
 */
class LocalDiscountFeedRepository : DiscountFeedRepository {
    override suspend fun nearby(query: DiscountQuery): Resource<List<DiscountCard>> =
        Resource.Success(emptyList())
}
