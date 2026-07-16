package dev.feature.discounts.data.dto

import kotlinx.serialization.Serializable

/** Firestore `businesses/{ownerId}` hujjati. Barcha maydonlarда default — deserializatsiya yiqilmasin. */
@Serializable
data class BusinessFirestoreDto(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val phone: String = "",
    val businessType: String? = null,
    val branches: List<BranchDto> = emptyList(),
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
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
}
