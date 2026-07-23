package dev.core.network.response

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.core.common.error.fieldErrors
import dev.core.network.appJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 422 validatsiya javobi — `error.fields` formagacha yetib borishi kerak.
 *
 * Non-2xx javoblar [EnvelopeUnwrapPlugin] gacha yetmaydi, shuning uchun ularning tanasi
 * [parseErrorEnvelope] orqali xom matndan o'qiladi — aynan shu yo'l tekshiriladi.
 */
class ValidationFieldsTest {

    @Test
    fun `422 tanasidagi maydon xatolari Validation fields ga tushadi`() {
        val body = """
            {
              "success": false,
              "status": 422,
              "code": "VALIDATION_ERROR",
              "error": {
                "code": "VALIDATION_ERROR",
                "message": "Ma'lumotlarni tekshiring",
                "fields": {
                  "phoneNumber": "Noto'g'ri format",
                  "firstName": "Bo'sh bo'lishi mumkin emas"
                }
              }
            }
        """.trimIndent()

        val e = parseErrorEnvelope(body, httpStatus = 422)

        val validation = assertIs<AppException.Validation>(e)
        assertEquals("Ma'lumotlarni tekshiring", validation.userMessage)
        assertEquals(
            mapOf(
                "phoneNumber" to "Noto'g'ri format",
                "firstName" to "Bo'sh bo'lishi mumkin emas",
            ),
            validation.fields,
        )
        // Chaqiruvchi `is Validation` tekshiruvini takrorlamasligi uchun.
        assertEquals(validation.fields, validation.fieldErrors)
    }

    @Test
    fun `fields siz 4xx ham Validation, lekin maydonlarsiz`() {
        val body = """{"success": false, "status": 400, "error": {"message": "So'rov noto'g'ri"}}"""

        val validation = assertIs<AppException.Validation>(parseErrorEnvelope(body, httpStatus = 400))

        assertEquals("So'rov noto'g'ri", validation.userMessage)
        assertTrue(validation.fields.isEmpty())
    }

    @Test
    fun `konvert bo'lmagan tana null qaytaradi — chaqiruvchi status bo'yicha zaxiraga o'tadi`() {
        assertNull(parseErrorEnvelope("", httpStatus = 422))
        assertNull(parseErrorEnvelope("<html>502 Bad Gateway</html>", httpStatus = 502))
        assertNull(parseErrorEnvelope("""{"detail": "boshqa shakl"}""", httpStatus = 422))
    }

    @Test
    fun `200 javob ichidagi success=false ham maydon xatolarini saqlaydi`() {
        val envelope = BaseResponse<String>(
            success = false,
            error = ApiError(message = "Xato", fields = mapOf("email" to "Band")),
        )

        val validation = assertIs<AppException.Validation>(envelope.toAppException(httpStatus = 200))

        assertEquals(mapOf("email" to "Band"), validation.fields)
    }

    /**
     * To'liq yo'l: haqiqiy Ktor klienti (`expectSuccess = true` + [EnvelopeUnwrapPlugin]) 422
     * javob olganda `safeCall` maydon xatolari bilan `Resource.Error` qaytarishi kerak.
     *
     * Bu — eng nozik joy: 422 tanasi plagin'gача yetmaydi, uni `safeCall` istisnodan o'qiydi.
     */
    @Test
    fun `422 HTTP javobi safeCall orqali maydon xatolari bilan qaytadi`() = runTest {
        val engine = MockEngine {
            respond(
                content = """
                    {"success":false,"status":422,
                     "error":{"message":"Tekshiring","fields":{"phoneNumber":"Noto'g'ri format"}}}
                """.trimIndent(),
                status = HttpStatusCode.UnprocessableEntity,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        // Ilova klientining xato-ishlash uchun muhim qismlari (`createHttpClient` bilan bir xil).
        val client = HttpClient(engine) {
            expectSuccess = true
            install(EnvelopeUnwrapPlugin)
            install(ContentNegotiation) { json(appJson) }
            defaultRequest {
                url("https://example.test/v1/")
                contentType(ContentType.Application.Json)
            }
        }

        val res = safeCall { client.put("profile/me").body<ProbeDto>() }

        val error = assertIs<Resource.Error>(res)
        val validation = assertIs<AppException.Validation>(error.error)
        assertEquals(mapOf("phoneNumber" to "Noto'g'ri format"), validation.fields)
        assertEquals("Tekshiring", error.message)
    }

    /** So'ralgan DTO — mazmuni muhim emas, 422 da u hech qachon deserializatsiya qilinmaydi. */
    @Serializable
    private data class ProbeDto(val id: String = "")
}
