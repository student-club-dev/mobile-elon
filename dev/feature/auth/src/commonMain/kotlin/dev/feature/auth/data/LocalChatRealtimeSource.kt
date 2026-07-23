package dev.feature.auth.data

import dev.core.domain.model.Conversation
import dev.core.domain.model.Message
import dev.core.domain.repository.ChatRealtimeSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Chat real-time manbasining **o'chirilgan** varianti.
 *
 * Ilgari bu yerda Firestore listener'lari turardi; Firebase olib tashlangach va yangi backend
 * spec'ida (`elon-uz.json`) chat endpoint'lari bo'lmagani uchun chat butunlay local bazadan
 * ishlaydi. `enabled = false` bo'lgani uchun `ChatRepository` bu manbaga umuman murojaat
 * qilmaydi — metodlar faqat interfeys to'liqligi uchun.
 *
 * Backend chat qo'shganda: shu klass o'rniga API implementatsiyasi ulanadi, qolgan kod tegilmaydi.
 */
class LocalChatRealtimeSource : ChatRealtimeSource {

    override val enabled: Boolean = false

    override fun conversations(): Flow<List<Conversation>> = flowOf(emptyList())
    override fun archivedConversations(): Flow<List<Conversation>> = flowOf(emptyList())
    override fun messages(conversationId: String): Flow<List<Message>> = flowOf(emptyList())

    override suspend fun send(conversationId: String, text: String, time: String, createdAt: Long) = Unit
    override suspend fun markRead(conversationId: String) = Unit
    override suspend fun deleteMessage(conversationId: String, messageId: String) = Unit
    override suspend fun clearMessages(conversationId: String) = Unit
    override suspend fun deleteConversation(conversationId: String) = Unit
    override suspend fun setArchived(conversationId: String, archived: Boolean) = Unit
}
