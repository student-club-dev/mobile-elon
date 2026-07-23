package dev.feature.auth.data

import dev.core.common.auth.TokenStore
import dev.core.domain.repository.SessionProvider

/**
 * Joriy `uid` manbai — access-token bilan birga saqlangan JWT `sub`.
 *
 * Profil/biznes/e'lon repository'lari egalikni shu id bo'yicha aniqlaydi; sessiya bo'lmasa `null`.
 */
class TokenSessionProvider(private val tokenStore: TokenStore) : SessionProvider {
    override fun currentUid(): String? = tokenStore.userId()
}
