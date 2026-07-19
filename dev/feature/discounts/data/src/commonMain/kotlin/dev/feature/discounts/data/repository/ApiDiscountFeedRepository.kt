package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.core.common.errorOf
import dev.core.common.error.AppException
import dev.core.common.error.toAppException
import dev.core.common.network.NetworkConnectivity
import dev.core.network.generated.api.DiscountsApi
import dev.core.network.generated.model.DiscountCardDto
import dev.core.network.generated.model.DiscountPageDto
import dev.core.network.generated.model.DiscountSortDto
import dev.core.network.generated.model.DiscountTypeDto
import dev.core.network.generated.model.NearestBranchDto
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.DiscountCard
import dev.feature.discounts.domain.model.DiscountQuery
import dev.feature.discounts.domain.model.DiscountSort
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingDiscount
import dev.feature.discounts.domain.model.NearestBranch
import dev.feature.discounts.domain.repository.DiscountFeedRepository
import io.ktor.client.call.body

/**
 * Talaba qidiruvining **backend** implementatsiyasi — `GET /discounts`.
 *
 * Masofani va eng yaqin filialni server hisoblaydi (`nearestBranch.distanceMeters`),
 * shuning uchun bu yerда qayta hisoblanmaydi.
 *
 * Xato **yutilmaydi** — [Resource.Error] qaytadi va `GetNearbyDiscountsUseCase` local
 * oqimga qaytadi.
 */
class ApiDiscountFeedRepository(
    private val api: DiscountsApi,
    private val connectivity: NetworkConnectivity,
) : DiscountFeedRepository {

    override suspend fun nearby(query: DiscountQuery): Resource<List<DiscountCard>> {
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            val page: DiscountPageDto = api.getDiscounts(
                lat = query.lat,
                lng = query.lng,
                radiusMeters = query.radiusMeters,
                type = query.businessType?.name,
                categoryKey = query.categoryKey,
                query = query.query?.takeIf { it.isNotBlank() },
                sort = query.sort.toDto(),
                page = query.page,
                size = query.size,
            ).body()
            // Turi/nomi noma'lum karta ko'rsatilmaydi — UI ularsiz karta chiza olmaydi.
            Resource.Success(page.items.mapNotNull { it.toDomain() })
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }
}

// ---------------------------------------------------------------------------
// Mapper'lar — DTO → domen
// ---------------------------------------------------------------------------

private fun DiscountSort.toDto(): DiscountSortDto = when (this) {
    DiscountSort.DISTANCE -> DiscountSortDto.DISTANCE
    DiscountSort.DISCOUNT_DESC -> DiscountSortDto.DISCOUNT_DESC
    DiscountSort.NEWEST -> DiscountSortDto.NEWEST
    DiscountSort.POPULAR -> DiscountSortDto.POPULAR
}

/** Backend bizga noma'lum tur yuborsa yoki nom bermasa — karta o'tkazib yuboriladi. */
private fun DiscountCardDto.toDomain(): DiscountCard? {
    val type = businessType?.let { key -> BusinessType.entries.firstOrNull { it.name == key } }
        ?: return null
    val discountType = DiscountType.entries.firstOrNull { it.name == discount.type.value }
        ?: return null

    return DiscountCard(
        id = id,
        title = title,
        businessName = businessName.orEmpty(),
        businessType = type,
        originalPrice = originalPrice,
        discount = ListingDiscount(
            type = discountType,
            value = discount.value,
            conditions = discount.conditions,
        ),
        imageUrl = imageUrl ?: businessLogoUrl,
        nearest = nearestBranch?.toDomain(),
    )
}

/**
 * Filialsiz (masalan onlayn) e'londa koordinata bo'lmasligi mumkin — bunda karta
 * masofasiz ko'rsatiladi.
 */
private fun NearestBranchDto.toDomain(): NearestBranch? {
    val branchLat = lat ?: return null
    val branchLng = lng ?: return null
    return NearestBranch(
        branch = ListingBranch(
            id = id.orEmpty(),
            lat = branchLat,
            lng = branchLng,
            address = address.orEmpty(),
            name = name,
            landmark = landmark,
        ),
        distanceMeters = distanceMeters?.toDouble(),
    )
}
