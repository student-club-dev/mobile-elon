package dev.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClubDto(
    val id: Long,
    val name: String,
    val description: String = "",
    val membersCount: Int = 0,
    val imageUrl: String? = null,
)
