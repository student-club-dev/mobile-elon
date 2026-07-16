package dev.feature.auth.data

import dev.core.common.AuthTokenProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

/**
 * [AuthTokenProvider] ning Firebase implementatsiyasi — joriy Firebase sessiyasidan
 * ID token qaytaradi. Backend so'rovlariga `Authorization: Bearer <idToken>` sifatida qo'shiladi.
 *
 * Backend ID tokenni Firebase Admin SDK bilan tekshiradi (uid, email, telefon...).
 */
class FirebaseTokenProvider : AuthTokenProvider {
    override suspend fun currentToken(): String? = try {
        // forceRefresh=false — Firebase eskirgan bo'lsa o'zi yangilaydi (odatda 1 soatlik token).
        Firebase.auth.currentUser?.getIdToken(false)
    } catch (e: Exception) {
        null
    }
}
