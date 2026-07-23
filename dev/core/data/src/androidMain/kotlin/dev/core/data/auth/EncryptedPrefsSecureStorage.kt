package dev.core.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dev.core.common.auth.InMemorySecureStorage
import dev.core.common.auth.SecureStorage
import io.github.aakira.napier.Napier

/**
 * Android'da xavfsiz ombor — **EncryptedSharedPreferences**.
 *
 * Kalit Android Keystore'да (apparat himoyasi bor qurilmalarda — TEE/StrongBox), qiymatlar esa
 * AES-256 bilan shifrlangan holda faylда yotadi: `root` bo'lmagan qurilmada boshqa ilova ham,
 * `adb backup` ham tokenni o'qiy olmaydi.
 */
internal class EncryptedPrefsSecureStorage(
    private val prefs: SharedPreferences,
) : SecureStorage {

    override fun read(key: String): String? = prefs.getString(key, null)?.takeIf { it.isNotBlank() }

    override fun write(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun delete(key: String) {
        prefs.edit().remove(key).apply()
    }
}

/**
 * Platformaga mos xavfsiz omborni quradi.
 *
 * Keystore buzilgan bo'lsa (proshivka yangilanishi, zaxiradan tiklash — real hollar)
 * `EncryptedSharedPreferences.create` istisno tashlaydi. Bunda shifrlangan faylni **bir marta**
 * o'chirib qayta urinamiz; u ham ishlamasa xotiradagi zaxiraga o'tamiz — foydalanuvchi bir marta
 * qaytadan kiradi, lekin token hech qachon ochiq holda diskka yozilmaydi.
 */
fun createSecureStorage(context: Context): SecureStorage {
    createPrefs(context)?.let { return EncryptedPrefsSecureStorage(it) }

    Napier.w { "Shifrlangan sozlamalar ochilmadi — fayl tozalanib qayta urinilmoqda" }
    context.deleteSharedPreferences(PREFS_FILE)
    createPrefs(context)?.let { return EncryptedPrefsSecureStorage(it) }

    Napier.e { "Xavfsiz ombor mavjud emas — sessiya faqat ilova ishlagunча saqlanadi" }
    return InMemorySecureStorage()
}

private fun createPrefs(context: Context): SharedPreferences? = runCatching {
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    EncryptedSharedPreferences.create(
        PREFS_FILE,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}.getOrNull()

private const val PREFS_FILE = "elonuz_secure_session"
