package dev.feature.auth.data

import dev.core.domain.repository.SessionProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

/** Prod sessiya manbai — Firebase auth uid. */
class FirebaseSessionProvider : SessionProvider {
    override fun currentUid(): String? = Firebase.auth.currentUser?.uid
}
