package dev.core.domain.model

/**
 * Suhbat turi — oddiy student, kompaniya HR yoki ilova qo'llab-quvvatlash xizmati.
 *
 * [SUPPORT] suhbatlar ro'yxatida KO'RINMAYDI (handoff, 5-ekran): u faqat "Bizneslarim"
 * ekranidagi alohida tugma orqali ochiladi.
 */
enum class ConversationType { PEER, HR, SUPPORT }

/** Xabarlar ro'yxatidagi bitta suhbat. */
data class Conversation(
    val id: String,
    val peerName: String,
    val peerInitial: String,
    val type: ConversationType,
    val online: Boolean,
    val lastMessage: String,
    val lastTime: String,        // "14:22", "Kecha"
    val unreadCount: Int = 0,
    val archived: Boolean = false,
)

/** Suhbat ichidagi bitta xabar. */
data class Message(
    val id: String,
    val conversationId: String,
    val text: String,
    val outgoing: Boolean,       // true → o'ng (violet), false → chap (oq)
    val time: String,            // "10:24"
    val createdAt: Long,         // tartiblash uchun
)
