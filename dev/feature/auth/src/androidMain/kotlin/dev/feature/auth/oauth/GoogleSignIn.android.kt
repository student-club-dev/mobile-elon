package dev.feature.auth.oauth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Android Google Sign-In — Credential Manager orqali.
 *
 * [webClientId] — Google Cloud'даgi **Web** (server) OAuth client ID. Ilova strings.xml da
 * `google_web_client_id` sifatida beriladi (elonUzApp moduli). Bo'sh bo'lsa — [GoogleSignInResult.Unavailable].
 * Ishlashi uchun Google Cloud Console'да ilova imzosining **SHA-1**i ham ro'yxatдан o'tishi shart.
 */
actual class GoogleSignIn(
    private val activity: Activity?,
    private val webClientId: String,
) {
    actual suspend fun signIn(): GoogleSignInResult {
        val act = activity ?: return GoogleSignInResult.Unavailable
        if (webClientId.isBlank()) return GoogleSignInResult.Unavailable

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            // Yangi foydalanuvchi ham kira olsin (avval ruxsat bermagan hisoblar ham ko'rinadi).
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val response = CredentialManager.create(act).getCredential(act, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInResult.Success(googleCredential.idToken)
            } else {
                GoogleSignInResult.Failed("Google credential turi kutilmagan")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            GoogleSignInResult.Failed("Qurilmada Google hisob topilmadi")
        } catch (e: GetCredentialException) {
            GoogleSignInResult.Failed(e.message ?: "Google kirish amalga oshmadi")
        }
    }
}

@Composable
actual fun rememberGoogleSignIn(): GoogleSignIn {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    // Client ID ilova (elonUzApp) strings.xml da; modulдан R'siz o'qiymiz.
    val clientId = remember(context) {
        val resId = context.resources.getIdentifier("google_web_client_id", "string", context.packageName)
        if (resId != 0) context.getString(resId) else ""
    }
    return remember(activity, clientId) { GoogleSignIn(activity, clientId) }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
