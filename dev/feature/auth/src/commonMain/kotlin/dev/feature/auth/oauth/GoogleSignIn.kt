package dev.feature.auth.oauth

import androidx.compose.runtime.Composable

/** Google Sign-In natijasi — ID token yoki nima uchun bo'lmagani. */
sealed interface GoogleSignInResult {
    /** [idToken] — backend (`/auth/business/oauth/google`) tekshiradigan Google ID token. */
    data class Success(val idToken: String) : GoogleSignInResult

    /** Foydalanuvchi oynani yopdi. */
    data object Cancelled : GoogleSignInResult

    /** Bu platformada/qurilmada Google kirish sozlanmagan (masalan client ID yo'q, iOS SDK yo'q). */
    data object Unavailable : GoogleSignInResult

    /** Boshqa xato (tarmoq, hisob topilmadi va h.k.). */
    data class Failed(val message: String) : GoogleSignInResult
}

/**
 * Platformaga xos Google Sign-In (ID token oladi).
 * - Android: `androidx.credentials` (Credential Manager) + Google ID; Web (server) client ID kerak.
 * - iOS: GoogleSignIn SDK (hozircha ulanmagan — [GoogleSignInResult.Unavailable]).
 *
 * Token backendга yuboriladi, u yerда tekshirilib sessiya ochiladi.
 */
expect class GoogleSignIn {
    /** Google tanlash oynasini ochadi va ID token'ni (yoki xatoni) qaytaradi. */
    suspend fun signIn(): GoogleSignInResult
}

/** Joriy platforma konteksti bilan [GoogleSignIn] yaratadi/eslaydi. */
@Composable
expect fun rememberGoogleSignIn(): GoogleSignIn
