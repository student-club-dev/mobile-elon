package dev.core.data.dto

import kotlinx.serialization.Serializable

/**
 * Chegirmalar API javob DTO'lari (B4 offline-first shabloni).
 *
 * Real API spek (`student-clubs.json`) kelганда: maydon nomlarini serverникiga moslang
 * (kerak bo'lsa `@SerialName("...")`), qolgan oqim (RemoteDataSource → refresh → DB) o'zgarmaydi.
 */
@Serializable
data class DiscountCategoryDto(
    val id: String,
    val name: String,
    val emoji: String = "🏷️",
    val offerCount: Int = 0,
    val accent: Long = 0xFF6C47FF,
)

@Serializable
data class DiscountOfferDto(
    val id: String,
    val categoryId: String,
    val merchant: String,
    val title: String,
    val discountPercent: Int,
    val tag: String = "STUDENT_ID",   // "STUDENT_ID" | "PROMO_CODE"
    val promoCode: String? = null,
    val location: String? = null,
    val expiry: String? = null,
    val emoji: String = "🎁",
    val bannerAccent: Long = 0xFF6C47FF,
    val featured: Boolean = false,
)

/** Bitta so'rovda ikkalasini olish uchun konteyner (yoki alohida endpoint'lar). */
@Serializable
data class DiscountsResponseDto(
    val categories: List<DiscountCategoryDto> = emptyList(),
    val offers: List<DiscountOfferDto> = emptyList(),
)
