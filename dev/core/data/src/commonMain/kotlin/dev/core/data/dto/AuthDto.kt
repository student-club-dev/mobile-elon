package dev.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Long = 0,
    val fullName: String = "",
)
