package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.CategoryInfo
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.repository.CatalogRepository

/**
 * Local katalog (`USE_LOCAL_DATA`) — backend'siz. Ma'lumot klientdagi qattiq kodlangan
 * [ListingCatalog] dan olinadi (turlar, kategoriyalar, atributlar, narx birliklari).
 */
class LocalCatalogRepository : CatalogRepository {

    override suspend fun businessTypes(gender: Gender?): Resource<List<BusinessTypeInfo>> =
        Resource.Success(
            BusinessType.entries
                .filter { availableFor(it, gender) }
                .map { BusinessTypeInfo.from(it) },
        )

    override suspend fun categories(type: BusinessType, gender: Gender?): Resource<List<CategoryInfo>> =
        Resource.Success(
            ListingCatalog.categoriesFor(type, gender)
                .mapIndexed { index, category -> CategoryInfo.from(type, category, index) },
        )

    // Sartaroshxona — ayolларга, go'zallik saloni — erkaklarга ko'rsatilmaydi.
    private fun availableFor(type: BusinessType, gender: Gender?): Boolean = when (gender) {
        Gender.MALE -> type != BusinessType.BEAUTY_SALON
        Gender.FEMALE -> type != BusinessType.BARBERSHOP
        null -> true
    }
}
