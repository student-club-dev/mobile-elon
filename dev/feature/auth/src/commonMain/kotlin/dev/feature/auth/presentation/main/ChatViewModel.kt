package dev.feature.auth.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.domain.model.Conversation
import dev.core.domain.model.ConversationType
import dev.core.domain.model.Message
import dev.core.domain.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Chat (1x ro'yxat + 1y suhbat) holati. */
data class ChatUiState(
    /** Ro'yxatda ko'rinadigan faol suhbatlar — qo'llab-quvvatlashsiz. */
    val conversations: List<Conversation> = emptyList(),
    val archivedConversations: List<Conversation> = emptyList(),
    /** Qo'llab-quvvatlash suhbati — ro'yxatda emas, alohida ekran orqali ochiladi. */
    val support: Conversation? = null,
    val selected: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val draft: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { chatRepository.refresh() }
    }

    private val selectedId = MutableStateFlow<String?>(null)
    private val draft = MutableStateFlow("")

    private val messagesFlow = selectedId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else chatRepository.observeMessages(id)
    }

    val state: StateFlow<ChatUiState> = combine(
        chatRepository.observeConversations(),
        chatRepository.observeArchivedConversations(),
        selectedId,
        messagesFlow,
        draft,
    ) { conversations, archived, id, messages, d ->
        // Qo'llab-quvvatlash suhbati ro'yxatdan CHIQARIB tashlanadi (handoff, 5-ekran), lekin
        // tanlash uchun to'liq ro'yxat kerak — shuning uchun avval `all` yig'iladi.
        val all = conversations + archived
        ChatUiState(
            conversations = conversations.filterNot { it.type == ConversationType.SUPPORT },
            archivedConversations = archived.filterNot { it.type == ConversationType.SUPPORT },
            support = all.firstOrNull { it.type == ConversationType.SUPPORT },
            selected = all.firstOrNull { it.id == id },
            messages = messages,
            draft = d,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState())

    fun open(conversation: Conversation) {
        selectedId.value = conversation.id
        viewModelScope.launch { chatRepository.markRead(conversation.id) }
    }

    fun close() {
        selectedId.value = null
    }

    /** Suhbatdagi xabarni o'chirish. */
    fun deleteMessage(messageId: String) {
        val id = selectedId.value ?: return
        viewModelScope.launch { chatRepository.deleteMessage(id, messageId) }
    }

    /** Suhbatni tozalash — barcha xabarlarni o'chirish. */
    fun clearMessages() {
        val id = selectedId.value ?: return
        viewModelScope.launch { chatRepository.clearMessages(id) }
    }

    /** Suhbatni butunlay o'chirish (ro'yxatdan). */
    fun deleteConversation(conversationId: String) {
        if (selectedId.value == conversationId) selectedId.value = null
        viewModelScope.launch { chatRepository.deleteConversation(conversationId) }
    }

    /** Suhbatni arxivlash / arxivdan chiqarish. */
    fun setArchived(conversationId: String, archived: Boolean) {
        viewModelScope.launch { chatRepository.setArchived(conversationId, archived) }
    }

    fun onDraft(v: String) {
        draft.value = v
    }

    fun send() {
        val id = selectedId.value ?: return
        val text = draft.value.trim()
        if (text.isEmpty()) return
        // Haqiqiy vaqt — qurilmalararo to'g'ri tartib (Firestore real-time) va to'g'ri HH:mm yorlig'i.
        val now = Clock.System.now()
        val createdAt = now.toEpochMilliseconds()
        val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val time = "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
        draft.value = ""
        viewModelScope.launch { chatRepository.send(id, text, time = time, createdAt = createdAt) }
    }
}
