package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.core.common.error.toAppException
import dev.core.common.errorOf
import dev.core.common.network.NetworkConnectivity
import dev.core.network.generated.api.CatalogApi
import dev.core.network.generated.model.AttributeFieldDto
import dev.core.network.generated.model.AttributeFieldTypeDto
import dev.core.network.generated.model.AttributesSchemaDto
import dev.core.network.generated.model.BusinessTypeInfoDto
import dev.core.network.generated.model.CategoryDto
import dev.core.network.generated.model.PriceUnitDto
import dev.feature.discounts.domain.model.AttributeKind
import dev.feature.discounts.domain.model.AttributeSpec
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.CategoryInfo
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.TypeAttributes
import dev.feature.discounts.domain.repository.CatalogRepository
import io.ktor.client.call.body

/**
 * Katalogni **backenddan** oladi (`GET /v1/business/types`, `.../categories`).
 *
 * Javob ichma-ich: kategoriya → `fields[]` → `options[]` (PlayStation → Model → PS5/PS4/PS3),
 * shuning uchun kategoriya tanlanganда yangi so'rov ketmaydi.
 *
 * Bu yerда xato **yutilmaydi** — [Resource.Error] qaytadi va `GetBusinessTypesUseCase`
 * klient katalogiga qaytadi.
 */
class ApiCatalogRepository(
    private val api: CatalogApi,
    private val connectivity: NetworkConnectivity,
) : CatalogRepository {

    override suspend fun businessTypes(gender: Gender?): Resource<List<BusinessTypeInfo>> {
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            val body: List<BusinessTypeInfoDto> = api.getBusinessTypes(gender?.toTypesQuery()).body()
            Resource.Success(body.map { it.toDomain() })
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    override suspend fun categories(type: BusinessType, gender: Gender?): Resource<List<CategoryInfo>> {
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            val body: List<CategoryDto> = api.getCategories(type.key, gender?.toCategoriesQuery()).body()
            Resource.Success(body.map { it.toDomain(type) }.sortedBy { it.sortOrder })
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    override suspend fun attributesSchema(type: BusinessType): Resource<TypeAttributes> {
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            val body: AttributesSchemaDto = api.getAttributesSchema(type.key).body()
            Resource.Success(
                TypeAttributes(
                    // Server javobidagi turni ishlatamiz, so'ralganini emas: ular bir xil
                    // bo'lishi kerak, lekin haqiqat manbai — server.
                    businessType = BusinessType(body.businessType),
                    common = body.common.map { it.toDomain() },
                    byCategory = body.byCategory.associate { it.categoryKey to it.fields.map { f -> f.toDomain() } },
                ),
            )
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }
}

// ---------------------------------------------------------------------------
// Mapper'lar — DTO ↔ domen
// ---------------------------------------------------------------------------

// Generator har bir so'rov uchun alohida query-enum chiqaradi, shuning uchun ikkita mapper.
private fun Gender.toTypesQuery(): CatalogApi.GenderGetBusinessTypes = when (this) {
    Gender.MALE -> CatalogApi.GenderGetBusinessTypes.MALE
    Gender.FEMALE -> CatalogApi.GenderGetBusinessTypes.FEMALE
}

private fun Gender.toCategoriesQuery(): CatalogApi.GenderGetCategories = when (this) {
    Gender.MALE -> CatalogApi.GenderGetCategories.MALE
    Gender.FEMALE -> CatalogApi.GenderGetCategories.FEMALE
}

/**
 * Serverdagi **har** tur o'tadi — hech biri tashlab yuborilmaydi.
 *
 * Ilgari bu yerda tur klient enum'iga solishtirilar va topilmasa `null` qaytarilardi:
 * natijada serverdagi 27 turdan faqat ilova biladigan bir nechtasi ko'rinardi. Endi tur —
 * ochiq kalit, zaxira katalog esa faqat serverda **bo'lmagan** maydonlarni to'ldiradi.
 */
private fun BusinessTypeInfoDto.toDomain(): BusinessTypeInfo {
    val domainType = BusinessType(type)
    val fallback = ListingCatalog.info(domainType)
    return BusinessTypeInfo(
        type = domainType,
        nameUz = nameUz,
        emoji = emoji ?: fallback?.emoji.orEmpty(),
        accentColor = accentColor?.toAccentLong() ?: fallback?.accentColor ?: DEFAULT_ACCENT,
        defaultPriceUnit = defaultPriceUnit.toDomain() ?: ListingCatalog.defaultPriceUnit(domainType),
        priceUnits = priceUnits.mapNotNull { it.toDomain() }.takeIf { it.isNotEmpty() }
            ?: ListingCatalog.priceUnits(domainType),
    )
}

/** Neytral urg'u rangi — server rang bermasa va tur zaxirada ham bo'lmasa. */
private const val DEFAULT_ACCENT = 0xFF7C5CFF

private fun CategoryDto.toDomain(type: BusinessType): CategoryInfo = CategoryInfo(
    key = key,
    nameUz = nameUz,
    sortOrder = sortOrder,
    // Backend maydon bermasa — klient katalogidan olamiz.
    fields = fields.map { it.toDomain() }.ifEmpty { ListingCatalog.categoryAttributes(type, key) },
    requiresCustomName = requiresCustomName ?: (key == ListingCatalog.OTHER_KEY),
)

private fun AttributeFieldDto.toDomain(): AttributeSpec = AttributeSpec(
    key = key,
    label = label,
    kind = type.toDomain(),
    hint = hint.orEmpty(),
    options = options?.map { it.value }.orEmpty(),
    required = required,
    suffix = suffix,
)

private fun AttributeFieldTypeDto.toDomain(): AttributeKind = when (this) {
    AttributeFieldTypeDto.TEXT -> AttributeKind.TEXT
    AttributeFieldTypeDto.NUMBER -> AttributeKind.NUMBER
    AttributeFieldTypeDto.BOOLEAN -> AttributeKind.BOOLEAN
    AttributeFieldTypeDto.SELECT -> AttributeKind.SELECT
    AttributeFieldTypeDto.MULTI_SELECT -> AttributeKind.MULTI_SELECT
    AttributeFieldTypeDto.TAGS -> AttributeKind.TAGS
}

private fun PriceUnitDto.toDomain(): PriceUnit? = PriceUnit.entries.firstOrNull { it.name == value }

/** `"#7C5CFF"` → `0xFF7C5CFF`. Noto'g'ri format bo'lsa `null`. */
private fun String.toAccentLong(): Long? =
    removePrefix("#").takeIf { it.length == 6 }?.toLongOrNull(16)?.or(0xFF000000L)
