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
 * Biznes turining dinamik forma sxemasi (`GET /business/types/{type}/attributes-schema`).
 *
 * [common] — turning **barcha** e'lonlariga tegishli maydonlar (masalan har qanday
 * PlayStation e'lonida "Joylar soni"), [byCategory] — kategoriya tanlangandan keyin
 * qo'shiladigan maydonlar (`categoryKey → maydonlar`).
 *
 * Forma ikkalasini birlashtiradi: `common + byCategory[tanlangan]` ([fieldsFor]).
 *
 * ⚠️ Turlar va ularning maydonlari **serverda** o'sib boradi — 27 ta tur bor va `attributes`
 * bazadagi `attribute_specs` jadvalidan keladi. Shuning uchun bu ro'yxat hech qachon ilovada
 * qotirilmaydi (`DISCOUNTS_BUSINESS_API_RESPONSE.md` §5.1).
 */
data class TypeAttributes(
    val businessType: BusinessType,
    val common: List<AttributeSpec> = emptyList(),
    val byCategory: Map<String, List<AttributeSpec>> = emptyMap(),
) {
    /**
     * Tanlangan kategoriya uchun to'liq maydonlar ro'yxati.
     *
     * Umumiy maydonlar **oldinda** turadi: ular butun turga tegishli va foydalanuvchi ularni
     * kategoriya tafsilotlaridan oldin to'ldirishi tabiiyroq. Kalit bo'yicha takrorlanish
     * bo'lsa kategoriyaniki emas, umumiysi qoladi — server ikkalasida bir xil kalit bersa,
     * bu bitta maydon, ikkita emas.
     */
    fun fieldsFor(categoryKey: String?): List<AttributeSpec> {
        val categoryFields = byCategory[categoryKey].orEmpty()
        return common + categoryFields.filterNot { field -> common.any { it.key == field.key } }
    }

    companion object {
        /** Sxema yuklanmaganda ishlatiladigan bo'sh qiymat — forma kategoriya maydonlari bilan ishlaydi. */
        fun empty(type: BusinessType) = TypeAttributes(type)
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
