package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.feature.discounts.domain.model.DiscountCard
import dev.feature.discounts.domain.model.DiscountQuery
import dev.feature.discounts.domain.repository.DiscountFeedRepository

/**
 * Talabaga ko'rinadigan chegirmalar — **faqat backend** (`GET /discounts`).
 *
 * Faqat server barcha bizneslarning e'lonlarini biladi va masofa/radius bo'yicha qidira oladi.
 * Ilgari zaxira sifatida **shu qurilmadagi** local e'lonlar ko'rsatilardi — bu talabaga
 * boshqa bizneslar chegirmasi kabi ko'rinar, aslida esa faqat o'zi yozgan e'lonlar edi.
 * Endi javob bo'lmasa ro'yxat bo'sh qaytadi.
 */
class GetNearbyDiscountsUseCase(
    private val feedRepository: DiscountFeedRepository,
) {
    suspend operator fun invoke(query: DiscountQuery): List<DiscountCard> {
        val remote = runCatching { feedRepository.nearby(query) }.getOrNull()
        return (remote as? Resource.Success)?.data.orEmpty()
    }
}
