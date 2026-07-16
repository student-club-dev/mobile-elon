package dev.feature.auth.biometric

import androidx.compose.runtime.Composable

/** Biometrik tekshiruv natijasi. */
enum class BiometricOutcome {
    SUCCESS,       // muvaffaqiyatli tasdiqlandi
    FAILED,        // tanilmadi / xato
    CANCELLED,     // foydalanuvchi bekor qildi
    UNAVAILABLE,   // qurilmada biometrika yo'q / sozlanmagan
}

/**
 * Platformaga xos biometrik autentifikatsiya (F1).
 * - Android: `androidx.biometric` (Face/Fingerprint), host `FragmentActivity`.
 * - iOS: `LAContext` (Face ID / Touch ID).
 *
 * Oqim: sessiya cache'da bo'lsa, biometrik tasdiqdan keyin to'g'ridan-to'g'ri HOME.
 */
expect class BiometricAuthenticator {
    /** Qurilmada biometrika mavjud va sozlanganmi. */
    fun canAuthenticate(): Boolean

    /** Biometrik so'rovni ko'rsatadi va natijani kutadi. */
    suspend fun authenticate(title: String, subtitle: String, cancel: String): BiometricOutcome
}

/** Joriy platforma konteksti bilan [BiometricAuthenticator] yaratadi/eslaydi. */
@Composable
expect fun rememberBiometricAuthenticator(): BiometricAuthenticator
