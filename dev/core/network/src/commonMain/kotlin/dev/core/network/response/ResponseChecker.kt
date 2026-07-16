package dev.core.network.response

import dev.core.common.error.AppException

/**
 * **Checker** — javob konvertini bir joyда tekshiradi (IYM-business naqshi).
 *
 * Muvaffaqiyat bo'lsa [BaseResponse.payload] ni qaytaradi; aks holda status/xato bo'yicha
 * typed [AppException] tashlaydi. Shu sabab har bir data-source'da `if (response.status...)`
 * takrorlanmaydi — bitta checker hammasini hal qiladi.
 */
object ResponseChecker {

    fun <T> check(response: BaseResponse<T>): T {
        if (response.isSuccessful) {
            return response.payload
                ?: throw AppException.Server(response.status) // 2xx, lekin tana bo'sh
        }
        throw response.toAppException()
    }
}

/** Qisqartma: `response.check()` — muvaffaqiyatli payload yoki [AppException] tashlaydi. */
fun <T> BaseResponse<T>.check(): T = ResponseChecker.check(this)

/** Konvertdagi status/xatoni typed [AppException] ga aylantiradi. */
fun BaseResponse<*>.toAppException(): AppException {
    val text = error?.message ?: message
    return when (val s = status) {
        401 -> AppException.Unauthorized()
        403 -> AppException.PermissionDenied()
        404 -> AppException.NotFound()
        408 -> AppException.Timeout()
        null -> if (text != null) AppException.Validation(text) else AppException.Unknown()
        in 400..499 -> AppException.Validation(text ?: "So'rov noto'g'ri.")
        in 500..599 -> AppException.Server(s)
        else -> if (text != null) AppException.Validation(text) else AppException.Unknown()
    }
}
