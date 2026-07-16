package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.core.common.errorOf
import dev.core.common.error.AppException
import dev.core.common.error.toAppException
import dev.core.common.network.NetworkConnectivity
import dev.feature.discounts.data.dto.BusinessFirestoreDto
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.repository.BusinessRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Biznes repository'sining Firestore implementatsiyasi — `businesses/{id}` (avtomatik id).
 * Bir egada bir nechta biznes; `.snapshots` real-time, filtr klient tomonда (ownerId).
 */
class FirestoreBusinessRepository(
    private val connectivity: NetworkConnectivity,
) : BusinessRepository {

    private val db get() = Firebase.firestore
    private val uid: String? get() = Firebase.auth.currentUser?.uid
    private fun col() = db.collection(COLLECTION)
    private fun doc(id: String) = col().document(id)

    override fun observeMine(): Flow<List<Business>> {
        val ownerId = uid ?: return flowOf(emptyList())
        return col().snapshots.map { snap ->
            snap.documents
                .mapNotNull { runCatching { it.data(BusinessFirestoreDto.serializer()) }.getOrNull() }
                .filter { it.ownerId == ownerId }
                .map { it.toDomain() }
                .sortedByDescending { it.createdAt }
        }
    }

    override suspend fun byId(id: String): Business? = try {
        val snap = doc(id).get()
        if (snap.exists) snap.data(BusinessFirestoreDto.serializer()).toDomain() else null
    } catch (e: Exception) {
        null
    }

    override suspend fun save(business: Business): Resource<Business> {
        val ownerId = uid ?: return errorOf(AppException.Unauthorized())
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        // id bo'sh bo'lsa — yangi hujjat (avtomatik id), aks holda mavjudini yangilaymiz.
        val ref = if (business.id.isBlank()) col().document else doc(business.id)
        val toSave = business.copy(id = ref.id, ownerId = ownerId)
        return try {
            ref.set(BusinessFirestoreDto.serializer(), toSave.toDto())
            Resource.Success(toSave)
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    override suspend fun delete(id: String): Resource<Unit> {
        if (uid == null) return errorOf(AppException.Unauthorized())
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            doc(id).delete()
            Resource.Success(Unit)
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    private companion object {
        const val COLLECTION = "businesses"
    }
}

private fun Business.toDto() = BusinessFirestoreDto(
    id = id,
    ownerId = ownerId,
    name = name,
    phone = phone,
    businessType = businessType?.name,
    branches = branches.map { b ->
        BusinessFirestoreDto.BranchDto(b.id, b.lat, b.lng, b.address, b.name, b.landmark, b.regionId, b.districtId)
    },
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun BusinessFirestoreDto.toDomain() = Business(
    id = id,
    ownerId = ownerId,
    name = name,
    phone = phone,
    businessType = businessType?.let { name -> BusinessType.entries.firstOrNull { it.name == name } },
    branches = branches.map { b ->
        ListingBranch(b.id, b.lat, b.lng, b.address, b.name, b.landmark, b.regionId, b.districtId)
    },
    createdAt = createdAt,
    updatedAt = updatedAt,
)
