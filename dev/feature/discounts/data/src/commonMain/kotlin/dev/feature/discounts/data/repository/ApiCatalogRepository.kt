package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.core.common.errorOf
import dev.core.common.error.toAppException
import dev.core.common.network.NetworkConnectivity
import dev.core.network.generated.api.BusinessApi
import dev.core.network.generated.model.AttributeFieldDto
import dev.core.network.generated.model.AttributeFieldTypeDto
import dev.core.network.generated.model.BusinessTypeInfoDto
import dev.core.network.generated.model.CategoryDto
import dev.core.network.generated.model.GenderDto
import dev.core.network.generated.model.PriceUnitDto
import dev.feature.discounts.domain.model.AttributeKind
import dev.feature.discounts.domain.model.AttributeSpec
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.CategoryInfo
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.repository.CatalogRepository
import io.ktor.client.call.body

/**
 * Katalogni **backenddan** oladi (`GET /business/types`, `.../categories`).
 *
 * Javob ichma-ich: kategoriya → `fields[]` → `options[]` (PlayStation → Model → PS5/PS4/PS3),
 * shuning uchun kategoriya tanlanganда yangi so'rov ketmaydi.
 *
 * Bu yerда xato **yutilmaydi** — [Resource.Error] qaytadi va `GetBusinessTypesUseCase`
 * klient katalogiga qaytadi.
 */
class ApiCatalogRepository(
    private val api: BusinessApi,
    private val connectivity: NetworkConnectivity,
) : CatalogRepository {

    override suspend fun businessTypes(gender: Gender?): Resource<List<BusinessTypeInfo>> {
        if (!connectivity.isOnline()) return errorOf(dev.core.common.error.AppException.NoInternet())
        return try {
            val body: List<BusinessTypeInfoDto> = api.getBusinessTypes(gender?.toDto()).body()
            Resource.Success(body.mapNotNull { it.toDomain() })
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    override suspend fun categories(type: BusinessType, gender: Gender?): Resource<List<CategoryInfo>> {
        if (!connectivity.isOnline()) return errorOf(dev.core.common.error.AppException.NoInternet())
        return try {
            val body: List<CategoryDto> = api.getCategoriesByBusinessType(type.toDto(), gender?.toDto()).body()
            Resource.Success(body.map { it.toDomain(type) }.sortedBy { it.sortOrder })
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }
}

// ---------------------------------------------------------------------------
// Mapper'lar — DTO ↔ domen
// ---------------------------------------------------------------------------

private fun Gender.toDto(): GenderDto = when (this) {
    Gender.MALE -> GenderDto.MALE
    Gender.FEMALE -> GenderDto.FEMALE
}

// Tur endi ochiq string (spec'да enum emas) — domen enum nomi to'g'ridan-to'g'ri kalit.
private fun BusinessType.toDto(): String = name

/** Backend bizga noma'lum tur yuborsa — o'tkazib yuboriladi (klient enum'ида yo'q). */
private fun BusinessTypeInfoDto.toDomain(): BusinessTypeInfo? {
    val domainType = BusinessType.entries.firstOrNull { it.name == type } ?: return null
    return BusinessTypeInfo(
        type = domainType,
        nameUz = nameUz,
        emoji = emoji ?: domainType.emoji,
        accentColor = accentColor?.toAccentLong() ?: domainType.accent,
        defaultPriceUnit = defaultPriceUnit?.toDomain() ?: domainType.defaultPriceUnit,
        priceUnits = priceUnits?.mapNotNull { it.toDomain() }?.takeIf { it.isNotEmpty() }
            ?: ListingCatalog.priceUnits(domainType),
    )
}

private fun CategoryDto.toDomain(type: BusinessType): CategoryInfo = CategoryInfo(
    key = key,
    nameUz = nameUz,
    sortOrder = sortOrder ?: 0,
    // Backend maydon bermasa — klient katalogidan olamiz (eski backend bilan ham ishlasin).
    fields = fields?.map { it.toDomain() } ?: ListingCatalog.categoryAttributes(type, key),
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
