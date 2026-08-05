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

    /**
     * 401 — kirish talab qilinadi.
     *
     * Bitta status ostida foydalanuvchi uchun **ikki xil** hodisa keladi:
     * - sessiya eskirgan/yaroqsiz (`TOKEN_EXPIRED`, `UNAUTHORIZED`) — qaytadan kirish kerak;
     * - kirish ma'lumoti xato (`INVALID_CREDENTIALS`) — parolni to'g'rilash kerak.
     *
     * Ilgari ikkalasi ham "Sessiya tugagan" deb ko'rsatilardi: parolni xato tergan
     * foydalanuvchi nima qilishini tushunmasdi (u kirmoqchi edi, sessiyasi umuman yo'q edi).
     * Shuning uchun [code] saqlanadi va sessiya kodlari bo'lmaganda backendning o'z matni
     * ([userMessage]) ko'rsatiladi.
     */
    class Unauthorized(
        val code: String? = null,
        message: String = SESSION_EXPIRED_MESSAGE,
        cause: Throwable? = null,
    ) : AppException(message, cause) {

        /** Sessiya masalasimi (ya'ni foydalanuvchini kirish ekraniga yuborish kerakmi). */
        val sessionExpired: Boolean get() = code == null || code in SESSION_EXPIRED_CODES

        companion object {
            const val SESSION_EXPIRED_MESSAGE = "Sessiya tugagan. Iltimos, qaytadan kiring."

            /** Backend sessiya uchun aynan shu kodlarni beradi (`BACKEND_PROMPT.md` §xatolar). */
            val SESSION_EXPIRED_CODES = setOf("TOKEN_EXPIRED", "UNAUTHORIZED")
        }
    }

    /**
     * Ruxsat yo'q (403) — masalan begona biznes yoki tasdiqlanmagan raqam
     * (`PHONE_NOT_VERIFIED`). Sabab har xil, shuning uchun matn backenddan keladi.
     */
    class PermissionDenied(
        val code: String? = null,
        message: String = "Bu amal uchun ruxsat yo'q.",
        cause: Throwable? = null,
    ) : AppException(message, cause)

    /** Ma'lumot topilmadi (404). */
    class NotFound(
        message: String = "Ma'lumot topilmadi.",
        cause: Throwable? = null,
    ) : AppException(message, cause)

    /** Server xatosi (5xx) yoki tushunarsiz javob. */
    class Server(
        val code: Int? = null,
        message: String = SERVER_ERROR_MESSAGE,
        cause: Throwable? = null,
    ) : AppException(message, cause)

    /**
     * Chegara to'ldi (429) — `RATE_LIMITED`, `LISTING_LIMIT_REACHED` va shu kabilar
     * (`DISCOUNTS_BUSINESS_API_RESPONSE.md` §4: 5 biznes/foydalanuvchi, 100 faol e'lon/biznes,
     * 50 `submit`/kun, 100 rasm/soat).
     *
     * Nega [Validation] emas: 429 — foydalanuvchi **kiritgan ma'lumot** haqida emas. Validatsiya
     * deb qabul qilinsa forma maydonlarni qizartirib, "nimani tuzatay?" degan savol qoldirardi;
     * bu yerda esa tuzatadigan narsa yo'q, kutish yoki eskisini o'chirish kerak.
     *
     * [code] — backend bergan mashina o'qiydigan kalit (`error.code`); ekran shunga qarab aniq
     * maslahat beradi. [userMessage] — backendning o'zbekcha matni (u har doim keladi).
     */
    class LimitReached(
        val code: String?,
        message: String,
        cause: Throwable? = null,
    ) : AppException(message, cause)

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

    companion object {
        /**
         * Backend xato matnini bermaganда ko'rsatiladigan yagona zaxira matn.
         *
         * Qoida: **har qanday xatoda backendning o'z matni ko'rsatiladi** (u har doim keladi va
         * o'zbekcha) — status bo'yicha o'ylab topilgan matn emas. Matn kelmasa demak javob
         * kutilmagan shaklda: buni foydalanuvchiga "server xatosi" deb aytamiz, chunki
         * uning tuzatadigan narsasi yo'q.
         */
        const val SERVER_ERROR_MESSAGE = "Serverда xatolik. Birozdan so'ng qayta urining."
    }
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
        msg.containsAny("permission", "denied", "permission_denied") -> AppException.PermissionDenied(cause = this)
        msg.containsAny("unauthenticated", "unauthorized", "not authenticated", "sign in") ->
            AppException.Unauthorized(cause = this)
        msg.containsAny("not found", "no document", "not_found") -> AppException.NotFound(cause = this)
        msg.containsAny("timeout", "timed out", "deadline") -> AppException.Timeout(this)
        msg.containsAny("unavailable", "network", "host", "connection", "internet", "offline", "resolve") ->
            AppException.NoInternet(this)
        msg.containsAny("internal", "server", "unknown error") -> AppException.Server(cause = this)
        // Xom `message` FOYDALANUVCHIGA CHIQMAYDI — u faqat `cause` da qoladi (log uchun).
        // Ktor istisnosining matni butun so'rov va javob tanasini o'z ichiga oladi
        // ("Client request(POST .../business) invalid: 429 ... Text: {...}") va ilgari shu
        // holicha ekranga chiqardi. Backendning o'z matni bu yo'ldan kelmaydi — u javob
        // tanasidan `toAppExceptionWithFields`/`parseErrorEnvelope` orqali olinadi.
        else -> AppException.Unknown(cause = this)
    }
}

private fun String.containsAny(vararg needles: String): Boolean =
    needles.any { this.contains(it) }
