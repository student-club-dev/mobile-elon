package dev.core.domain.model

data class User(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
)

enum class UserRole { STUDENT, ADMIN }

/** Foydalanuvchi ro'yxatdan o'tgan usul. */
enum class AuthProvider { EMAIL, GOOGLE, APPLE, PHONE, TELEGRAM }

/**
 * Firebase (Google yoki Telefon OTP) orqali autentifikatsiyadan o'tgan
 * foydalanuvchi ma'lumoti. Platforma qatlami (Android/iOS) shu modelni qaytaradi,
 * repository esa uni domen [User] ga aylantirib saqlaydi / backend bilan sinxronlaydi.
 */
data class ExternalAuthUser(
    val uid: String,
    val provider: AuthProvider,
    val fullName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val idToken: String? = null,
)
