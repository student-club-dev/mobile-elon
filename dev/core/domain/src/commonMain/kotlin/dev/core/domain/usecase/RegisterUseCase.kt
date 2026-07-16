package dev.core.domain.usecase

import dev.core.common.Resource
import dev.core.domain.model.User
import dev.core.domain.repository.AuthRepository

/** Email + parol bilan ro'yxatdan o'tish. */
class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank() || password.isBlank()) {
            return Resource.Error("Email va parol bo'sh bo'lmasligi kerak")
        }
        if (password.length < 6) {
            return Resource.Error("Parol kamida 6 belgidan iborat bo'lishi kerak")
        }
        return repository.register(email.trim(), password)
    }
}
