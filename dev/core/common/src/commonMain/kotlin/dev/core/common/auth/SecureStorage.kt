package dev.core.common.auth

/**
 * Platformaning **xavfsiz** kalit/qiymat ombori — Android'da EncryptedSharedPreferences,
 * iOS'da Keychain.
 *
 * Faqat kichik maxfiy qiymatlar (sessiya tokenlari) uchun: har ikkala ombor ham tez, lekin
 * katta hajm uchun mo'ljallanmagan. Qiymatlar qurilma qulfdan bir marta ochilgandan keyin
 * o'qiladi (ilova fonda token yangilay olishi uchun).
 *
 * Bu abstraktsiya tufayli [TokenStore] mantig'i bitta joyда (commonMain) qoladi — platformalar
 * faqat saqlash usulini beradi.
 */
interface SecureStorage {

    /** Qiymat yoki `null` (kalit yo'q). */
    fun read(key: String): String?

    /** Qiymatni yozadi (mavjudi ustiga). */
    fun write(key: String, value: String)

    /** Kalitni o'chiradi. Kalit bo'lmasa — hech narsa qilmaydi. */
    fun delete(key: String)
}

/**
 * Xotiradagi zaxira ombor.
 *
 * Platforma ombori ochilmasa (masalan Android Keystore buzilgan) ishlatiladi: sessiya
 * ilova ishlagunча saqlanadi, qayta ochilganда foydalanuvchi bir marta qaytadan kiradi.
 * Tokenni ochiq holda diskka yozishdan ko'ra shu xavfsizroq.
 */
class InMemorySecureStorage : SecureStorage {
    private val values = mutableMapOf<String, String>()

    override fun read(key: String): String? = values[key]

    override fun write(key: String, value: String) {
        values[key] = value
    }

    override fun delete(key: String) {
        values.remove(key)
    }
}
