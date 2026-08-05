package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.CategoryInfo
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.TypeAttributes
import dev.feature.discounts.domain.repository.CatalogRepository

/**
 * Local katalog (`USE_LOCAL_DATA`) — backend'siz. Ma'lumot klientdagi qattiq kodlangan
 * [ListingCatalog] dan olinadi (turlar, kategoriyalar, atributlar, narx birliklari).
 */
class LocalCatalogRepository : CatalogRepository {

    // Jins bo'yicha filtr (sartaroshxona ayollarga, go'zallik saloni erkaklarga
    // ko'rsatilmaydi) katalogning o'zida — backenddagi `availableForGenders` bilan bir xil.
    override suspend fun businessTypes(gender: Gender?): Resource<List<BusinessTypeInfo>> =
        Resource.Success(ListingCatalog.typesForGender(gender).map { BusinessTypeInfo.from(it) })

    override suspend fun categories(type: BusinessType, gender: Gender?): Resource<List<CategoryInfo>> =
        Resource.Success(
            ListingCatalog.categoriesFor(type, gender)
                .mapIndexed { index, category -> CategoryInfo.from(type, category, index) },
        )

    /**
     * Klient katalogida turga umumiy maydonlar tushunchasi yo'q — hamma maydon kategoriyaga
     * bog'langan. Shuning uchun `common` bo'sh qaytadi va forma faqat kategoriya
     * maydonlarini ko'rsatadi (backendsiz rejimdagi ilgarigi xatti-harakat).
     */
    override suspend fun attributesSchema(type: BusinessType): Resource<TypeAttributes> =
        Resource.Success(
            TypeAttributes(
                businessType = type,
                byCategory = ListingCatalog.categoriesFor(type, gender = null)
                    .associate { it.key to ListingCatalog.categoryAttributes(type, it.key) },
            ),
        )
}
