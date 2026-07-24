package dev.feature.auth.oauth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * iOS Google Sign-In hali ulanmagan.
 *
 * Ulash uchun: GoogleSignIn SDK (SPM/CocoaPods), `Info.plist` da reversed client ID URL scheme,
 * va `GIDSignIn`дан ID token olib [GoogleSignInResult.Success] qaytarish. Shu bo'lguncha
 * Android'даgidek oqim ishlaydi, iOS'да tugma "mavjud emas" xabarini beradi.
 */
actual class GoogleSignIn {
    actual suspend fun signIn(): GoogleSignInResult = GoogleSignInResult.Unavailable
}

@Composable
actual fun rememberGoogleSignIn(): GoogleSignIn = remember { GoogleSignIn() }
