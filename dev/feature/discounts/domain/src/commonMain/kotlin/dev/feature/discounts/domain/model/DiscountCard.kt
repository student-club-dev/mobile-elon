package dev.feature.discounts.domain.model

/**
 * Talabaga ko'rinadigan chegirma **kartasi** — `GET /discounts` qaytaradigan yengil model.
 *
 * [Listing] dan farqi: bu yerда e'lonning to'liq holati yo'q (atributlar, variantlar,
 * redemption shartlari). Ro'yxat uchun shuncha kifoya — kartaga bosilganда to'liq e'lon
 * alohida yuklanadi.
 *
 * [nearest] — foydalanuvchiga **eng yaqin filial** va unga masofa. Backend uni koordinata
 * berilganда o'zi hisoblaydi; zaxira rejimда klient hisoblaydi ([Listing.nearestBranch]).
 */
data class DiscountCard(
    val id: String,
    val title: String,
    val businessName: String,
    val businessType: BusinessType,
    val originalPrice: Long,
    val discount: ListingDiscount,
    val imageUrl: String? = null,
    val nearest: NearestBranch? = null,
) {
    /** Chegirmadan keyingi narx — [Listing.finalPrice] bilan bir xil formula. */
    val finalPrice: Long get() = discount.finalPrice(originalPrice)
}

/** `GET /discounts` so'rovining parametrlari (spec §6 — talaba qidiruvi). */
data class DiscountQuery(
    val lat: Double? = null,
    val lng: Double? = null,
    val radiusMeters: Int = DEFAULT_RADIUS_METERS,
    val businessType: BusinessType? = null,
    val categoryKey: String? = null,
    val query: String? = null,
    val sort: DiscountSort = DiscountSort.DISTANCE,
    val page: Int = 0,
    val size: Int = DEFAULT_PAGE_SIZE,
) {
    companion object {
        const val DEFAULT_RADIUS_METERS = 5_000
        const val DEFAULT_PAGE_SIZE = 20
    }
}

/** Saralash tartibi — spec'даgi `DiscountSortDto` bilan bir xil qiymatlar. */
enum class DiscountSort { DISTANCE, DISCOUNT_DESC, NEWEST, POPULAR }
