package dev.core.network.response

import dev.core.network.appJson
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

/**
 * **Envelope unwrap** — backend har javobni [BaseResponse] konvertida qaytaradi
 * (`{success,status,code,message,result,error}`), lekin OpenAPI'dan generatsiya qilingan klient
 * `.body()` bilan **xom DTO** kutadi. Bu plagin konvertni **shaffof ochadi**:
 *
 * - muvaffaqiyat → `result` (yoki `data`) so'ralgan DTO sifatida deserializatsiya qilinadi;
 * - konvertда xato (`success=false` / `error != null`) → typed [AppException] tashlanadi
 *   ([toAppException]) — data-source'lardagi `catch` bloklari uni tushunadi. `error.fields`
 *   bo'lsa u [AppException.Validation.fields] ga o'tadi va formagacha yetib boradi.
 *
 * Konvert bo'lmagan (xom) javob kelsa — butun tana DTO deb o'qiladi (o'tish davri uchun moslashuvchan).
 *
 * ⚠️ **ContentNegotiation'dan OLDIN o'rnatiladi** (`createHttpClient`da) — shunda raw JSON'ni
 * birinchi bo'lib shu plagin ushlaydi, aks holda ContentNegotiation konvertni bo'sh DTO'ga
 * aylantirib yuboradi (kalitlar mos kelmaydi).
 *
 * Non-2xx javoblar bu yergача yetmaydi — `expectSuccess=true` ularni body-transform'дан oldin
 * `ResponseException` bilan tashlaydi (masalan profil yo'q → HTTP 404). Ularning tanasi
 * `safeApiCall`/`safeCall` da — [toAppExceptionWithFields] orqali — o'qiladi.
 *
 * ⚠️ **Binar javoblar** (rasm, fayl) chetlab o'tiladi — qarang [RAW_BODY_TYPES].
 */
val EnvelopeUnwrapPlugin = createClientPlugin("EnvelopeUnwrap") {
    transformResponseBody { response, content, requestedType ->
        val kType = requestedType.kotlinType
        // Unit (masalan void DELETE) — tanani deserializatsiya qilishning hojati yo'q.
        if (kType == null || requestedType.type == Unit::class) return@transformResponseBody Unit
        // Xom tana so'ralgan (rasm baytlari) — `null` qaytarsak Ktor tanaga tegmasdan o'tkazadi.
        if (requestedType.type in RAW_BODY_TYPES) return@transformResponseBody null

        val text = content.readRemaining().readByteArray().decodeToString()
        if (text.isBlank()) return@transformResponseBody Unit

        val serializer = appJson.serializersModule.serializer(kType)
        val root = appJson.parseToJsonElement(text)

        // Konvert belgisi — backend har doim `success` maydonini qo'shadi. Xom DTO'да u yo'q.
        val payload: JsonElement = if (root is JsonObject && "success" in root) {
            val envelope = appJson.decodeFromJsonElement(
                BaseResponse.serializer(JsonElement.serializer()),
                root,
            )
            if (!envelope.isSuccessful) throw envelope.toAppException(response.status.value)
            envelope.payload ?: JsonNull
        } else {
            root
        }

        appJson.decodeFromJsonElement(serializer, payload)
    }
}

/**
 * JSON konverti **kutilmaydigan** tana turlari — bularni Ktor'ning o'z transformer'i beradi.
 *
 * Rasm yuklovchi (Coil) ilovaning umumiy klientidan foydalanadi va tanani [ByteReadChannel]
 * sifatida so'raydi. Plagin ularni ham JSON deb o'qib ko'rgan edi: JPEG baytlari matnga
 * o'girilib `JsonDecodingException` bilan tugardi, ya'ni **hech bir rasm ko'rinmasdi**
 * (`GET /uploads/LISTING/….jpg` xatoga tushardi). Qarang `BinaryResponseTest`.
 */
private val RAW_BODY_TYPES = setOf(
    ByteReadChannel::class,
    ByteArray::class,
    Source::class,
    HttpStatusCode::class,
)
