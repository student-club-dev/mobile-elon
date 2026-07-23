package dev.core.data.auth

import dev.core.common.auth.SecureStorage
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * iOS'da xavfsiz ombor — **Keychain** (`kSecClassGenericPassword`).
 *
 * Keychain ma'lumotni tizim darajasida shifrlaydi va ilova sandbox'idan tashqarida saqlaydi:
 * qurilma zaxirasi (iTunes/iCloud) va fayl tizimi orqali o'qib bo'lmaydi.
 *
 * [kSecAttrAccessibleAfterFirstUnlock] tanlangan — qurilma qayta yoqilgach bir marta qulfdan
 * chiqarilsa bas: shu tufayli ilova fonda ham token yangilay oladi (`ThisDeviceOnly` emas,
 * chunki foydalanuvchi qurilma almashtirsa ham sessiyasi ko'chishi kutiladi).
 */
@OptIn(ExperimentalForeignApi::class)
internal class KeychainSecureStorage(
    private val service: String,
) : SecureStorage {

    override fun read(key: String): String? = withQuery(key) { query ->
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        memScoped {
            val found = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, found.ptr)
            if (status != errSecSuccess) return@memScoped null
            // CFBridgingRelease egalikni ARC'ga beradi — qo'lda CFRelease qilish shart emas.
            val data = CFBridgingRelease(found.value) as? NSData ?: return@memScoped null
            (NSString.create(data = data, encoding = NSUTF8StringEncoding) as String?)
                ?.takeIf { it.isNotBlank() }
        }
    }

    override fun write(key: String, value: String) {
        // Keychain `update` alohida chaqiruv talab qiladi — eskisini o'chirib qayta yozish
        // soddaroq va bir xil natija beradi.
        delete(key)
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        withQuery(key) { query ->
            val bridged = CFBridgingRetain(data)
            CFDictionaryAddValue(query, kSecValueData, bridged)
            CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlock)
            SecItemAdd(query as CFDictionaryRef, null)
            bridged?.let { CFRelease(it) }
        }
    }

    override fun delete(key: String) {
        withQuery(key) { query -> SecItemDelete(query as CFDictionaryRef) }
    }

    /**
     * Bitta yozuvni aniqlaydigan so'rov lug'atini quradi va [block] tugagach uni bo'shatadi.
     *
     * Lug'at `kCFType...CallBacks` bilan yaratilgani uchun qo'shilgan qiymatlarni o'zi
     * retain/release qiladi; biz faqat `CFBridgingRetain` bilan yaratgan `+1` havolalarni
     * qaytaramiz.
     */
    private inline fun <T> withQuery(key: String, block: (CFMutableDictionaryRef) -> T): T {
        val query = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr,
        )!!
        val serviceRef = CFBridgingRetain(service as NSString)
        val accountRef = CFBridgingRetain(key as NSString)
        try {
            CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(query, kSecAttrService, serviceRef)
            CFDictionaryAddValue(query, kSecAttrAccount, accountRef)
            return block(query)
        } finally {
            serviceRef?.let { CFRelease(it) }
            accountRef?.let { CFRelease(it) }
            CFRelease(query)
        }
    }
}

/** Platformaga mos xavfsiz omborni beradi. Keychain har doim mavjud — zaxira kerak emas. */
@OptIn(ExperimentalForeignApi::class)
fun createSecureStorage(): SecureStorage = KeychainSecureStorage(service = KEYCHAIN_SERVICE)

/** Keychain yozuvlari shu "xizmat" nomi ostida guruhlanadi. */
private const val KEYCHAIN_SERVICE = "uz.elonuz.session"
