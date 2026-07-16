package dev.core.domain.model

/** Chegirma taklifining olish turi. */
enum class DiscountTag { STUDENT_ID, PROMO_CODE }

/** Chegirmalar bo'limi (Ovqat, Kiyim-kechak, Kurslar...). */
data class DiscountCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val offerCount: Int,
    val accent: Long,
)

/** Bitta chegirma taklifi (kategoriya ichida). */
data class DiscountOffer(
    val id: String,
    val categoryId: String,
    val merchant: String,        // "Evos", "Chopar"
    val title: String,           // "barcha pitsalarga"
    val discountPercent: Int,    // 30 → −30%
    val tag: DiscountTag,
    val promoCode: String? = null,
    val location: String? = null, // "5 filial", "yetkazib berish"
    val expiry: String? = null,   // "bugun tugaydi"
    val emoji: String,
    val bannerAccent: Long,
    val featured: Boolean = false, // Home feed'da ko'rsatiladimi
)
