package dev.core.network

import dev.core.network.generated.api.MediaApi
import dev.core.network.media.MediaPurpose
import dev.core.network.media.MediaUploader
import dev.core.network.response.EnvelopeUnwrapPlugin
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.InputProvider
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `POST /v1/media/upload` chiquvchi so'rovi haqiqatan **fayl** yuboradimi.
 *
 * Backend (NestJS + multer) qismni fayl deb qabul qilishi uchun uning `Content-Disposition`
 * sarlavhasida `filename` bo'lishi SHART. Generatsiya qilingan klient uni qo'ymaydi, shuning
 * uchun yuklash qo'lda yozilgan [MediaUploader] orqali ketadi — bu test aynan shuni ushlab turadi
 * (spec qayta generatsiya qilinса ham buzilmasin).
 */
class MediaUploadRequestTest {

    private class Recorder {
        var body: String = ""
        var path: String = ""
    }

    private fun clientWith(recorder: Recorder): HttpClient {
        val engine = MockEngine { request ->
            recorder.path = request.url.encodedPath
            recorder.body = (request.body as OutgoingContent).readAllText()
            respond(
                content = """{"success":true,"status":200,"result":{"url":"https://cdn/x.jpg"}}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        // Haqiqiy klientdagi tartib: konvertni ochuvchi plagin ContentNegotiation'DAN OLDIN.
        return HttpClient(engine) {
            install(EnvelopeUnwrapPlugin)
            install(ContentNegotiation) { json(appJson) }
        }
    }

    @Test
    fun uploaderSendsFilePartWithFilenameAndContentType() = runTest {
        val recorder = Recorder()
        val uploader = MediaUploader(
            client = clientWith(recorder),
            config = NetworkConfig(baseUrl = "https://example.test/v1/"),
        )

        uploader.upload(byteArrayOf(1, 2, 3, 4), "logo.png", MediaPurpose.LOGO)

        assertEquals("/v1/media/upload", recorder.path)
        assertTrue(recorder.body.contains("filename=\"logo.png\""), "filename yo'q:\n${recorder.body}")
        assertTrue(recorder.body.contains("image/png"), "Content-Type yo'q:\n${recorder.body}")
        assertTrue(recorder.body.contains("LOGO"), "purpose yo'q:\n${recorder.body}")
    }

    @Test
    fun uploaderPicksMimeTypeFromExtension() = runTest {
        val recorder = Recorder()
        val uploader = MediaUploader(
            client = clientWith(recorder),
            config = NetworkConfig(baseUrl = "https://example.test/v1/"),
        )

        uploader.upload(byteArrayOf(9), "photo.jpg", MediaPurpose.LISTING)

        assertTrue(recorder.body.contains("image/jpeg"), "JPEG turi noto'g'ri:\n${recorder.body}")
    }

    /**
     * Generatsiya qilingan klient nega ishlatilmasligining **isboti**: uning qismida `filename`
     * yo'q. Test shu holat o'zgarganini (masalan generator yangilanganini) bildiradi — o'shanda
     * [MediaUploader] dan voz kechish mumkin bo'ladi.
     */
    @Test
    fun generatedApiStillOmitsFilename() = runTest {
        val recorder = Recorder()
        val bytes = byteArrayOf(1, 2, 3, 4)
        val part = InputProvider(bytes.size.toLong()) { Buffer().apply { write(bytes) } }

        MediaApi(baseUrl = "https://example.test/v1/", httpClient = clientWith(recorder))
            .upload(file = part, purpose = "LISTING")

        assertFalse(
            recorder.body.contains("filename="),
            "Generator endi `filename` qo'yayapti — MediaUploader'dan voz kechish mumkin:\n${recorder.body}",
        )
    }
}

/** Multipart tanasini matn sifatida o'qiydi (binar baytlar test uchun ahamiyatsiz). */
private suspend fun OutgoingContent.readAllText(): String = when (this) {
    is OutgoingContent.ByteArrayContent -> bytes().decodeToString()
    is OutgoingContent.WriteChannelContent -> {
        val channel = ByteChannel()
        writeTo(channel)
        channel.flushAndClose()
        channel.readRemaining().readByteArray().decodeToString()
    }
    is OutgoingContent.ReadChannelContent -> readFrom().readRemaining().readByteArray().decodeToString()
    else -> ""
}
