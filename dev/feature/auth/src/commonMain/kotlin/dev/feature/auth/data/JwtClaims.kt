package dev.feature.auth.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Access-token (JWT) ning payload qismini o'qiydi.
 *
 * Bizga faqat `sub` — foydalanuvchi id'si kerak: backend uni alohida endpoint bilan bermaydi,
 * biznes/e'lon egaligi esa aynan shu id bo'yicha tekshiriladi (`BusinessDto.ownerUserId`).
 *
 * Bu **imzoni tekshirmaydi** va tekshirishi ham shart emas: tokenni serverning o'zi bergan,
 * klient undan faqat ma'lumot o'qiydi. Har qanday xatoда `null` qaytadi.
 */
object JwtClaims {

    @OptIn(ExperimentalEncodingApi::class)
    fun subject(token: String): String? = runCatching {
        val payload = token.split('.').getOrNull(1) ?: return null
        // JWT base64url'ni to'ldiruvchi (`=`) belgilarsiz yozadi — qo'shib qo'yamiz.
        val padded = payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '=')
        val json = Base64.UrlSafe.decode(padded).decodeToString()
        Json.parseToJsonElement(json).jsonObject["sub"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
    }.getOrNull()
}
