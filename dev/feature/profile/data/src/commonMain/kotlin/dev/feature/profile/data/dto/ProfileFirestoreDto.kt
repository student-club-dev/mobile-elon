package dev.feature.profile.data.dto

import kotlinx.serialization.Serializable

/**
 * Firestore `users/{uid}` hujjatining serializatsiya modeli (backendsiz rejim).
 *
 * REST rejimida buning o'rniga OpenAPI'dan generatsiya qilingan
 * `dev.core.network.generated.model.UserProfileDto` ishlatiladi — ikkalasi ham
 * bir xil domen modeliga (`UserProfile`) o'giriladi.
 */
@Serializable
data class ProfileFirestoreDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,
    val role: String? = null,
    val universityId: String? = null,
    val universityEmail: String? = null,
    val birthYear: Int? = null,
    val courseYear: String? = null,
    val avatarUrl: String? = null,
    val businessName: String? = null,
    val businessType: String? = null,
    val email: String? = null,
)
