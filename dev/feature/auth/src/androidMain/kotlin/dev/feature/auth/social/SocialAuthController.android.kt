package dev.feature.auth.social

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dev.core.domain.model.AuthProvider
import dev.core.domain.model.ExternalAuthUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

@Composable
actual fun rememberSocialAuthController(): SocialAuthController {
    val context = LocalContext.current
    return remember(context) { AndroidSocialAuthController(context) }
}

private class AndroidSocialAuthController(
    private val context: Context,
) : SocialAuthController {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private var verificationId: String? = null

    override suspend fun signInWithGoogle(): SocialAuthResult {
        val serverClientId = resolveServerClientId()
        if (serverClientId.isBlank()) {
            return SocialAuthResult.Error(
                "Google Client ID topilmadi. google-services.json ni haqiqiy fayl bilan almashtiring."
            )
        }
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)
            val response = credentialManager.getCredential(context, request)
            val googleCredential = GoogleIdTokenCredential.createFrom(response.credential.data)

            val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
            val user = auth.signInWithCredential(firebaseCredential).await().user
                ?: return SocialAuthResult.Error("Firebase foydalanuvchisi topilmadi")

            SocialAuthResult.Success(user.toExternal(AuthProvider.GOOGLE, googleCredential.idToken))
        } catch (e: GetCredentialCancellationException) {
            SocialAuthResult.Cancelled
        } catch (e: GetCredentialException) {
            SocialAuthResult.Error(e.message ?: "Google Sign-In amalga oshmadi")
        } catch (e: Exception) {
            SocialAuthResult.Error(e.message ?: "Google Sign-In amalga oshmadi")
        }
    }

    override suspend fun signInWithApple(): SocialAuthResult {
        val activity = context.findActivity()
            ?: return SocialAuthResult.Error("Activity topilmadi — Apple kirish uchun kerak")
        return try {
            val provider = OAuthProvider.newBuilder("apple.com").apply {
                scopes = listOf("email", "name")
            }.build()
            // Ekran aylanishidan keyin qolgan natija bo'lsa — o'shani olamiz
            val pending = auth.pendingAuthResult
            val result = (pending ?: auth.startActivityForSignInWithProvider(activity, provider)).await()
            val user = result.user
                ?: return SocialAuthResult.Error("Firebase foydalanuvchisi topilmadi")
            SocialAuthResult.Success(user.toExternal(AuthProvider.APPLE))
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            if (msg.contains("cancel", ignoreCase = true) || msg.contains("closed", ignoreCase = true)) {
                SocialAuthResult.Cancelled
            } else {
                SocialAuthResult.Error(msg.ifBlank { "Apple bilan kirish amalga oshmadi" })
            }
        }
    }

    override suspend fun signInWithTelegram(): SocialAuthResult {
        return try {
            val token = TelegramAuthLauncher.launch(context, TelegramConfig.authUrl)
                ?: return SocialAuthResult.Cancelled
            val user = auth.signInWithCustomToken(token).await().user
                ?: return SocialAuthResult.Error("Firebase foydalanuvchisi topilmadi")
            SocialAuthResult.Success(user.toExternal(AuthProvider.TELEGRAM))
        } catch (e: Exception) {
            SocialAuthResult.Error(e.message ?: "Telegram bilan kirish amalga oshmadi")
        }
    }

    override suspend fun sendOtp(phoneNumber: String): OtpSendResult {
        val activity = context.findActivity()
            ?: return OtpSendResult.Error("Activity topilmadi — telefon tasdiqlash uchun kerak")
        return suspendCancellableCoroutine { cont ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // SMS avtomatik o'qildi — to'g'ridan-to'g'ri kiramiz
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { result ->
                            val user = result.user
                            if (cont.isActive) {
                                if (user != null) {
                                    cont.resume(OtpSendResult.AutoVerified(user.toExternal(AuthProvider.PHONE)))
                                } else {
                                    cont.resume(OtpSendResult.Error("Foydalanuvchi topilmadi"))
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            if (cont.isActive) cont.resume(OtpSendResult.Error(e.message ?: "Tasdiqlash xatosi"))
                        }
                }

                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    if (cont.isActive) cont.resume(OtpSendResult.Error(e.message ?: "SMS yuborilmadi"))
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    verificationId = id
                    if (cont.isActive) cont.resume(OtpSendResult.Sent)
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    override suspend fun confirmOtp(code: String): SocialAuthResult {
        val id = verificationId
            ?: return SocialAuthResult.Error("Avval kod yuboring")
        return try {
            val credential = PhoneAuthProvider.getCredential(id, code)
            val user = auth.signInWithCredential(credential).await().user
                ?: return SocialAuthResult.Error("Foydalanuvchi topilmadi")
            verificationId = null
            SocialAuthResult.Success(user.toExternal(AuthProvider.PHONE))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            SocialAuthResult.Error("Kod noto'g'ri")
        } catch (e: Exception) {
            SocialAuthResult.Error(e.message ?: "Tasdiqlash amalga oshmadi")
        }
    }

    /** google-services plagini yaratgan `default_web_client_id` resursini o'qiydi. */
    private fun resolveServerClientId(): String {
        val resId = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName,
        )
        return if (resId != 0) context.getString(resId) else ""
    }
}

private fun FirebaseUser.toExternal(provider: AuthProvider, idToken: String? = null) = ExternalAuthUser(
    uid = uid,
    provider = provider,
    fullName = displayName,
    email = email,
    phoneNumber = phoneNumber,
    photoUrl = photoUrl?.toString(),
    idToken = idToken,
)

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
