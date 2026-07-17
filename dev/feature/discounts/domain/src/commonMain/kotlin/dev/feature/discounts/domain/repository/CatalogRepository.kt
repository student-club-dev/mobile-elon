package dev.feature.discounts.domain.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.CategoryInfo
import dev.feature.discounts.domain.model.Gender

/**
 * Katalog manbasi — biznes turlari va kategoriyalar **backenddan**.
 *
 * Xato bo'lsa [Resource.Error] qaytaradi; nima qilishni UseCase hal qiladi
 * (`GetBusinessTypesUseCase` — klient katalogiga qaytadi).
 */
interface CatalogRepository {

    /** `GET /business/types?gender=` — jinsga mos turlar. */
    suspend fun businessTypes(gender: Gender?): Resource<List<BusinessTypeInfo>>

    /** `GET /business/types/{type}/categories?gender=` — kategoriyalar + maydonlari. */
    suspend fun categories(type: BusinessType, gender: Gender?): Resource<List<CategoryInfo>>
}
