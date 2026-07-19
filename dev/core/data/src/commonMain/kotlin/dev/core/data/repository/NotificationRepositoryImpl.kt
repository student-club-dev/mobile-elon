package dev.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import dev.core.common.AppDispatchers
import dev.core.database.sql.NotificationEntity
import dev.core.database.sql.ElonUzDatabase
import dev.core.domain.model.AppNotification
import dev.core.domain.model.NotificationType
import dev.core.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** [NotificationRepository] — SQLDelight `NotificationEntity` ustidagi implementatsiya. */
class NotificationRepositoryImpl(
    private val db: ElonUzDatabase,
    private val dispatchers: AppDispatchers,
) : NotificationRepository {

    private val q get() = db.notificationQueries

    override fun observeAll(): Flow<List<AppNotification>> =
        q.selectAll().asFlow().mapToList(dispatchers.io).map { rows -> rows.map { it.toDomain() } }

    override fun observeUnreadCount(): Flow<Int> =
        q.countUnread().asFlow().mapToOne(dispatchers.io).map { it.toInt() }

    override suspend fun markRead(id: String) = withContext(dispatchers.io) {
        q.markRead(id)
    }

    override suspend fun markAllRead() = withContext(dispatchers.io) {
        q.markAllRead()
    }

    private fun NotificationEntity.toDomain(): AppNotification = AppNotification(
        id = id,
        title = title,
        body = body,
        type = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.SYSTEM),
        timeLabel = timeLabel,
        read = read != 0L,
    )
}
