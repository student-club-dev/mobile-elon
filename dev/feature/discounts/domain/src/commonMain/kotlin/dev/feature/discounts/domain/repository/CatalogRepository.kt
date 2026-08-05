package dev.feature.discounts.domain.repository

import dev.core.common.Resource
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.CategoryInfo
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.TypeAttributes

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

    /**
     * `GET /business/types/{type}/attributes-schema` — turning **dinamik forma sxemasi**.
     *
     * `categories` dan farqi: bu yerда turning BARCHA e'lonlariga tegishli umumiy maydonlar
     * ham bor ([TypeAttributes.common]), `categories` esa faqat kategoriyaga xoslarini
     * beradi. Forma ikkalasini birlashtiradi.
     *
     * ⚠️ Bu JSON Schema **emas**: backend `AttributeFieldDto` ni qaytaradi, ya'ni
     * `categories` allaqachon beradigan formatni (`DISCOUNTS_BUSINESS_API_RESPONSE.md` §5.2).
     * Shu sabab ilovada bitta parser yetarli.
     */
    suspend fun attributesSchema(type: BusinessType): Resource<TypeAttributes>
}
