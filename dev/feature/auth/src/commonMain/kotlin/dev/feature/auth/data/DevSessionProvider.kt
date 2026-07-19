package dev.feature.auth.data

import dev.core.database.sql.ElonUzDatabase
import dev.core.domain.repository.SessionProvider

/**
 * Local test sessiya manbai (`USE_LOCAL_DATA`) — Firebase'siz. uid local `UserEntity`dan olinadi
 * (dev-login uni yozadi). Kirilmagan bo'lsa `null`.
 */
class DevSessionProvider(private val db: ElonUzDatabase) : SessionProvider {
    override fun currentUid(): String? =
        db.userQueries.selectCurrent().executeAsOneOrNull()?.uid
}
