package dev.core.domain.repository

import dev.core.domain.model.Conversation
import dev.core.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Chat uchun real-time manba (B7). `enabled=false` bo'lsa — ChatRepository local DB'dan ishlaydi
 * (demo/offline). `enabled=true` bo'lganda esa Firestore listener'lari orqali xabarlar jonli oqadi.
 *
 * Firestore o'zi offline cache beradi, shuning uchun bu real-time + offline-first.
 */
interface ChatRealtimeSource {
    val enabled: Boolean
    fun conversations(): Flow<List<Conversation>>
    fun archivedConversations(): Flow<List<Conversation>>
    fun messages(conversationId: String): Flow<List<Message>>
    suspend fun send(conversationId: String, text: String, time: String, createdAt: Long)
    suspend fun markRead(conversationId: String)
    suspend fun deleteMessage(conversationId: String, messageId: String)
    suspend fun clearMessages(conversationId: String)
    suspend fun deleteConversation(conversationId: String)
    suspend fun setArchived(conversationId: String, archived: Boolean)
}
