package dev.core.domain.repository

/**
 * Joriy sessiyaning `uid` manbai — kim kirgan bo'lsa. Ma'lumot repository'lari (profil, biznes,
 * e'lon egaligi) shu orqali `uid` oladi, **Firebase'ga bevosita bog'lanmasдан**.
 *
 * - Prod: `FirebaseSessionProvider` → `Firebase.auth.currentUser?.uid`.
 * - Local test (`USE_LOCAL_DATA`): `DevSessionProvider` → local `UserEntity` dagi uid.
 *
 * Shu abstraktsiya tufayli backend/Firebase'siz local rejimда ham profil/biznes ishlaydi.
 */
interface SessionProvider {
    fun currentUid(): String?
}
