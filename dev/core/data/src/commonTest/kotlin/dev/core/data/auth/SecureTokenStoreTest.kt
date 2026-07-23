package dev.core.data.auth

import dev.core.common.auth.AuthTokens
import dev.core.common.auth.InMemorySecureStorage
import dev.core.common.auth.TokenStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * [SecureTokenStore] — asosiy e'tibor **bir martalik ko'chirishga**: eski (shifrlanmagan)
 * ombordagi sessiya yo'qolmasligi va u yerda ochiq holda qolib ketmasligi kerak.
 */
class SecureTokenStoreTest {

    /** Sinov uchun eski ombor — [SqlDelightTokenStore] o'rnini bosadi (bazasiz). */
    private class FakeLegacyStore(
        private var tokens: AuthTokens? = null,
        private var userId: String? = null,
    ) : TokenStore {
        var cleared = false
            private set

        override fun tokens(): AuthTokens? = tokens
        override fun userId(): String? = userId

        override fun save(tokens: AuthTokens, userId: String?) {
            this.tokens = tokens
            if (userId != null) this.userId = userId
        }

        override fun clear() {
            tokens = null
            userId = null
            cleared = true
        }
    }

    @Test
    fun savedTokensAreReadBack() {
        val store = SecureTokenStore(secure = InMemorySecureStorage(), legacy = null)

        store.save(AuthTokens("access-1", "refresh-1"), userId = "usr_1")

        assertEquals(AuthTokens("access-1", "refresh-1"), store.tokens())
        assertEquals("usr_1", store.userId())
    }

    /** Token yangilanganda `userId` uzatilmaydi — avvalgisi saqlanib qolishi kerak. */
    @Test
    fun refreshKeepsUserId() {
        val store = SecureTokenStore(secure = InMemorySecureStorage(), legacy = null)
        store.save(AuthTokens("access-1", "refresh-1"), userId = "usr_1")

        store.save(AuthTokens("access-2", "refresh-2"))

        assertEquals("access-2", store.tokens()?.accessToken)
        assertEquals("usr_1", store.userId())
    }

    @Test
    fun clearRemovesEverything() {
        val store = SecureTokenStore(secure = InMemorySecureStorage(), legacy = null)
        store.save(AuthTokens("access-1", "refresh-1"), userId = "usr_1")

        store.clear()

        assertNull(store.tokens())
        assertNull(store.userId())
    }

    /** Eski ombordagi sessiya xavfsiz omborga ko'chadi va eski nusxa o'chiriladi. */
    @Test
    fun legacySessionIsMigratedOnceAndErased() {
        val legacy = FakeLegacyStore(AuthTokens("old-access", "old-refresh"), userId = "usr_9")
        val secure = InMemorySecureStorage()
        val store = SecureTokenStore(secure = secure, legacy = legacy)

        assertEquals(AuthTokens("old-access", "old-refresh"), store.tokens())
        assertEquals("usr_9", store.userId())

        // Ko'chirilgach eski ombor tozalangan bo'lishi kerak — token u yerda qolmasin.
        assertEquals(true, legacy.cleared)
        assertNull(legacy.tokens())

        // Ikkinchi o'qish endi faqat xavfsiz ombordan keladi.
        assertEquals("old-access", store.tokens()?.accessToken)
    }

    /** Eski omborda sessiya bo'lmasa ham ko'chirish xatosiz o'tadi. */
    @Test
    fun emptyLegacyStoreIsHarmless() {
        val store = SecureTokenStore(secure = InMemorySecureStorage(), legacy = FakeLegacyStore())

        assertNull(store.tokens())
        assertNull(store.userId())
    }

    /** Xavfsiz omborda allaqachon sessiya bo'lsa, eski qiymat uni BOSIB KETMASLIGI kerak. */
    @Test
    fun existingSecureSessionWinsOverLegacy() {
        val secure = InMemorySecureStorage()
        val legacy = FakeLegacyStore(AuthTokens("old-access", "old-refresh"), userId = "usr_old")
        val store = SecureTokenStore(secure = secure, legacy = legacy)
        store.save(AuthTokens("new-access", "new-refresh"), userId = "usr_new")

        assertEquals("new-access", store.tokens()?.accessToken)
        assertEquals("usr_new", store.userId())
        assertEquals(true, legacy.cleared)
    }
}
