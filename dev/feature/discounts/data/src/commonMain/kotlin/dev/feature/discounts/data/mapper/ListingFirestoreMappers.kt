package dev.feature.discounts.data.mapper

import dev.feature.discounts.data.dto.ListingFirestoreDto
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingDiscount
import dev.feature.discounts.domain.model.ListingRedemption
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.OptionGroup
import dev.feature.discounts.domain.model.OptionItem
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.RedemptionMethod
import dev.feature.discounts.domain.model.SelectionType

/**
 * Domen [Listing] ↔ Firestore [ListingFirestoreDto].
 *
 * Enum'lar `.name` bilan yoziladi va [enumOr] orqali xavfsiz o'qiladi — noma'lum/eski qiymat
 * bo'lsa e'lon yo'qolmaydi, oqilona standart qiymatga tushadi (SQLDelight mapper'i bilan bir xil).
 */

fun Listing.toFirestoreDto(): ListingFirestoreDto = ListingFirestoreDto(
    id = id,
    ownerId = ownerId,
    businessId = businessId,
    businessType = businessType.name,
    businessName = businessName,
    categoryKey = categoryKey,
    customCategoryName = customCategoryName,
    title = title,
    description = description,
    images = images,
    priceUnit = priceUnit.name,
    originalPrice = originalPrice,
    currency = currency,
    finalPrice = finalPrice,
    isDiscount = isDiscount,
    discount = ListingFirestoreDto.DiscountDto(
        type = discount.type.name,
        value = discount.value,
        conditions = discount.conditions,
        appliesToOptions = discount.appliesToOptions,
    ),
    redemption = ListingFirestoreDto.RedemptionDto(
        method = redemption.method.name,
        promoCode = redemption.promoCode,
        perUserLimit = redemption.perUserLimit,
        totalLimit = redemption.totalLimit,
        usedCount = redemption.usedCount,
    ),
    branches = branches.map { b ->
        ListingFirestoreDto.BranchDto(
            id = b.id,
            lat = b.lat,
            lng = b.lng,
            address = b.address,
            name = b.name,
            landmark = b.landmark,
            regionId = b.regionId,
            districtId = b.districtId,
        )
    },
    validFrom = validFrom,
    validTo = validTo,
    attributes = attributes,
    optionGroups = optionGroups.map { g ->
        ListingFirestoreDto.OptionGroupDto(
            name = g.name,
            selectionType = g.selectionType.name,
            isRequired = g.isRequired,
            options = g.options.map { o ->
                ListingFirestoreDto.OptionDto(o.name, o.priceDelta, o.isAvailable)
            },
        )
    },
    status = status.name,
    rejectionReason = rejectionReason,
    viewsCount = viewsCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ListingFirestoreDto.toDomain(): Listing = Listing(
    id = id,
    ownerId = ownerId,
    businessId = businessId,
    businessType = enumOr(businessType, BusinessType.entries, BusinessType.CAFE_RESTAURANT),
    businessName = businessName,
    categoryKey = categoryKey,
    customCategoryName = customCategoryName,
    title = title,
    description = description,
    images = images,
    priceUnit = enumOr(priceUnit, PriceUnit.entries, PriceUnit.PER_ITEM),
    originalPrice = originalPrice,
    currency = currency,
    discount = ListingDiscount(
        type = enumOr(discount.type, DiscountType.entries, DiscountType.PERCENT),
        value = discount.value,
        conditions = discount.conditions,
        appliesToOptions = discount.appliesToOptions,
    ),
    redemption = ListingRedemption(
        method = enumOr(redemption.method, RedemptionMethod.entries, RedemptionMethod.STUDENT_ID),
        promoCode = redemption.promoCode,
        perUserLimit = redemption.perUserLimit,
        totalLimit = redemption.totalLimit,
        usedCount = redemption.usedCount,
    ),
    branches = branches.map { b ->
        ListingBranch(
            id = b.id,
            lat = b.lat,
            lng = b.lng,
            address = b.address,
            name = b.name,
            landmark = b.landmark,
            regionId = b.regionId,
            districtId = b.districtId,
        )
    },
    validFrom = validFrom,
    validTo = validTo,
    attributes = attributes,
    optionGroups = optionGroups.map { g ->
        OptionGroup(
            name = g.name,
            selectionType = enumOr(g.selectionType, SelectionType.entries, SelectionType.SINGLE),
            isRequired = g.isRequired,
            options = g.options.map { o -> OptionItem(o.name, o.priceDelta, o.isAvailable) },
        )
    },
    status = enumOr(status, ListingStatus.entries, ListingStatus.DRAFT),
    rejectionReason = rejectionReason,
    viewsCount = viewsCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/** Matnни enum'ga xavfsiz aylantiradi — mos kelmasa [default]. */
private fun <T : Enum<T>> enumOr(name: String, entries: List<T>, default: T): T =
    entries.firstOrNull { it.name == name } ?: default
