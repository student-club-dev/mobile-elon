package dev.core.domain.model

/**
 * Universitet — profil to'ldirish va student bo'limlari uchun.
 * Backend kelганда `id` server ID'siga mos keladi.
 */
data class University(
    val id: String,
    val name: String,
    val city: String,
    val monogram: String,
    val faculty: String? = null,
    val accent: Long, // ARGB, masalan 0xFF6C47FF
)
