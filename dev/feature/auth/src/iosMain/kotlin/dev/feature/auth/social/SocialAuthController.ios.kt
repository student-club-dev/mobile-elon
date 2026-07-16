package dev.feature.auth.social

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.core.domain.model.AuthProvider
import dev.core.domain.model.ExternalAuthUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS'da Firebase va GoogleSignIn SDK'lari Swift tomonida joylashadi.
 * Swift ilova ishga tushganda [IosSocialAuthBridge.delegate] ni o'rnatadi
 * (qarang: iosApp/SocialAuthBridge.swift).
 */
object IosSocialAuthBridge {
    var delegate: IosSocialAuthDelegate? = null
}

/** Swift shu interfeysni amalga oshiradi (FirebaseAuth + GoogleSignIn orqali). */
interface IosSocialAuthDelegate {
    /** onResult(user, errorMessage) — user null bo'lsa xato yoki bekor qilingan. */
    fun signInWithGoogle(onResult: (IosAuthUser?, String?) -> Unit)

    /** Apple (ASAuthorization + Firebase OAuthProvider) orqali kirish. */
    fun signInWithApple(onResult: (IosAuthUser?, String?) -> Unit)

    /**
     * Telegram — [url] ni ASWebAuthenticationSession'da ochadi, [callbackScheme] bilan
     * qaytgan custom token orqali Firebase'ga kiradi.
     */
    fun signInWithTelegram(url: String, callbackScheme: String, onResult: (IosAuthUser?, String?) -> Unit)

    /** onResult(sent, autoUser, errorMessage) — iOS'da odatda faqat sent=true. */
    fun sendOtp(phoneNumber: String, onResult: (Boolean, IosAuthUser?, String?) -> Unit)

    fun confirmOtp(code: String, onResult: (IosAuthUser?, String?) -> Unit)
}

/** Swift tomonidan yaratiladigan oddiy ma'lumot ko'chiruvchisi. */
data class IosAuthUser(
    val uid: String,
    val provider: String, // "google" yoki "phone"
    val fullName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
)

@Composable
actual fun rememberSocialAuthController(): SocialAuthController =
    remember { IosSocialAuthController() }

private class IosSocialAuthController : SocialAuthController {

    private val delegate: IosSocialAuthDelegate?
        get() = IosSocialAuthBridge.delegate

    override suspend fun signInWithGoogle(): SocialAuthResult {
        val d = delegate ?: return notWired()
        return suspendCancellableCoroutine { cont ->
            d.signInWithGoogle { user, error ->
                if (!cont.isActive) return@signInWithGoogle
                cont.resume(user.toResult(error))
            }
        }
    }

    override suspend fun signInWithApple(): SocialAuthResult {
        val d = delegate ?: return notWired()
        return suspendCancellableCoroutine { cont ->
            d.signInWithApple { user, error ->
                if (!cont.isActive) return@signInWithApple
                cont.resume(user.toResult(error))
            }
        }
    }

    override suspend fun signInWithTelegram(): SocialAuthResult {
        val d = delegate ?: return notWired()
        return suspendCancellableCoroutine { cont ->
            d.signInWithTelegram(TelegramConfig.authUrl, TelegramConfig.REDIRECT_SCHEME) { user, error ->
                if (!cont.isActive) return@signInWithTelegram
                cont.resume(user.toResult(error))
            }
        }
    }

    override suspend fun sendOtp(phoneNumber: String): OtpSendResult {
        val d = delegate ?: return OtpSendResult.Error(NOT_WIRED)
        return suspendCancellableCoroutine { cont ->
            d.sendOtp(phoneNumber) { sent, autoUser, error ->
                if (!cont.isActive) return@sendOtp
                val result = when {
                    autoUser != null -> OtpSendResult.AutoVerified(autoUser.toExternal())
                    sent -> OtpSendResult.Sent
                    else -> OtpSendResult.Error(error ?: "SMS yuborilmadi")
                }
                cont.resume(result)
            }
        }
    }

    override suspend fun confirmOtp(code: String): SocialAuthResult {
        val d = delegate ?: return notWired()
        return suspendCancellableCoroutine { cont ->
            d.confirmOtp(code) { user, error ->
                if (!cont.isActive) return@confirmOtp
                cont.resume(user.toResult(error))
            }
        }
    }

    private fun notWired() = SocialAuthResult.Error(NOT_WIRED)

    private fun IosAuthUser?.toResult(error: String?): SocialAuthResult = when {
        this != null -> SocialAuthResult.Success(toExternal())
        error != null -> SocialAuthResult.Error(error)
        else -> SocialAuthResult.Cancelled
    }

    companion object {
        private const val NOT_WIRED =
            "iOS social auth ulanmagan. iosApp/SocialAuthBridge.swift ni sozlang."
    }
}

private fun IosAuthUser.toExternal(): ExternalAuthUser = ExternalAuthUser(
    uid = uid,
    provider = when (provider) {
        "google" -> AuthProvider.GOOGLE
        "apple" -> AuthProvider.APPLE
        "telegram" -> AuthProvider.TELEGRAM
        else -> AuthProvider.PHONE
    },
    fullName = fullName,
    email = email,
    phoneNumber = phoneNumber,
    photoUrl = photoUrl,
)
