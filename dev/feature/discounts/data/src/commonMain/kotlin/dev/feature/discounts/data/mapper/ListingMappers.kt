package dev.feature.discounts.data.mapper

import dev.core.database.sql.ListingEntity
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingDiscount
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingRedemption
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.OptionGroup
import dev.feature.discounts.domain.model.OptionItem
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.RedemptionMethod
import dev.feature.discounts.domain.model.RedemptionPeriod
import dev.feature.discounts.domain.model.SelectionType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Local DB (SQLDelight) ↔ domen modeli.
 *
 * Ro'yxatli maydonlar (rasmlar, atributlar, qo'shimchalar) JSON matn ustunlarida saqlanadi —
 * shunda har yangi biznes turi qo'shilganda sxema o'zgarmaydi.
 */
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
private data class OptionGroupJson(
    val name: String,
    val selectionType: String = "SINGLE",
    val isRequired: Boolean = false,
    val minSelect: Int? = null,
    val maxSelect: Int? = null,
    val options: List<OptionJson> = emptyList(),
)

@Serializable
private data class OptionJson(
    val name: String,
    val priceDelta: Long = 0,
    val isAvailable: Boolean = true,
)

@Serializable
private data class BranchJson(
    val id: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val name: String? = null,
    val landmark: String? = null,
    val regionId: String? = null,
    val districtId: String? = null,
)

fun ListingEntity.toDomain(): Listing = Listing(
    id = id,
    ownerId = ownerId,
    businessId = businessId,
    // Tur — backend katalogidagi ochiq kalit, shuning uchun hech narsa "noma'lum"ga
    // tushmaydi: bazadagi qiymat qanday bo'lsa shunday qaytadi.
    businessType = BusinessType(businessType),
    businessName = businessName,
    categoryKey = categoryKey,
    customCategoryName = customCategoryName,
    title = title,
    description = description,
    images = imagesJson.decodeList(),
    priceUnit = priceUnit.toEnum(PriceUnit.entries, PriceUnit.PER_ITEM),
    originalPrice = originalPrice,
    currency = currency,
    discount = ListingDiscount(
        type = discountType.toEnum(DiscountType.entries, DiscountType.PERCENT),
        value = discountValue,
        conditions = discountConditions,
    ),
    redemption = ListingRedemption(
        method = redemptionMethod.toEnum(RedemptionMethod.entries, RedemptionMethod.STUDENT_ID),
        promoCode = promoCode,
        url = redemptionUrl,
        perUserLimit = perUserLimit?.toInt(),
        perUserPeriod = perUserPeriod.toEnum(RedemptionPeriod.entries, RedemptionPeriod.DAY),
        totalLimit = totalLimit?.toInt(),
        usedCount = usedCount.toInt(),
    ),
    branches = branchesJson.decodeBranches(),
    validFrom = validFrom,
    validTo = validTo,
    attributes = attributesJson.decodeMap(),
    optionGroups = optionGroupsJson.decodeGroups(),
    status = status.toEnum(ListingStatus.entries, ListingStatus.DRAFT),
    rejectionReason = rejectionReason,
    viewsCount = viewsCount.toInt(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/** Domen → DB ustunlari. `finalPrice` shu yerda hisoblanadi (domen formulasi bilan). */
fun Listing.toEntity(): ListingEntity = ListingEntity(
    id = id,
    ownerId = ownerId,
    businessId = businessId,
    businessType = businessType.key,
    businessName = businessName,
    categoryKey = categoryKey,
    customCategoryName = customCategoryName,
    title = title,
    description = description,
    imagesJson = json.encodeToString(images),
    priceUnit = priceUnit.name,
    originalPrice = originalPrice,
    currency = currency,
    discountType = discount.type.name,
    discountValue = discount.value,
    finalPrice = finalPrice,
    discountConditions = discount.conditions,
    redemptionMethod = redemption.method.name,
    promoCode = redemption.promoCode,
    redemptionUrl = redemption.url,
    perUserLimit = redemption.perUserLimit?.toLong(),
    perUserPeriod = redemption.perUserPeriod.name,
    totalLimit = redemption.totalLimit?.toLong(),
    usedCount = redemption.usedCount.toLong(),
    branchesJson = json.encodeToString(
        branches.map { branch ->
            BranchJson(
                id = branch.id,
                lat = branch.lat,
                lng = branch.lng,
                address = branch.address,
                name = branch.name,
                landmark = branch.landmark,
                regionId = branch.regionId,
                districtId = branch.districtId,
            )
        },
    ),
    validFrom = validFrom,
    validTo = validTo,
    attributesJson = json.encodeToString(attributes),
    optionGroupsJson = json.encodeToString(
        optionGroups.map { group ->
            OptionGroupJson(
                name = group.name,
                selectionType = group.selectionType.name,
                isRequired = group.isRequired,
                minSelect = group.minSelect,
                maxSelect = group.maxSelect,
                options = group.options.map { OptionJson(it.name, it.priceDelta, it.isAvailable) },
            )
        },
    ),
    status = status.name,
    rejectionReason = rejectionReason,
    viewsCount = viewsCount.toLong(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private inline fun <reified T : Enum<T>> String.toEnum(entries: List<T>, fallback: T): T =
    entries.firstOrNull { it.name == this } ?: fallback

private fun String.decodeList(): List<String> =
    runCatching { json.decodeFromString<List<String>>(this) }.getOrDefault(emptyList())

private fun String.decodeMap(): Map<String, String> =
    runCatching { json.decodeFromString<Map<String, String>>(this) }.getOrDefault(emptyMap())

private fun String.decodeBranches(): List<ListingBranch> =
    runCatching { json.decodeFromString<List<BranchJson>>(this) }
        .getOrDefault(emptyList())
        .map { branch ->
            ListingBranch(
                id = branch.id,
                lat = branch.lat,
                lng = branch.lng,
                address = branch.address,
                name = branch.name,
                landmark = branch.landmark,
                regionId = branch.regionId,
                districtId = branch.districtId,
            )
        }

private fun String.decodeGroups(): List<OptionGroup> =
    runCatching { json.decodeFromString<List<OptionGroupJson>>(this) }
        .getOrDefault(emptyList())
        .map { group ->
            OptionGroup(
                name = group.name,
                selectionType = group.selectionType.toEnum(SelectionType.entries, SelectionType.SINGLE),
                isRequired = group.isRequired,
                minSelect = group.minSelect,
                maxSelect = group.maxSelect,
                options = group.options.map { OptionItem(it.name, it.priceDelta, it.isAvailable) },
            )
        }
