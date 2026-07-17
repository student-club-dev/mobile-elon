package dev.feature.discounts.domain.model

/**
 * Katalog modellari — **backenddan keladigan** biznes turlari va kategoriyalar.
 *
 * Nega alohida model: [ListingCatalog] — klientдаgi qattiq kodlangan nusxa (zaxira).
 * Backend ishlaganда katalog serverdan keladi, shunda adminka yangi tur/kategoriya/variant
 * qo'shsa ilova **yangilanmasdan** o'zgarishni ko'radi.
 */

/** Biznes turi + uni chizish uchun kerakli hamma narsa (`GET /business/types`). */
data class BusinessTypeInfo(
    val type: BusinessType,
    val nameUz: String,
    val emoji: String,
    val accentColor: Long,
    val defaultPriceUnit: PriceUnit,
    val priceUnits: List<PriceUnit>,
) {
    companion object {
        /** Klientдаgi katalogdan — backend javob bermaganда ishlatiladi. */
        fun from(type: BusinessType): BusinessTypeInfo = BusinessTypeInfo(
            type = type,
            nameUz = type.label,
            emoji = type.emoji,
            accentColor = type.accent,
            defaultPriceUnit = type.defaultPriceUnit,
            priceUnits = ListingCatalog.priceUnits(type),
        )
    }
}

/**
 * Kategoriya + uning maydonlari (`GET /business/types/{type}/categories`).
 *
 * [fields] — ichma-ich tuzilma: maydon → `options` (masalan PlayStation → Model → PS5/PS4/PS3).
 * Shuning uchun kategoriya tanlanganда **yangi so'rov kerak emas**.
 */
data class CategoryInfo(
    val key: String,
    val nameUz: String,
    val sortOrder: Int = 0,
    val fields: List<AttributeSpec> = emptyList(),
    /** `OTHER` uchun `true` — `customCategoryName` majburiy bo'ladi. */
    val requiresCustomName: Boolean = false,
) {
    companion object {
        /** Klientдаgi katalogdan — backend javob bermaganда ishlatiladi. */
        fun from(type: BusinessType, category: ListingCategory, index: Int): CategoryInfo = CategoryInfo(
            key = category.key,
            nameUz = category.label,
            sortOrder = index,
            fields = ListingCatalog.categoryAttributes(type, category.key),
            requiresCustomName = category.key == ListingCatalog.OTHER_KEY,
        )
    }
}
