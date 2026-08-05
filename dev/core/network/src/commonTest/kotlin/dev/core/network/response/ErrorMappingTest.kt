package dev.core.network.response

import dev.core.common.error.AppException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Xato javoblarini foydalanuvchi ko'radigan matnga aylantirish qoidalari.
 *
 * Asosiy qoida: **matn har doim backenddan**. Ilova status bo'yicha matn "o'ylab topmaydi" —
 * bitta 401 login'da "parol xato", boshqa joyda "sessiya tugadi" bo'lishi mumkin, buni faqat
 * backend biladi. Matn umuman kelmasa — "server xatosi".
 */
class ErrorMappingTest {

    @Test
    fun `401 INVALID_CREDENTIALS — backend matni ko'rsatiladi, sessiya tugagan emas`() {
        val body = """
            {"success":false,"status":401,
             "message":"Login yoki parol xato",
             "error":{"code":"INVALID_CREDENTIALS","message":"Login yoki parol xato"}}
        """.trimIndent()

        val e = assertIs<AppException.Unauthorized>(parseErrorEnvelope(body, httpStatus = 401))

        assertEquals("Login yoki parol xato", e.userMessage)
        assertFalse(e.sessionExpired)
    }

    @Test
    fun `401 TOKEN_EXPIRED — qaytadan kirish xabari`() {
        val body = """
            {"success":false,"status":401,
             "error":{"code":"TOKEN_EXPIRED","message":"Token expired"}}
        """.trimIndent()

        val e = assertIs<AppException.Unauthorized>(parseErrorEnvelope(body, httpStatus = 401))

        assertEquals(AppException.Unauthorized.SESSION_EXPIRED_MESSAGE, e.userMessage)
        assertTrue(e.sessionExpired)
    }

    @Test
    fun `403 — backend sababi ko'rsatiladi, umumiy matn emas`() {
        val body = """
            {"success":false,"status":403,
             "error":{"code":"PHONE_NOT_VERIFIED","message":"Avval telefon raqamingizni tasdiqlang"}}
        """.trimIndent()

        val e = assertIs<AppException.PermissionDenied>(parseErrorEnvelope(body, httpStatus = 403))

        assertEquals("Avval telefon raqamingizni tasdiqlang", e.userMessage)
        assertEquals("PHONE_NOT_VERIFIED", e.code)
    }

    @Test
    fun `409 va 410 ham matni bilan chiqadi`() {
        val conflict = parseErrorEnvelope(
            """{"success":false,"status":409,"error":{"message":"Bu raqam band"}}""",
            httpStatus = 409,
        )
        assertEquals("Bu raqam band", conflict?.userMessage)

        val gone = parseErrorEnvelope(
            """{"success":false,"status":410,"error":{"message":"Kod muddati tugagan"}}""",
            httpStatus = 410,
        )
        assertEquals("Kod muddati tugagan", gone?.userMessage)
    }

    @Test
    fun `503 — backend matni bo'lsa aynan u chiqadi`() {
        val e = assertIs<AppException.Server>(
            parseErrorEnvelope(
                """{"success":false,"status":503,"error":{"message":"Xizmat vaqtincha ishlamayapti"}}""",
                httpStatus = 503,
            ),
        )

        assertEquals("Xizmat vaqtincha ishlamayapti", e.userMessage)
        assertEquals(503, e.code)
    }

    @Test
    fun `matnsiz xato — server xatosi deb ko'rsatiladi`() {
        val e = BaseResponse<String>(success = false, status = 500).toAppException(httpStatus = 500)

        assertEquals(AppException.SERVER_ERROR_MESSAGE, e.userMessage)
    }

    /**
     * Hamma xato ilovaning konvert filtridan o'tavermaydi: qorovul (401), tana hajmi chegarasi
     * (413) va framework validatsiyasi o'z shaklini beradi — unda `error` OBYEKT emas, SATR.
     * Ilgari bunday tana deserializatsiyada yiqilib, matn butunlay yo'qolardi.
     */
    @Test
    fun `konvertsiz xato tanasi ham o'qiladi`() {
        val body = """{"statusCode":413,"message":"Rasm hajmi 5 MB dan oshmasin","error":"Payload Too Large"}"""

        val e = assertIs<AppException.Validation>(parseErrorEnvelope(body, httpStatus = 413))

        assertEquals("Rasm hajmi 5 MB dan oshmasin", e.userMessage)
    }

    @Test
    fun `konvertsiz javobda message ro'yxat bo'lsa birlashtiriladi`() {
        val body = """{"statusCode":400,"message":["Telefon noto'g'ri","Parol qisqa"],"error":"Bad Request"}"""

        val e = assertIs<AppException.Validation>(parseErrorEnvelope(body, httpStatus = 400))

        assertEquals("Telefon noto'g'ri. Parol qisqa", e.userMessage)
    }

    @Test
    fun `tana o'qilmaganda 4xx server xatosi bo'ladi, inglizcha HTTP izohi emas`() {
        val e = io.ktor.http.HttpStatusCode.BadRequest.toAppException()

        assertEquals(AppException.SERVER_ERROR_MESSAGE, e.userMessage)
    }

    @Test
    fun `201 kabi boshqa 2xx javoblar muvaffaqiyat deb qabul qilinadi`() {
        assertTrue(BaseResponse(success = true, status = 201, result = "ok").isSuccessful)
        assertTrue(BaseResponse(success = true, status = 204, result = "ok").isSuccessful)
        // 2xx bo'lsa ham konvertда xato bo'lsa — muvaffaqiyat emas.
        assertFalse(
            BaseResponse(success = true, status = 200, result = "ok", error = ApiError(message = "Xato"))
                .isSuccessful,
        )
    }
}
