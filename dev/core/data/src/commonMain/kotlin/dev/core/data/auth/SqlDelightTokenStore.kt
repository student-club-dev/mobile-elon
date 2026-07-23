package dev.core.data.auth

import dev.core.common.auth.AuthTokens
import dev.core.common.auth.TokenStore
import dev.core.database.sql.ElonUzDatabase

/**
 * [TokenStore] ning local baza (SQLDelight `AppSettingEntity`) ustidagi implementatsiyasi.
 *
 * ⚠️ **Eskirgan (legacy).** Tokenlar endi platformaning shifrlangan omborida saqlanadi
 * ([SecureTokenStore] → Android EncryptedSharedPreferences / iOS Keychain). Bu klass faqat
 * **bir martalik ko'chirish** uchun qoldirilgan: eski o'rnatmalarda shu jadvalda sessiya bo'lishi
 * mumkin, [SecureTokenStore] uni o'qib xavfsiz omborga ko'chiradi va bu yerdagi yozuvni o'chiradi.
 *
 * Yangi kod bu klassni to'g'ridan-to'g'ri ishlatmaydi — Koin `TokenStore` sifatida
 * [SecureTokenStore] ni beradi.
 */
class SqlDelightTokenStore(
    private val database: ElonUzDatabase,
) : TokenStore {

    private val queries get() = database.appSettingQueries

    override fun tokens(): AuthTokens? {
        val access = read(KEY_ACCESS) ?: return null
        val refresh = read(KEY_REFRESH) ?: return null
        return AuthTokens(accessToken = access, refreshToken = refresh)
    }

    override fun save(tokens: AuthTokens, userId: String?) {
        queries.transaction {
            queries.upsert(KEY_ACCESS, tokens.accessToken)
            queries.upsert(KEY_REFRESH, tokens.refreshToken)
            // Token yangilanganda uid o'zgarmaydi — `null` kelsa avvalgisi qoladi.
            if (userId != null) queries.upsert(KEY_USER_ID, userId)
        }
    }

    override fun clear() {
        queries.transaction {
            queries.deleteByKey(KEY_ACCESS)
            queries.deleteByKey(KEY_REFRESH)
            queries.deleteByKey(KEY_USER_ID)
        }
    }

    override fun userId(): String? = read(KEY_USER_ID)

    private fun read(key: String): String? =
        queries.selectByKey(key).executeAsOneOrNull()?.takeIf { it.isNotBlank() }

    private companion object {
        const val KEY_ACCESS = "auth_access_token"
        const val KEY_REFRESH = "auth_refresh_token"
        const val KEY_USER_ID = "auth_user_id"
    }
}
