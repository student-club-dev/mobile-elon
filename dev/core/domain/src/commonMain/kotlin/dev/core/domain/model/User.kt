package dev.core.domain.model

/**
 * Kirgan foydalanuvchining sessiya ma'lumoti (local kesh + backend).
 *
 * [id] — backend'ning foydalanuvchi identifikatori: access-token (JWT) ning `sub` maydonidan
 * olinadi va biznes/e'lon egaligini belgilaydi (`BusinessDto.ownerUserId` bilan solishtiriladi).
 * Ism/telefon/rasm profil (`GET /profile/me`) dan to'ladi.
 */
data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
)

/** Backend `ProfileRoleDto` bilan bir xil to'plam. */
enum class UserRole { STUDENT, BUSINESS, EMPLOYER, UNIVERSITY }

/**
 * Kirish/ro'yxatdan o'tish identifikatori. Backend email va telefonni **alohida** maydonlarda
 * kutadi (`LoginDto.email` / `LoginDto.phoneNumber`), foydalanuvchi esa bitta maydonga yozadi —
 * shuning uchun ajratish domenда bo'ladi.
 */
sealed interface AuthIdentifier {

    data class Email(val value: String) : AuthIdentifier

    /** E.164 formatda, masalan `+998901234567`. */
    data class Phone(val value: String) : AuthIdentifier

    companion object {
        /**
         * Foydalanuvchi kiritgan matnni identifikatorga aylantiradi.
         * `null` — na to'g'ri email, na to'g'ri O'zbekiston raqami.
         */
        fun of(raw: String): AuthIdentifier? {
            val text = raw.trim()
            if (text.isEmpty()) return null
            if (text.contains('@')) {
                return if (text.substringAfter('@').contains('.')) Email(text.lowercase()) else null
            }
            val digits = text.filter { it.isDigit() }
            val local = when {
                digits.length == 9 -> digits
                digits.length == 12 && digits.startsWith("998") -> digits.drop(3)
                else -> return null
            }
            return Phone("+998$local")
        }
    }
}

/**
 * SMS kod so'ralganda backend qaytaradigan cheklovlar (`OtpRequestResultDto`) —
 * UI shundan taymer va "qayta yuborish" tugmasini boshqaradi.
 */
data class OtpChallenge(
    val expiresInSeconds: Int,
    val resendCooldownSeconds: Int,
)

/** Hisobning faol qurilma sessiyasi (`GET /auth/business/sessions`). */
data class DeviceSession(
    val id: String,
    val deviceName: String?,
    val platform: String?,
    val ipAddress: String?,
    val lastUsedAtMillis: Long?,
    val createdAtMillis: Long,
)
