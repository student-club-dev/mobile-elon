package dev.core.network.response

import dev.core.common.error.AppException
import dev.core.network.appJson
import kotlinx.serialization.json.JsonElement

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

/**
 * Konvertdagi status/xatoni typed [AppException] ga aylantiradi.
 *
 * [ApiError.fields] bo'lsa u [AppException.Validation.fields] ga o'tadi — shu sabab 422
 * validatsiya xatolari formagacha yetib boradi va maydon ostida ko'rsatiladi.
 *
 * [httpStatus] — konvertda `status` bo'lmaganda ishlatiladigan zaxira (HTTP javob kodi).
 */
fun BaseResponse<*>.toAppException(httpStatus: Int? = null): AppException {
    val text = error?.message ?: message
    val fields = error?.fields.orEmpty()
    // Maydon xatolari bor bo'lsa status qanday bo'lishidan qat'i nazar bu — validatsiya.
    return when (val s = status ?: httpStatus) {
        401 -> AppException.Unauthorized()
        403 -> AppException.PermissionDenied()
        404 -> AppException.NotFound()
        408 -> AppException.Timeout()
        // 429 — chegara (limit/rate). Validatsiyadan ataylab ajratilgan: tuzatiladigan
        // maydon yo'q, shuning uchun forma uni maydon xatosi sifatida ko'rsatmasligi kerak.
        429 -> AppException.LimitReached(
            code = error?.code,
            message = text ?: "Chegara to'ldi. Birozdan so'ng qayta urining.",
        )
        null -> validationOrUnknown(text, fields)
        in 400..499 -> AppException.Validation(text ?: "So'rov noto'g'ri.", fields)
        in 500..599 -> if (fields.isNotEmpty()) {
            AppException.Validation(text ?: "So'rov noto'g'ri.", fields)
        } else {
            AppException.Server(s)
        }
        else -> validationOrUnknown(text, fields)
    }
}

private fun validationOrUnknown(text: String?, fields: Map<String, String>): AppException =
    if (text != null || fields.isNotEmpty()) {
        AppException.Validation(text ?: "So'rov noto'g'ri.", fields)
    } else {
        AppException.Unknown()
    }

/**
 * Xato javobining **tanasidan** typed xato quradi.
 *
 * Non-2xx javoblar [EnvelopeUnwrapPlugin] gacha yetmaydi (Ktor `expectSuccess` ularni
 * `ResponseException` bilan tashlaydi), shuning uchun 422 ning `error.fields` ini shu yerda —
 * xom matndan — o'qiymiz. Tana konvert bo'lmasa yoki umuman JSON bo'lmasa `null` qaytadi
 * va chaqiruvchi HTTP status bo'yicha zaxira xatoga o'tadi.
 */
fun parseErrorEnvelope(body: String, httpStatus: Int? = null): AppException? {
    if (body.isBlank()) return null
    val envelope = runCatching {
        appJson.decodeFromString(BaseResponse.serializer(JsonElement.serializer()), body)
    }.getOrNull() ?: return null
    // Konvert emas (masalan oddiy `{"detail": ...}`) — hech qanday foydali ma'lumot yo'q.
    if (envelope.error == null && envelope.message == null) return null
    return envelope.toAppException(httpStatus)
}
