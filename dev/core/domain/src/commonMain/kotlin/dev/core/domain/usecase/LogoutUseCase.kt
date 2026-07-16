package dev.core.domain.usecase

import dev.core.domain.repository.AuthRepository

/** Tizimdan chiqish — Firebase sessiyasi va local keshni tozalaydi. */
class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}
