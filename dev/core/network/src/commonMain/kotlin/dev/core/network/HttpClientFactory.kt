package dev.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import dev.core.network.response.EnvelopeUnwrapPlugin
import kotlinx.serialization.json.Json

data class NetworkConfig(
    val baseUrl: String,
    val enableLogging: Boolean = true,
)

val appJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
}

/** Har ikkala platforma uchun yagona, sozlangan Ktor klienti. */
fun createHttpClient(
    config: NetworkConfig,
    tokenProvider: suspend () -> String? = { null },
): HttpClient = platformHttpClient {
    expectSuccess = true

    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MS
        connectTimeoutMillis = CONNECT_TIMEOUT_MS
        socketTimeoutMillis = SOCKET_TIMEOUT_MS
    }

    // BaseResponse konvertini shaffof ochadi — ContentNegotiation'DAN OLDIN o'rnatiladi,
    // shunda raw JSON'ni birinchi bo'lib shu ushlaydi (aks holda konvert bo'sh DTO'ga aylanadi).
    install(EnvelopeUnwrapPlugin)

    install(ContentNegotiation) { json(appJson) }

    if (config.enableLogging) {
        install(Logging) { level = LogLevel.HEADERS }
    }

    // Har so'rovga joriy foydalanuvchi tokeni (Firebase ID token) qo'shiladi.
    // tokenProvider null qaytarsa (sessiya yo'q) — so'rov tokensiz ketadi.
    install(Auth) {
        bearer {
            loadTokens {
                tokenProvider()?.let { BearerTokens(accessToken = it, refreshToken = "") }
            }
            // 401 kelganda tokenni qayta o'qiydi (Firebase ID token muddati o'tган bo'lsa yangilanadi).
            refreshTokens {
                tokenProvider()?.let { BearerTokens(accessToken = it, refreshToken = "") }
            }
        }
    }

    install(DefaultRequest)
    defaultRequest {
        url(config.baseUrl)
        contentType(ContentType.Application.Json)
    }
}

/**
 * Tashqi (uchinchi tomon) xizmatlar uchun klient — masalan OpenStreetMap Nominatim.
 *
 * Ilovaning umumiy klientidan farqi: **Bearer token qo'shmaydi** va bazaviy manzili yo'q.
 * Firebase tokenini begona serverga yuborish mumkin emas, shuning uchun alohida klient.
 */
fun createPublicHttpClient(): HttpClient = platformHttpClient {
    expectSuccess = true
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MS
        connectTimeoutMillis = CONNECT_TIMEOUT_MS
        socketTimeoutMillis = SOCKET_TIMEOUT_MS
    }
    install(ContentNegotiation) { json(appJson) }
}

/**
 * Tarmoq kutish chegaralari.
 *
 * Ilgari umuman belgilanmagan edi va engine standartlari ishlardi — Android/OkHttp'da ~10 s,
 * iOS/Darwin'da esa **60 s**. Server javob bermasa foydalanuvchi shuncha vaqt kutардi; bu ayniqsa
 * xaritadan joy tanlashda seziladi, chunki u yerda zaxira geokoder birinchisi tugagachgina
 * boshlanadi.
 */
private const val CONNECT_TIMEOUT_MS = 8_000L
private const val REQUEST_TIMEOUT_MS = 15_000L
private const val SOCKET_TIMEOUT_MS = 15_000L

/** Platformaga xos HTTP engine (Android: OkHttp, iOS: Darwin). */
expect fun platformHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
