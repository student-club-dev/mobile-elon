package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.CategoryInfo
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.repository.CatalogRepository

/**
 * Katalog UseCase'lari — **backend + zaxira** naqshi.
 *
 * Qoida: katalog e'lon qo'yishning **birinchi qadami**, shuning uchun u hech qachon
 * "ishlamay qolmasligi" kerak. Backend o'chgan/xato bergan/hali yozilmagan bo'lsa —
 * klientдаgi [ListingCatalog] dan **fake ma'lumot** qaytadi va foydalanuvchi ishini
 * davom ettiraveradi. Ekran hech qachon bo'sh yoki xato ko'rsatmaydi.
 *
 * Backend tayyor bo'lganда hech narsa o'zgartirilmaydi — javob kelsa, o'zi ishlatiladi.
 */

/** Biznes turlari. Backend ishlamasa — klient katalogi (jinsga qarab filtrlangan). */
class GetBusinessTypesUseCase(
    private val repository: CatalogRepository,
) {
    suspend operator fun invoke(gender: Gender?): List<BusinessTypeInfo> {
        val remote = runCatching { repository.businessTypes(gender) }.getOrNull()
        val fromApi = (remote as? Resource.Success)?.data.orEmpty()
        return fromApi.ifEmpty { fake(gender) }
    }

    /** Zaxira — klientдаgi katalog. */
    private fun fake(gender: Gender?): List<BusinessTypeInfo> =
        ListingCatalog.typesForGender(gender).map { BusinessTypeInfo.from(it) }
}

/** Kategoriyalar + maydonlari. Backend ishlamasa — klient katalogi. */
class GetCategoriesUseCase(
    private val repository: CatalogRepository,
) {
    suspend operator fun invoke(type: BusinessType, gender: Gender?): List<CategoryInfo> {
        val remote = runCatching { repository.categories(type, gender) }.getOrNull()
        val fromApi = (remote as? Resource.Success)?.data.orEmpty()
        return fromApi.ifEmpty { fake(type, gender) }
    }

    /** Zaxira — klientдаgi katalog (kiyimда jinsga xos kategoriyalar). */
    private fun fake(type: BusinessType, gender: Gender?): List<CategoryInfo> =
        ListingCatalog.categoriesFor(type, gender)
            .mapIndexed { i, c -> CategoryInfo.from(type, c, i) }
}
