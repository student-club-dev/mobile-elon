package dev.feature.auth.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.core.uikit.component.OutlineButton
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_google_login
import dev.core.uikit.resources.auth_google_unavailable
import dev.feature.auth.oauth.GoogleSignInResult
import dev.feature.auth.oauth.rememberGoogleSignIn
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * "Google bilan kirish" tugmasi (`POST /auth/business/oauth/google`).
 *
 * Alohida `public` komponent, chunki kirish ekrani (`BusinessWelcomeScreen`) boshqa modulда va
 * `rememberGoogleSignIn` ga bog'lana olmaydi — tugma unga slot sifatida uzatiladi.
 *
 * Google — yangi foydalanuvchi uchun yagona kirish yo'li: parolli ro'yxatdan o'tish olib
 * tashlangan, hisob birinchi Google kirishida backend tomonда ochiladi.
 */
@Composable
fun GoogleSignInButton(vm: AuthFlowViewModel) {
    val googleSignIn = rememberGoogleSignIn()
    val scope = rememberCoroutineScope()
    // `stringResource` faqat kompozitsiya doirasида — lambda ichида chaqirib bo'lmaydi.
    val googleUnavailable = stringResource(Res.string.auth_google_unavailable)

    OutlineButton(
        stringResource(Res.string.auth_google_login),
        onClick = {
            scope.launch {
                when (val result = googleSignIn.signIn()) {
                    is GoogleSignInResult.Success -> vm.signInWithGoogle(result.idToken)
                    is GoogleSignInResult.Failed -> vm.showAuthError(result.message)
                    GoogleSignInResult.Unavailable -> vm.showAuthError(googleUnavailable)
                    // Foydalanuvchi o'zi bekor qildi — xato ko'rsatilmaydi.
                    GoogleSignInResult.Cancelled -> Unit
                }
            }
        },
    )
}
