package dev.feature.auth.oauth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Android Google Sign-In — Credential Manager orqali.
 *
 * [webClientId] — Google Cloud'даgi **Web** (server) OAuth client ID. Ilova strings.xml da
 * `google_web_client_id` sifatida beriladi (elonUzApp moduli).
 * Ishlashi uchun Google Cloud Console'да ilova imzosining **SHA-1**i ham ro'yxatдан
 * o'tishi shart (debug va release uchun alohida).
 *
 * **Nega [GetSignInWithGoogleOption], `GetGoogleIdOption` emas:** ikkinchisi "one-tap"
 * oqimi — u qurilmада allaqachon kirgan hisoblarni pastdan kichik varaqда taklif qiladi va
 * mos hisob topilmasa `NoCredentialException` bilan tugaydi, ya'ni foydalanuvchi **hisob
 * tanlay olmaydi**. `GetSignInWithGoogleOption` esa "Sign in with Google" tugmasi uchun
 * mo'ljallangan: har doim to'liq hisob tanlash oynasini ochadi va "boshqa hisob qo'shish"
 * imkonini ham beradi.
 */
actual class GoogleSignIn(
    private val activity: Activity?,
    private val webClientId: String,
) {
    actual suspend fun signIn(): GoogleSignInResult {
        val act = activity ?: return GoogleSignInResult.Unavailable
        // Eng ko'p uchraydigan sozlash xatosi — shuning uchun umumiy "mavjud emas" emas,
        // aniq sabab qaytariladi.
        if (webClientId.isBlank()) {
            return GoogleSignInResult.Failed(
                "Google kirish sozlanmagan: strings.xml даgi google_web_client_id bo'sh",
            )
        }

        // Tugma bosilgani — aniq niyat, shuning uchun to'liq hisob tanlash oynasi.
        val option: CredentialOption = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

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
            GoogleSignInResult.Failed("Qurilmада Google hisob topilmadi. Sozlamalarда hisob qo'shing.")
        } catch (e: GetCredentialException) {
            // Bu yerга ko'pincha noto'g'ri client ID yoki ro'yxatdан o'tmagan SHA-1 tushadi —
            // asl xabar saqlanadi, chunki sababni faqat shundan bilish mumkin.
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
