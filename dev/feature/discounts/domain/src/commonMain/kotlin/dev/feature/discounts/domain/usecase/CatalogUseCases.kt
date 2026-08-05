package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.CategoryInfo
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.TypeAttributes
import dev.feature.discounts.domain.repository.CatalogRepository

/**
 * Katalog UseCase'lari — **faqat backend** (`GET /business/types`, `.../categories`).
 *
 * Klient katalogiga qaytish (fake) olib tashlandi: uning kalitlari serverникidan farq qilsa,
 * shu kalit bilan yaratilgan e'lonni server rad etardi, foydalanuvchi esa sababini ko'rmasdi.
 * So'rov uzilsa ro'yxat **bo'sh** qaytadi va ekran shuni ko'rsatadi.
 *
 * [ListingCatalog] o'chirilmagan — u forma taksonomiyasi (atributlar, narx birliklari,
 * validatsiya) uchun kerak, lekin endi server javobining **o'rnini bosmaydi**.
 */

/** Biznes turlari (jinsga qarab filtrlangan). */
class GetBusinessTypesUseCase(
    private val repository: CatalogRepository,
) {
    suspend operator fun invoke(gender: Gender?): List<BusinessTypeInfo> {
        val remote = runCatching { repository.businessTypes(gender) }.getOrNull()
        return (remote as? Resource.Success)?.data.orEmpty()
    }
}

/** Kategoriyalar + maydonlari. */
class GetCategoriesUseCase(
    private val repository: CatalogRepository,
) {
    suspend operator fun invoke(type: BusinessType, gender: Gender?): List<CategoryInfo> {
        val remote = runCatching { repository.categories(type, gender) }.getOrNull()
        return (remote as? Resource.Success)?.data.orEmpty()
    }
}

/**
 * Turning dinamik forma sxemasi (`GET /business/types/{type}/attributes-schema`).
 *
 * Bu yerдаgi zaxira **bo'sh sxema**, klient katalogi emas: kategoriya maydonlarini forma
 * allaqachon `GetCategoriesUseCase` dan oladi, bu so'rov esa faqat **umumiy** maydonlarni
 * qo'shadi. So'rov uzilsa forma avvalgidek — kategoriya maydonlari bilan — ishlashda davom
 * etadi, ya'ni yangi endpoint hech qachon e'lon qo'yishga to'sqinlik qilmaydi.
 */
class GetTypeAttributesUseCase(
    private val repository: CatalogRepository,
) {
    suspend operator fun invoke(type: BusinessType): TypeAttributes {
        val remote = runCatching { repository.attributesSchema(type) }.getOrNull()
        return (remote as? Resource.Success)?.data ?: TypeAttributes.empty(type)
    }
}
