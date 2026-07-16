package dev.core.domain.usecase

import dev.core.common.Resource
import dev.core.domain.repository.AuthRepository

/** Parolni tiklash havolasini yuborish. */
class SendPasswordResetUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        if (email.isBlank()) return Resource.Error("Email manzilini kiriting")
        return repository.sendPasswordReset(email.trim())
    }
}
