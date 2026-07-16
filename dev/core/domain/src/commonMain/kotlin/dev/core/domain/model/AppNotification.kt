package dev.core.domain.model

/** Bildirishnoma turi — ikonka va rangni belgilaydi. */
enum class NotificationType { JOB, DISCOUNT, AD, CHAT, SYSTEM }

/** Foydalanuvchi bildirishnomasi. */
data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val timeLabel: String,   // "2 soat oldin"
    val read: Boolean,
)
