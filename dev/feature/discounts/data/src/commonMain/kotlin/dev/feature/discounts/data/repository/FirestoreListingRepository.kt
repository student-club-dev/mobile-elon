package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.core.common.errorOf
import dev.core.common.error.AppException
import dev.core.common.error.toAppException
import dev.core.common.network.NetworkConnectivity
import dev.feature.discounts.data.dto.ListingFirestoreDto
import dev.feature.discounts.data.mapper.toDomain
import dev.feature.discounts.data.mapper.toFirestoreDto
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.repository.ListingRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * E'lon repository'sining Firestore (GitLive) implementatsiyasi — **backend o'rniga Firebase**.
 *
 * Struktura: `listings/{listingId}` hujjatlari (`ListingFirestoreDto`). `.snapshots` — real-time
 * oqim, shu sabab biznes egasi yaratgan e'lon **boshqa qurilmadagi talabaga darrov** ko'rinadi.
 * Firestore o'zi offline cache beradi → tarmoqsiz ham ishlaydi (offline-first).
 *
 * Filtrlash (status/muddat/ega) `conversations` real-time chat naqshidek **klient tomonida**
 * qilinadi — shunda Firestore composite index'lar kerak bo'lmaydi. Ma'lumot ko'payganda
 * server tomon `where` + index'ga o'tiladi.
 *
 * Rasm: hozircha `data:` URI (Spark reja — Storage/Blaze shart emas). Ishlab chiqarishда
 * Firebase Storage'ga o'tiladi.
 */
class FirestoreListingRepository(
    private val connectivity: NetworkConnectivity,
) : ListingRepository {

    private val db get() = Firebase.firestore
    private fun col() = db.collection(COLLECTION)
    private fun doc(id: String) = col().document(id)

    override fun observeMyListings(ownerId: String): Flow<List<Listing>> =
        col().snapshots.map { snap ->
            snap.documents
                .mapNotNull { runCatching { it.data(ListingFirestoreDto.serializer()) }.getOrNull() }
                .filter { it.ownerId == ownerId }
                .map { it.toDomain() }
                .sortedByDescending { it.updatedAt }
        }

    override fun observeActive(): Flow<List<Listing>> =
        col().snapshots.map { snap ->
            val now = nowMs()
            snap.documents
                .mapNotNull { runCatching { it.data(ListingFirestoreDto.serializer()) }.getOrNull() }
                .filter { it.status == ListingStatus.ACTIVE.name && it.validTo >= now }
                .map { it.toDomain() }
                .sortedByDescending { it.updatedAt }
        }

    override suspend fun byId(id: String): Listing? = try {
        val snapshot = doc(id).get()
        if (snapshot.exists) snapshot.data(ListingFirestoreDto.serializer()).toDomain() else null
    } catch (e: Exception) {
        null
    }

    override suspend fun save(listing: Listing): Resource<Listing> {
        val draft = listing.copy(updatedAt = nowMs())
        return write(draft)
    }

    override suspend fun submit(listing: Listing): Resource<Listing> {
        // Moderator yo'q — e'lon darrov ACTIVE bo'ladi (backendsiz naqsh bilan bir xil).
        val published = listing.copy(status = ListingStatus.ACTIVE, updatedAt = nowMs())
        return write(published)
    }

    override suspend fun updateStatus(id: String, status: ListingStatus): Resource<Unit> {
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            doc(id).set(StatusPatch.serializer(), StatusPatch(status.name, nowMs()), merge = true)
            Resource.Success(Unit)
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    override suspend fun delete(id: String): Resource<Unit> {
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            doc(id).delete()
            Resource.Success(Unit)
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun uploadImage(bytes: ByteArray, fileName: String): Resource<String> = try {
        // Spark reja: rasm `data:` URI sifatida e'lon bilan Firestore hujjatida saqlanadi.
        val mime = if (fileName.endsWith(".png", ignoreCase = true)) "image/png" else "image/jpeg"
        Resource.Success("data:$mime;base64,${Base64.encode(bytes)}")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Rasmni o'qib bo'lmadi", e)
    }

    private suspend fun write(listing: Listing): Resource<Listing> {
        // Internet yo'q bo'lsa — so'rov qilmasdan aniq xato (UI retry ko'rsatadi).
        if (!connectivity.isOnline()) return errorOf(AppException.NoInternet())
        return try {
            doc(listing.id).set(ListingFirestoreDto.serializer(), listing.toFirestoreDto())
            Resource.Success(listing)
        } catch (e: Exception) {
            errorOf(e.toAppException(connectivity.isOnline()))
        }
    }

    private fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()

    /** Faqat status + updatedAt ni yangilash uchun qisman DTO (merge). */
    @Serializable
    private data class StatusPatch(val status: String, val updatedAt: Long)

    private companion object {
        const val COLLECTION = "listings"
    }
}
