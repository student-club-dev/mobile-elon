package dev.core.network.media

import dev.core.network.NetworkConfig
import dev.core.network.generated.model.MediaUploadResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

/**
 * Rasm nima uchun yuklanayotgani — backend `POST /v1/media/upload` faqat shu uchtasini qabul qiladi.
 *
 * Profil rasmi uchun alohida tur yo'q, shuning uchun eng yaqini — [LOGO].
 */
enum class MediaPurpose { LOGO, COVER, LISTING }

/**
 * Rasm yuklash (`POST /v1/media/upload`) — **qo'lda** yozilgan, generatsiya qilingan
 * `MediaApi.upload` ishlatilmaydi.
 *
 * Sababi: generator multipart qismini `formData { append("file", file) }` deb quradi va qismga
 * sarlavha qo'shmaydi. Natijada so'rov shunday ketardi:
 *
 * ```
 * Content-Disposition: form-data; name=file      // ← filename YO'Q
 * ```
 *
 * NestJS'ning `FileInterceptor`i (multer) esa qismni **faqat `filename` bo'lganda** fayl deb
 * qabul qiladi; aks holda uni oddiy matn maydoniga qo'yadi va `file` bo'sh bo'lib qoladi.
 * Shuning uchun bu yerda qism `Content-Disposition: filename` va `Content-Type` bilan quriladi.
 *
 * ⚠️ Spec qayta generatsiya qilinса ham shu klass qoladi — `MediaUploadRequestTest` buni
 * ushlab turadi.
 */
class MediaUploader(
    private val client: HttpClient,
    private val config: NetworkConfig,
) {

    suspend fun upload(
        bytes: ByteArray,
        fileName: String,
        purpose: MediaPurpose,
    ): MediaUploadResponseDto = client.post(config.baseUrl + PATH) {
        setBody(
            MultiPartFormDataContent(
                formData {
                    append(
                        key = "file",
                        value = bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, mimeTypeOf(fileName))
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        },
                    )
                    append("purpose", purpose.name)
                },
            ),
        )
    }.body()

    private companion object {
        const val PATH = "media/upload"
    }
}

/** Fayl kengaytmasidan MIME turi — backend JPEG/PNG/WebP ni qabul qiladi. */
internal fun mimeTypeOf(fileName: String): String = when (fileName.substringAfterLast('.', "").lowercase()) {
    "png" -> "image/png"
    "webp" -> "image/webp"
    else -> "image/jpeg"
}
