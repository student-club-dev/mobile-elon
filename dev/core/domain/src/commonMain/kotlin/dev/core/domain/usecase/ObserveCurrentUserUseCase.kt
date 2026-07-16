package dev.core.domain.usecase

import dev.core.domain.model.User
import dev.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Local keshdagi joriy foydalanuvchini reaktiv kuzatadi.
 * Ilova ochilishida avtomatik kirish (session restore) uchun ishlatiladi.
 */
class ObserveCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<User?> = repository.observeCurrentUser()
}
