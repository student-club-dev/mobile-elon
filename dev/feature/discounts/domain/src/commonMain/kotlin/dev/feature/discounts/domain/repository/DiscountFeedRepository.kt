package dev.feature.discounts.domain.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.DiscountCard
import dev.feature.discounts.domain.model.DiscountQuery

/**
 * Talaba tomonidagi chegirmalar oqimi — `GET /discounts`.
 *
 * [ListingRepository] biznes egasining e'lonlari bilan ishlaydi (local, offline-first);
 * bu esa **talabaga** ko'rinadigan faol chegirmalarni serverdan qidiradi: yaqinlik, radius,
 * tur/kategoriya va saralash bo'yicha. Bunday qidiruvni klientда qilib bo'lmaydi — barcha
 * bizneslarning e'lonlari qurilmaда yo'q.
 */
interface DiscountFeedRepository {

    /** Berilgan filtr bo'yicha chegirmalar sahifasi. Xato yutilmaydi — UseCase zaxiraga o'tadi. */
    suspend fun nearby(query: DiscountQuery): Resource<List<DiscountCard>>
}
