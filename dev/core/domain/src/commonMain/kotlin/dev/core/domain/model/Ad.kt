package dev.core.domain.model

/** E'lon turi (Elon berish oqimi). */
enum class AdType { JOB, RENTAL, SALE, SERVICE, OTHER }

/** Foydalanuvchi joylagan e'lon. */
data class Ad(
    val id: String,
    val type: AdType,
    val title: String,
    val category: String,
    val price: String,           // "3–5 mln", "1.2 mln/oy"
    val description: String,
    val images: List<String> = emptyList(),
    val ownerId: String,
    val createdAgo: String,      // "2 soat oldin"
)
