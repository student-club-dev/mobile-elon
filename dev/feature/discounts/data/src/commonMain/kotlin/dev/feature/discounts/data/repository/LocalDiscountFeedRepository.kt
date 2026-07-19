package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.DiscountCard
import dev.feature.discounts.domain.model.DiscountQuery
import dev.feature.discounts.domain.repository.DiscountFeedRepository

/**
 * Local chegirma feed (`USE_LOCAL_DATA`) — backend'siz. Hozircha bo'sh ro'yxat qaytaradi
 * (talaba tomoni bu ilovaда ikkinchi darajali). Keyin local e'lonlardan chegirma kartalari
 * yig'ib to'ldirilishi mumkin.
 */
class LocalDiscountFeedRepository : DiscountFeedRepository {
    override suspend fun nearby(query: DiscountQuery): Resource<List<DiscountCard>> =
        Resource.Success(emptyList())
}
