package dev.feature.discounts.data.dto

import kotlinx.serialization.Serializable

/**
 * Firestore `listings/{listingId}` hujjatining serializatsiya modeli.
 *
 * GitLive Firestore nested `@Serializable` obyektlarni bevosita saqlaydi/o'qiydi, shuning uchun
 * domen [dev.feature.discounts.domain.model.Listing] modelining butun tuzilishi shu yerda
 * DTO'lar ko'rinishida takrorlanadi. Enum'lar **matn** sifatida saqlanadi (mapper `.name` ni
 * yozadi, `valueOf` bilan o'qiydi) — shunda yangi qiymat qo'shilsa sxema o'zgarmaydi.
 *
 * MUHIM: barcha maydonlarда default qiymat bo'lishi shart — Firestore hujjatida bir maydon
 * yo'q bo'lsa ham deserializatsiya yiqilmasin.
 */
@Serializable
data class ListingFirestoreDto(
    val id: String = "",
    val ownerId: String = "",
    val businessId: String? = null,
    val businessType: String = "",
    val businessName: String = "",
    val categoryKey: String = "",
    val customCategoryName: String? = null,

    val title: String = "",
    val description: String? = null,
    val images: List<String> = emptyList(),

    val priceUnit: String = "",
    val originalPrice: Long = 0,
    val currency: String = "UZS",
    /** Denormalizatsiya — server/klient bir xil formula, sort va ko'rsatish uchun. */
    val finalPrice: Long = 0,
    /** Filtrlash uchun denormalizatsiya (chegirma e'lonimi). */
    val isDiscount: Boolean = true,

    val discount: DiscountDto = DiscountDto(),
    val redemption: RedemptionDto = RedemptionDto(),
    val branches: List<BranchDto> = emptyList(),

    val validFrom: Long = 0,
    val validTo: Long = 0,

    val attributes: Map<String, String> = emptyMap(),
    val optionGroups: List<OptionGroupDto> = emptyList(),

    val status: String = "DRAFT",
    val rejectionReason: String? = null,
    val viewsCount: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
    @Serializable
    data class DiscountDto(
        val type: String = "PERCENT",
        val value: Long = 0,
        val conditions: String? = null,
        val appliesToOptions: Boolean = false,
    )

    @Serializable
    data class RedemptionDto(
        val method: String = "STUDENT_ID",
        val promoCode: String? = null,
        val perUserLimit: Int? = 1,
        val totalLimit: Int? = null,
        val usedCount: Int = 0,
    )

    @Serializable
    data class BranchDto(
        val id: String = "",
        val lat: Double = 0.0,
        val lng: Double = 0.0,
        val address: String = "",
        val name: String? = null,
        val landmark: String? = null,
        val regionId: String? = null,
        val districtId: String? = null,
    )

    @Serializable
    data class OptionGroupDto(
        val name: String = "",
        val selectionType: String = "SINGLE",
        val isRequired: Boolean = false,
        val options: List<OptionDto> = emptyList(),
    )

    @Serializable
    data class OptionDto(
        val name: String = "",
        val priceDelta: Long = 0,
        val isAvailable: Boolean = true,
    )
}
