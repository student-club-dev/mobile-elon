package dev.feature.auth.biometric

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_WEAK

actual class BiometricAuthenticator(private val activity: FragmentActivity?) {

    actual fun canAuthenticate(): Boolean {
        val a = activity ?: return false
        return BiometricManager.from(a).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }

    actual suspend fun authenticate(title: String, subtitle: String, cancel: String): BiometricOutcome =
        suspendCancellableCoroutine { cont ->
            val a = activity
            if (a == null) {
                if (cont.isActive) cont.resume(BiometricOutcome.UNAVAILABLE)
                return@suspendCancellableCoroutine
            }
            val executor = ContextCompat.getMainExecutor(a)
            val prompt = BiometricPrompt(a, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (cont.isActive) cont.resume(BiometricOutcome.SUCCESS)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val outcome = when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED,
                        -> BiometricOutcome.CANCELLED
                        BiometricPrompt.ERROR_NO_BIOMETRICS,
                        BiometricPrompt.ERROR_HW_NOT_PRESENT,
                        BiometricPrompt.ERROR_HW_UNAVAILABLE,
                        -> BiometricOutcome.UNAVAILABLE
                        else -> BiometricOutcome.FAILED
                    }
                    if (cont.isActive) cont.resume(outcome)
                }

                override fun onAuthenticationFailed() {
                    // Bitta urinish tanilmadi — prompt ochiq qoladi (natija bermаymiz).
                }
            })
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(cancel)
                .setAllowedAuthenticators(AUTHENTICATORS)
                .build()
            prompt.authenticate(info)
        }
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    val context = LocalContext.current
    return remember { BiometricAuthenticator(context.findFragmentActivity()) }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
