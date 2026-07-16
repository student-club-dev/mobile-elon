package dev.core.common

/**
 * Joriy foydalanuvchining backend uchun autentifikatsiya tokenini (masalan Firebase ID token)
 * beruvchi abstraksiya. `null` — sessiya yo'q (so'rov tokensiz ketadi).
 *
 * Network qatlami (Ktor) shu orqali har so'rovga `Authorization: Bearer <token>` qo'shadi;
 * haqiqiy implementatsiya auth feature'da (Firebase) bog'lanadi. Shu tarzda network moduli
 * Firebase'ga bog'lanmaydi — faqat bu interfeysga tayanadi.
 */
fun interface AuthTokenProvider {
    suspend fun currentToken(): String?
}

/** Sessiya yo'q holat / testlar uchun standart (doim `null`). */
val NoAuthTokenProvider: AuthTokenProvider = AuthTokenProvider { null }
