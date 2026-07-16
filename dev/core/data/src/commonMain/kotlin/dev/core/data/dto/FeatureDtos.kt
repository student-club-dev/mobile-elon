package dev.core.data.dto

import kotlinx.serialization.Serializable

/**
 * Qolgan domenlar uchun API DTO'lari (B4 offline-first shabloni — Discounts'dan tarqatilgan).
 * Real API spek kelганда maydon nomlarini serverникiga moslang (@SerialName), oqim o'zgarmaydi.
 */

// --- Ishlar ---
@Serializable
data class JobDto(
    val id: String,
    val title: String,
    val company: String,
    val companyMonogram: String = "",
    val location: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    val salary: String = "",
    val remote: Boolean = false,
    val partTime: Boolean = false,
    val postedAgo: String = "",
    val field: String = "",
    val bookmarked: Boolean = false,
)

// --- Studentlar ---
@Serializable
data class StudentDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val initial: String = "",
    val universityId: String = "",
    val universityMonogram: String = "",
    val course: Int = 1,
    val faculty: String = "",
    val friendStatus: String = "NONE",
    val interests: List<String> = emptyList(),
    val friendsCount: Int = 0,
    val adsCount: Int = 0,
    val rating: Double = 0.0,
)

// --- E'lonlar ---
@Serializable
data class AdDto(
    val id: String,
    val type: String = "OTHER",
    val title: String,
    val category: String = "",
    val price: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val ownerId: String = "",
    val createdAgo: String = "",
)

// --- Universitetlar ---
@Serializable
data class UniversityDto(
    val id: String,
    val name: String,
    val city: String = "",
    val monogram: String = "",
    val faculty: String? = null,
    val accent: Long = 0xFF6C47FF,
)

// --- Chat (suhbatlar ro'yxati; xabarlar real-time/alohida) ---
@Serializable
data class ConversationDto(
    val id: String,
    val peerName: String,
    val peerInitial: String = "",
    val type: String = "PEER",
    val online: Boolean = false,
    val lastMessage: String = "",
    val lastTime: String = "",
    val unreadCount: Int = 0,
)
