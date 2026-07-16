package dev.core.domain.usecase

import dev.core.common.Resource
import dev.core.domain.repository.AuthRepository

/** Email ro'yxatdan o'tish 1-qadam: emailga kod yuboradi. */
class RequestEmailSignupUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        if (email.isBlank() || !email.contains("@")) return Resource.Error("To'g'ri email kiriting")
        return repository.requestEmailSignup(email.trim())
    }
}

/** 2-qadam: kodni tekshirib akkaunt yaratadi. */
class ConfirmEmailSignupUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, code: String, password: String): Resource<Unit> {
        if (code.length != 6) return Resource.Error("6 xonali kodni kiriting")
        return repository.confirmEmailSignup(email.trim(), code, password)
    }
}
