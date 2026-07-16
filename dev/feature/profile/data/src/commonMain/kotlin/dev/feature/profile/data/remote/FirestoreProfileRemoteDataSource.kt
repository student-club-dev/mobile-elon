package dev.feature.profile.data.remote

import dev.core.common.Resource
import dev.feature.profile.data.dto.ProfileFirestoreDto
import dev.feature.profile.data.mapper.toDomain
import dev.feature.profile.data.mapper.toFirestoreDto
import dev.feature.profile.domain.model.UserProfile
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

/**
 * Backendsiz rejim — profil Firestore `users/{uid}` hujjatida (GitLive KMP SDK).
 * Real API tayyor bo'lgach DI [ApiProfileRemoteDataSource] ga o'tadi, bu klass qoladi
 * (offline/demo rejimi uchun).
 */
class FirestoreProfileRemoteDataSource : ProfileRemoteDataSource {

    private val auth get() = Firebase.auth
    private val db get() = Firebase.firestore

    private fun doc(uid: String) = db.collection("users").document(uid)

    override suspend fun fetch(): Resource<UserProfile?> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Sessiya topilmadi")
        return try {
            val snapshot = doc(uid).get()
            val profile = if (snapshot.exists) {
                snapshot.data(ProfileFirestoreDto.serializer()).toDomain()
            } else {
                null
            }
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Profilni yuklab bo'lmadi", e)
        }
    }

    override suspend fun save(profile: UserProfile): Resource<UserProfile> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Sessiya topilmadi — avval kiring")
        return try {
            doc(uid).set(ProfileFirestoreDto.serializer(), profile.toFirestoreDto(), merge = true)
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Profilni saqlab bo'lmadi", e)
        }
    }

    override suspend fun exists(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            doc(uid).get().exists
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Backendsiz rejimda rasm saqlaydigan joy yo'q (Firestore hujjat bazasi, fayl ombori emas).
     * Rasm yuklash backend orqali ishlaydi — `REMOTE_SYNC_ENABLED = true` bo'lganda
     * [ApiProfileRemoteDataSource] `POST /v1/profile/me/avatar` ga yuboradi.
     */
    override suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String> =
        Resource.Error("Rasm yuklash uchun backend kerak (hozir o'chirilgan)")
}
