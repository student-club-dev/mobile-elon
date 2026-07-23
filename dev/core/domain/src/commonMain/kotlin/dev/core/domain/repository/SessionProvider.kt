package dev.core.domain.repository

/**
 * Joriy sessiyaning `uid` manbai — kim kirgan bo'lsa. Ma'lumot repository'lari (profil, biznes,
 * e'lon egaligi) shu orqali `uid` oladi, tokenlarni o'zi o'qimasдан.
 *
 * - `TokenSessionProvider` → access-token (JWT) ning `sub` maydoni (`TokenStore` da saqlanadi).
 * - Local test (`USE_LOCAL_DATA`): `DevSessionProvider` → local `UserEntity` dagi uid.
 *
 * Shu abstraktsiya tufayli data qatlami tokenlarni o'zi o'qimaydi va sinovda almashtirsa bo'ladi.
 */
interface SessionProvider {
    fun currentUid(): String?
}
