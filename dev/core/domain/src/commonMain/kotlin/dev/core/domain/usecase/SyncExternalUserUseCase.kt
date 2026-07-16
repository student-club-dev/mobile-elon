package dev.core.domain.usecase

import dev.core.common.Resource
import dev.core.domain.model.ExternalAuthUser
import dev.core.domain.model.User
import dev.core.domain.repository.AuthRepository

/**
 * Google/Telefon (Firebase) orqali kelgan foydalanuvchini domen [User] ga aylantirib,
 * repository orqali saqlaydi.
 */
class SyncExternalUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(external: ExternalAuthUser): Resource<User> =
        repository.syncExternalUser(external)
}
