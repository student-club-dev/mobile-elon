package dev.core.common.error

/**
 * Ilova bo'ylab yagona **typed xato** ierarxiyasi — oddiy `String` xabar o'rniga.
 *
 * Har bir xato [userMessage] beradi (foydalanuvchiga ko'rsatiladigan o'zbekcha matn) va
 * asl [cause] ni saqlaydi (log/telemetriya uchun). Repository/UseCase qatlamlari istalgan
 * `Throwable` ni [toAppException] orqali shu turlarga aylantiradi — UI esa faqat shu
 * cheklangan to'plam bilan ishlaydi (retry ko'rsatishmi, login'ga yuborishmi va h.k.).
 */
sealed class AppException(
    val userMessage: String,
    cause: Throwable? = null,
) : Exception(userMessage, cause) {

    /** Internet yo'q — retry mazmunli. */
    class NoInternet(cause: Throwable? = null) :
        AppException("Internet aloqasi yo'q. Ulanishni tekshirib, qayta urining.", cause)

    /** So'rov muddati tugadi. */
    class Timeout(cause: Throwable? = null) :
        AppException("So'rov vaqti tugadi. Qayta urining.", cause)

    /** Sessiya tugagan yoki kirilmagan — login kerak. */
    class Unauthorized(cause: Throwable? = null) :
        AppException("Sessiya tugagan. Iltimos, qaytadan kiring.", cause)

    /** Ruxsat yo'q (403). */
    class PermissionDenied(cause: Throwable? = null) :
        AppException("Bu amal uchun ruxsat yo'q.", cause)

    /** Ma'lumot topilmadi (404). */
    class NotFound(cause: Throwable? = null) :
        AppException("Ma'lumot topilmadi.", cause)

    /** Server xatosi (5xx). */
    class Server(val code: Int? = null, cause: Throwable? = null) :
        AppException("Serverда xatolik. Birozdan so'ng qayta urining.", cause)

    /**
     * Kiritilgan ma'lumot noto'g'ri (validatsiya / 4xx).
     *
     * [fields] — backend qaytargan **maydonga bog'langan** xatolar: `{"phoneNumber": "Noto'g'ri
     * format"}`. Kalit — so'rov tanasidagi maydon nomi (masalan `UpdateProfileDto` maydoni),
     * qiymat — foydalanuvchiga ko'rsatiladigan matn. Forma ularni aynan shu maydon ostida
     * ko'rsatadi, [userMessage] esa umumiy xabar bo'lib qoladi. Bo'sh bo'lishi normal —
     * hamma 4xx ham maydon-darajali emas.
     */
    class Validation(
        val reason: String,
        val fields: Map<String, String> = emptyMap(),
        cause: Throwable? = null,
    ) : AppException(reason, cause)

    /** Boshqa/noma'lum xato. */
    class Unknown(message: String = "Noma'lum xatolik yuz berdi.", cause: Throwable? = null) :
        AppException(message, cause)
}

/**
 * Maydonga bog'langan validatsiya xatolari — faqat [AppException.Validation] da bo'ladi,
 * qolgan turlarda bo'sh. Chaqiruvchi `is Validation` tekshiruvini takrorlamasligi uchun.
 */
val AppException.fieldErrors: Map<String, String>
    get() = (this as? AppException.Validation)?.fields.orEmpty()

/**
 * Istalgan [Throwable] ni [AppException] ga aylantiradi.
 *
 * [isOnline] — chaqiruvchi (repository) internet holatini bilsa uzatadi: offline bo'lsa
 * xato aniq [AppException.NoInternet] bo'ladi. Aks holda xato matni/turi bo'yicha taxmin
 * qilinadi (Ktor va platforma istisnolarining umumiy so'zlari).
 */
fun Throwable.toAppException(isOnline: Boolean = true): AppException {
    if (this is AppException) return this
    val msg = message?.lowercase() ?: ""
    return when {
        !isOnline -> AppException.NoInternet(this)
        msg.containsAny("permission", "denied", "permission_denied") -> AppException.PermissionDenied(this)
        msg.containsAny("unauthenticated", "unauthorized", "not authenticated", "sign in") ->
            AppException.Unauthorized(this)
        msg.containsAny("not found", "no document", "not_found") -> AppException.NotFound(this)
        msg.containsAny("timeout", "timed out", "deadline") -> AppException.Timeout(this)
        msg.containsAny("unavailable", "network", "host", "connection", "internet", "offline", "resolve") ->
            AppException.NoInternet(this)
        msg.containsAny("internal", "server", "unknown error") -> AppException.Server(cause = this)
        else -> AppException.Unknown(message ?: "Noma'lum xatolik yuz berdi.", this)
    }
}

private fun String.containsAny(vararg needles: String): Boolean =
    needles.any { this.contains(it) }
