package dev.core.data.auth

import dev.core.common.auth.AuthTokens
import dev.core.common.auth.SecureStorage
import dev.core.common.auth.TokenStore
import kotlin.concurrent.Volatile

/**
 * [TokenStore] ning **xavfsiz** implementatsiyasi — tokenlar platformaning shifrlangan
 * omborida yotadi ([SecureStorage]: Android EncryptedSharedPreferences, iOS Keychain).
 *
 * [legacy] — eski, shifrlanmagan ombor ([SqlDelightTokenStore]). Birinchi murojaatда undagi
 * sessiya **bir marta** ko'chiriladi va eski yozuv o'chiriladi, shuning uchun yangilanishdan
 * keyin foydalanuvchi qaytadan kirishga majbur bo'lmaydi va ochiq token bazada qolmaydi.
 */
class SecureTokenStore(
    private val secure: SecureStorage,
    private val legacy: TokenStore? = null,
) : TokenStore {

    @Volatile
    private var migrationDone = legacy == null

    override fun tokens(): AuthTokens? {
        migrateIfNeeded()
        val access = secure.read(KEY_ACCESS) ?: return null
        val refresh = secure.read(KEY_REFRESH) ?: return null
        return AuthTokens(accessToken = access, refreshToken = refresh)
    }

    override fun save(tokens: AuthTokens, userId: String?) {
        // Yangi juftlik yozilganда ko'chirishning ma'nosi yo'q — eski yozuvni shunchaki tozalaymiz.
        migrationDone = true
        legacy?.clear()

        secure.write(KEY_ACCESS, tokens.accessToken)
        secure.write(KEY_REFRESH, tokens.refreshToken)
        // Token yangilanganда uid o'zgarmaydi — `null` kelsa avvalgisi qoladi.
        if (userId != null) secure.write(KEY_USER_ID, userId)
    }

    override fun clear() {
        migrationDone = true
        legacy?.clear()
        secure.delete(KEY_ACCESS)
        secure.delete(KEY_REFRESH)
        secure.delete(KEY_USER_ID)
    }

    override fun userId(): String? {
        migrateIfNeeded()
        return secure.read(KEY_USER_ID)
    }

    /**
     * Eski ombordagi sessiyani bir marta ko'chiradi.
     *
     * Ikki oqim bir vaqtда kirsa ham xavfsiz: ko'chirish **idempotent** (ayni qiymatlar
     * qayta yoziladi), so'ng eski yozuv o'chiriladi.
     */
    private fun migrateIfNeeded() {
        if (migrationDone) return
        migrationDone = true
        val old = legacy?.tokens()
        if (old != null && secure.read(KEY_ACCESS) == null) {
            secure.write(KEY_ACCESS, old.accessToken)
            secure.write(KEY_REFRESH, old.refreshToken)
            legacy.userId()?.let { secure.write(KEY_USER_ID, it) }
        }
        legacy?.clear()
    }

    private companion object {
        const val KEY_ACCESS = "auth_access_token"
        const val KEY_REFRESH = "auth_refresh_token"
        const val KEY_USER_ID = "auth_user_id"
    }
}
