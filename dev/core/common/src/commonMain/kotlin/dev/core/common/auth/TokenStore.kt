package dev.core.common.auth

/**
 * Backend bergan sessiya tokenlari (`POST /auth/business/login|register|refresh`).
 *
 * - [accessToken] — qisqa muddatli JWT (15 daqiqa), har so'rovga `Authorization: Bearer` bo'lib ketadi.
 * - [refreshToken] — opaque token; har yangilashda **almashtiriladi** (rotation), shuning uchun
 *   yangi juftlik kelishi bilan eskisi darhol yaroqsiz bo'ladi.
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)

/**
 * Sessiya tokenlarining saqlanish joyi.
 *
 * Tarmoq qatlami (Ktor `Auth` plagini) shu orqali tokenni o'qiydi va `TOKEN_EXPIRED` kelganda
 * yangilaganini qaytarib yozadi; auth feature'i esa kirish/chiqishda to'ldiradi va tozalaydi.
 * Implementatsiya `:dev:core:data` da (SQLDelight `AppSettingEntity`) — shu tarzda network
 * moduli bazaга bog'lanmaydi.
 *
 * Metodlar **sinxron**: manba local baza, chaqiruvlar arzon va `SessionProvider.currentUid()`
 * ham sinxron bo'lishi kerak.
 */
interface TokenStore {

    /** Saqlangan juftlik yoki `null` (sessiya yo'q). */
    fun tokens(): AuthTokens?

    /**
     * Juftlikni saqlaydi. [userId] — JWT `sub` (kim kirgan); `null` uzatilsa avvalgi qiymat
     * o'zgarmaydi (token yangilanishida uid o'sha-o'sha bo'ladi).
     */
    fun save(tokens: AuthTokens, userId: String? = null)

    /** Sessiyani o'chiradi (chiqish yoki refresh token yaroqsiz bo'lganda). */
    fun clear()

    /** Joriy foydalanuvchi id'si (JWT `sub`) yoki `null`. */
    fun userId(): String?
}

/** Sessiyasiz holat / testlar uchun standart (doim bo'sh). */
object EmptyTokenStore : TokenStore {
    override fun tokens(): AuthTokens? = null
    override fun save(tokens: AuthTokens, userId: String?) = Unit
    override fun clear() = Unit
    override fun userId(): String? = null
}
