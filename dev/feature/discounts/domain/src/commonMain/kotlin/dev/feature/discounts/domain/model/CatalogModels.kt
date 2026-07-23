package dev.feature.discounts.domain.model

/**
 * Katalog modellari — **backenddan keladigan** biznes turlari va kategoriyalar.
 *
 * Nega alohida model: [ListingCatalog] — klientdagi zaxira nusxa. Backend ishlaganda katalog
 * serverdan keladi, shunda adminka yangi tur/kategoriya/variant qo'shsa ilova **yangilanmasdan**
 * o'zgarishni ko'radi.
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

        /** Klientdagi zaxira katalogdan — backend javob bermaganda ishlatiladi. */
        fun from(type: BusinessType): BusinessTypeInfo {
            val fallback = ListingCatalog.info(type)
            return BusinessTypeInfo(
                type = type,
                // Zaxirada ham yo'q tur (server yangi qo'shgan, keyin oflayn qolgan) —
                // kalitning o'zi nomdan ko'ra yaxshiroq: bo'sh joy qolmaydi.
                nameUz = fallback?.nameUz ?: type.key,
                emoji = fallback?.emoji.orEmpty(),
                accentColor = fallback?.accentColor ?: DEFAULT_ACCENT,
                defaultPriceUnit = ListingCatalog.defaultPriceUnit(type),
                priceUnits = ListingCatalog.priceUnits(type),
            )
        }

        /** Neytral urg'u rangi — tur zaxirada bo'lmaganda. */
        private const val DEFAULT_ACCENT = 0xFF7C5CFF
    }
}

/**
 * Kategoriya + uning maydonlari (`GET /business/types/{type}/categories`).
 *
 * [fields] — ichma-ich tuzilma: maydon → `options` (masalan PlayStation → Model → PS5/PS4/PS3).
 * Shuning uchun kategoriya tanlanganda **yangi so'rov kerak emas**.
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
        /** Klientdagi zaxira katalogdan — backend javob bermaganda ishlatiladi. */
        fun from(type: BusinessType, category: ListingCategory, index: Int): CategoryInfo = CategoryInfo(
            key = category.key,
            nameUz = category.label,
            sortOrder = index,
            fields = ListingCatalog.categoryAttributes(type, category.key),
            requiresCustomName = category.key == ListingCatalog.OTHER_KEY,
        )
    }
}
