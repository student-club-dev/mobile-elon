package dev.core.network.response

import dev.core.common.error.AppException
import dev.core.network.appJson
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

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
    // Ko'rsatiladigan matn — DOIM backenddan. Kelmasa (javob kutilmagan shaklda) zaxira
    // sifatida "server xatosi": foydalanuvchi tuzata oladigan narsa yo'q.
    val text = (error?.message ?: message)?.takeIf { it.isNotBlank() }
    val shown = text ?: AppException.SERVER_ERROR_MESSAGE
    val code = error?.code
    val fields = error?.fields.orEmpty()

    // Backend `status` ni konvert ichida beradi; bo'lmasa HTTP kodiga tayanamiz. Ikkalasi
    // ham yo'q bo'lsa — pastdagi `null` shoxi (maydon xatolari bo'lsa validatsiya).
    return when (val s = status ?: httpStatus) {
        // 401 — "sessiya tugadi" degani HAR DOIM emas: login'da u "login yoki parol xato"
        // (`INVALID_CREDENTIALS`). Sessiya kodlarida o'z matnimiz aniqroq ("qaytadan kiring"),
        // qolganda backendniki ko'rsatiladi — aks holda parolni xato tergan foydalanuvchi
        // ma'nosiz maslahat olardi.
        401 -> AppException.Unauthorized(
            code = code,
            message = if (code in AppException.Unauthorized.SESSION_EXPIRED_CODES || text == null) {
                AppException.Unauthorized.SESSION_EXPIRED_MESSAGE
            } else {
                text
            },
        )
        403 -> AppException.PermissionDenied(code, shown)
        404 -> AppException.NotFound(shown)
        408 -> AppException.Timeout()
        // 429 — chegara (limit/rate). Validatsiyadan ataylab ajratilgan: tuzatiladigan
        // maydon yo'q, shuning uchun forma uni maydon xatosi sifatida ko'rsatmasligi kerak.
        429 -> AppException.LimitReached(code, shown)
        // Qolgan barcha 4xx — 409 (band/ziddiyat), 410 (muddati o'tgan), 413 (fayl katta),
        // 422 (validatsiya) va h.k. Ular bitta turda: matn ko'rinadi, `fields` bo'lsa
        // maydonlar ostiga tarqaladi.
        in 400..499 -> AppException.Validation(shown, fields)
        // 5xx (503 — xizmat vaqtincha yo'q) — backend matni bo'lsa aynan u ko'rsatiladi.
        in 500..599 -> if (fields.isNotEmpty()) {
            AppException.Validation(shown, fields)
        } else {
            AppException.Server(s, shown)
        }
        // Status umuman yo'q yoki 2xx (konvertda `success=false`) — matn/maydonga qarab.
        else -> if (text != null || fields.isNotEmpty()) {
            AppException.Validation(shown, fields)
        } else {
            AppException.Server(s)
        }
    }
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
    val root = runCatching { appJson.parseToJsonElement(body) }.getOrNull() as? JsonObject ?: return null

    // 1. Standart konvert.
    val envelope = runCatching {
        appJson.decodeFromJsonElement(BaseResponse.serializer(JsonElement.serializer()), root)
    }.getOrNull()
    if (envelope != null && (envelope.error != null || envelope.message != null)) {
        return envelope.toAppException(httpStatus)
    }

    // 2. Konvertsiz xato tanasi. Hamma xato ilovaning o'z filtridan o'tavermaydi: token
    // qorovuli (401), tana hajmi chegarasi (413) va framework validatsiyasi ko'pincha
    // `{"statusCode":…, "message":…, "error":"Bad Request"}` shaklida keladi — unda `error`
    // OBYEKT emas, SATR bo'ladi va konvert deserializatsiyasi butunlay yiqiladi. Ilgari bunday
    // javoblarning matni yo'qolib, foydalanuvchi HTTP status bo'yicha umumiy xabar ko'rardi.
    val text = root.errorText() ?: return null
    val status = root["statusCode"]?.jsonPrimitiveOrNull()?.content?.toIntOrNull()
    return BaseResponse<JsonElement>(status = status, error = ApiError(message = text))
        .toAppException(httpStatus)
}

/**
 * Konvertsiz tanadan foydalanuvchiga ko'rsatsa bo'ladigan matnni topadi.
 *
 * `message` massiv ham bo'lishi mumkin (framework validatsiyasi har maydon uchun bitta qator
 * beradi) — ular bitta matnga birlashtiriladi.
 */
private fun JsonObject.errorText(): String? {
    val message = this["message"]
    val text = when {
        message is JsonArray -> message.mapNotNull { it.jsonPrimitiveOrNull()?.content }
            .filter { it.isNotBlank() }
            .joinToString(". ")
        message != null -> message.jsonPrimitiveOrNull()?.content
        // `error` — konvertda obyekt, konvertsiz javobda esa oddiy satr.
        else -> this["error"]?.jsonPrimitiveOrNull()?.content
    }
    return text?.takeIf { it.isNotBlank() }
}

private fun JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? = this as? JsonPrimitive
