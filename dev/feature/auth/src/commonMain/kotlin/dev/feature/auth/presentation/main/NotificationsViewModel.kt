package dev.feature.auth.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.domain.model.AppNotification
import dev.core.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val items: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
)

class NotificationsViewModel(
    private val repository: NotificationRepository,
) : ViewModel() {

    val state: StateFlow<NotificationsUiState> =
        repository.observeAll()
            .map { list -> NotificationsUiState(items = list, unreadCount = list.count { !it.read }) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationsUiState())

    fun markRead(id: String) {
        viewModelScope.launch { repository.markRead(id) }
    }

    fun markAllRead() {
        viewModelScope.launch { repository.markAllRead() }
    }
}
