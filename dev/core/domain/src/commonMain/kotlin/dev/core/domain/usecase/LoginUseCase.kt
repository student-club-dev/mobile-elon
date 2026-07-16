package dev.core.domain.usecase

import dev.core.common.Resource
import dev.core.domain.model.User
import dev.core.domain.repository.AuthRepository

/** Bitta biznes-amal. Feature ViewModel'i shu use-case'ni chaqiradi. */
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank() || password.isBlank()) {
            return Resource.Error("Email va parol bo'sh bo'lmasligi kerak")
        }
        return repository.login(email.trim(), password)
    }
}
