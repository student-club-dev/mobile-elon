package dev.core.network

import dev.core.common.auth.AuthTokens
import dev.core.common.auth.TokenStore
import dev.core.common.deviceName
import dev.core.common.platformName
import dev.core.network.response.EnvelopeUnwrapPlugin
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Tarmoq sozlamalari.
 *
 * [baseUrl] oxirida **slash bilan** `/v1/` turadi — Ktor nisbiy yo'llarni shunga nisbatan hal
 * qiladi va generatsiya qilingan klient ham shu bazaga yo'lni qo'shadi.
 *
 * [refreshPath] — token yangilash endpoint'i (bazaga nisbatan). ElonUz'da talaba va biznes uchun
 * alohida: `auth/student/refresh` / `auth/business/refresh`. Bu ilova — biznes ilovasi.
 */
data class NetworkConfig(
    val baseUrl: String,
    val refreshPath: String = "auth/business/refresh",
    val enableLogging: Boolean = true,
)

val appJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
}

/**
 * Har ikkala platforma uchun yagona, sozlangan Ktor klienti.
 *
 * Sessiya to'liq **backend tokenlariga** tayanadi ([TokenStore]):
 * - har so'rovga `Authorization: Bearer <accessToken>` qo'shiladi;
 * - 401 (`TOKEN_EXPIRED`) kelganda `refreshToken` bilan yangi juftlik olinadi va so'rov
 *   avtomatik takrorlanadi;
 * - refresh ham rad etilsa — sessiya tozalanadi (foydalanuvchi qayta kirishi kerak).
 */
fun createHttpClient(
    config: NetworkConfig,
    tokenStore: TokenStore,
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

    install(Auth) {
        bearer {
            loadTokens {
                tokenStore.tokens()?.let { BearerTokens(it.accessToken, it.refreshToken) }
            }
            // 401 kelganda chaqiriladi. `markAsRefreshTokenRequest` bo'lmasa yangilash
            // so'rovining o'zi ham 401'ga tushib cheksiz sikl hosil qilardi.
            refreshTokens {
                val current = tokenStore.tokens()?.refreshToken ?: return@refreshTokens null
                val renewed = runCatching {
                    client.post(config.baseUrl + config.refreshPath) {
                        markAsRefreshTokenRequest()
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequest(refreshToken = current))
                    }.body<TokensResponse>()
                }.getOrNull()

                if (renewed == null) {
                    // Refresh token yaroqsiz/muddati o'tgan — sessiyani tozalaymiz, aks holda
                    // ilova har so'rovda yaroqsiz token bilan urinaverardi.
                    tokenStore.clear()
                    null
                } else {
                    tokenStore.save(AuthTokens(renewed.accessToken, renewed.refreshToken))
                    BearerTokens(renewed.accessToken, renewed.refreshToken)
                }
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
 * Ktor `Auth` plagini tokenni **xotirada keshlaydi** — kirish/chiqishdan keyin keshni majburan
 * tozalash kerak, aks holda klient eski (yoki umuman yo'q) token bilan ishlayveradi.
 */
fun HttpClient.resetAuthTokenCache() {
    authProvider<BearerAuthProvider>()?.clearToken()
}

/** Token yangilash so'rovi/javobi — generatsiya qilingan modelga bog'lanmaslik uchun local. */
@Serializable
private data class RefreshRequest(
    val refreshToken: String,
    val deviceName: String = dev.core.common.deviceName,
    val platform: String = platformName,
)

@Serializable
private data class TokensResponse(
    val accessToken: String,
    val refreshToken: String,
)

/**
 * Tashqi (uchinchi tomon) xizmatlar uchun klient — masalan OpenStreetMap Nominatim.
 *
 * Ilovaning umumiy klientidan farqi: **Bearer token qo'shmaydi** va bazaviy manzili yo'q.
 * Sessiya tokenini begona serverga yuborish mumkin emas, shuning uchun alohida klient.
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
 * xaritadan joy tanlashda seziladi, chunki u yerда zaxira geokoder birinchisi tugagachgina
 * boshlanadi.
 */
private const val CONNECT_TIMEOUT_MS = 8_000L
private const val REQUEST_TIMEOUT_MS = 15_000L
private const val SOCKET_TIMEOUT_MS = 15_000L

/** Platformaga xos HTTP engine (Android: OkHttp, iOS: Darwin). */
expect fun platformHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
