package dev.core.domain.repository

import dev.core.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

/** Bildirishnomalar — local DB (`NotificationEntity`) ustidagi reaktiv repository. */
interface NotificationRepository {
    fun observeAll(): Flow<List<AppNotification>>
    fun observeUnreadCount(): Flow<Int>
    suspend fun markRead(id: String)
    suspend fun markAllRead()
}
