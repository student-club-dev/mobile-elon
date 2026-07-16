package dev.feature.profile.domain.usecase

import dev.core.common.Resource
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

/** Local keshdagi joriy profilni reaktiv kuzatadi (Home sarlavhasi, Profil ekrani...). */
class ObserveProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(): Flow<UserProfile?> = repository.observeProfile()
}

/** Profilni saqlaydi (masofaviy manba + local kesh). */
class SaveProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(profile: UserProfile): Resource<Unit> =
        repository.saveProfile(profile)
}

/** Joriy sessiyada profil bor-yo'qligini tekshiradi (login/register yo'nalishini ajratish). */
class HasProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Boolean = repository.hasProfile()
}

/** Profilni masofaviy manbadan qayta yuklaydi (ekran ochilganda fon rejimida). */
class RefreshProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Resource<Unit> = repository.refresh()
}

/**
 * Profil rasmini yuklaydi. Rasm hajmi [MAX_AVATAR_BYTES] dan oshsa server'ga bormaydi —
 * xatoni darrov qaytaradi (spec ham 5 MB chegara qo'yadi).
 */
class UploadAvatarUseCase(private val repository: ProfileRepository) {

    suspend operator fun invoke(bytes: ByteArray, fileName: String): Resource<String> {
        if (bytes.isEmpty()) return Resource.Error("Rasm bo'sh")
        if (bytes.size > MAX_AVATAR_BYTES) {
            return Resource.Error("Rasm juda katta (maks. 5 MB)")
        }
        return repository.uploadAvatar(bytes, fileName)
    }

    companion object {
        const val MAX_AVATAR_BYTES = 5 * 1024 * 1024
    }
}
