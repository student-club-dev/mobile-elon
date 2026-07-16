package dev.core.domain.model

/** Domain modeli — tashqi (DTO/Entity) qatlamlardan mustaqil. */
data class Club(
    val id: Long,
    val name: String,
    val description: String,
    val membersCount: Int,
    val imageUrl: String? = null,
    val joined: Boolean = false,
)
