package dev.feature.auth.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

@OptIn(ExperimentalForeignApi::class)
actual class BiometricAuthenticator {

    actual fun canAuthenticate(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)
    }

    actual suspend fun authenticate(title: String, subtitle: String, cancel: String): BiometricOutcome =
        suspendCancellableCoroutine { cont ->
            val context = LAContext()
            context.localizedCancelTitle = cancel
            if (!context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)) {
                if (cont.isActive) cont.resume(BiometricOutcome.UNAVAILABLE)
                return@suspendCancellableCoroutine
            }
            context.evaluatePolicy(
                policy = LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = subtitle.ifBlank { title },
            ) { success, _ ->
                if (cont.isActive) cont.resume(if (success) BiometricOutcome.SUCCESS else BiometricOutcome.FAILED)
            }
        }
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator = remember { BiometricAuthenticator() }
