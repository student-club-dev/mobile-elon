package dev.core.network.response

import dev.core.network.appJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.test.runTest
import kotlinx.io.readByteArray
import kotlin.test.Test
import kotlin.test.assertContentEquals

/**
 * Rasm (binar) javob [EnvelopeUnwrapPlugin] dan **o'zgarmasdan** o'tishi kerak.
 *
 * Coil ilovaning umumiy Ktor klientidan foydalanadi (qarang `App.kt`), ya'ni har bir rasm
 * so'rovi ham shu plagindan o'tadi. Plagin javobni JSON deb o'qishga urinsa — JPEG baytlari
 * uchun bu xato bilan tugaydi va rasm hech qachon ko'rinmaydi.
 */
class BinaryResponseTest {

    /** JPEG sarlavhasi bilan boshlanadigan "rasm" — JSON emas. */
    private val jpegBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 1, 2, 3)

    private fun imageClient(): HttpClient = HttpClient(
        MockEngine {
            respond(
                content = ByteReadChannel(jpegBytes),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Image.JPEG.toString()),
            )
        },
    ) {
        install(EnvelopeUnwrapPlugin)
        install(ContentNegotiation) { json(appJson) }
    }

    @Test
    fun imageBytesPassThroughUnchanged() = runTest {
        val bytes: ByteArray = imageClient().get("https://cdn.test/uploads/LISTING/a.jpg").body()
        assertContentEquals(jpegBytes, bytes)
    }

    /** Coil aynan shu turni so'raydi (`bodyAsChannel()`). */
    @Test
    fun imageChannelPassesThroughUnchanged() = runTest {
        val channel: ByteReadChannel = imageClient().get("https://cdn.test/uploads/LISTING/a.jpg").body()
        assertContentEquals(jpegBytes, channel.readRemaining().readByteArray())
    }
}
