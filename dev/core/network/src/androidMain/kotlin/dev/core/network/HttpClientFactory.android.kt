package dev.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Interceptor

/**
 * Android-only OkHttp interceptor'lar ro'yxati (masalan **Chucker** — Debug HTTP inspektori).
 *
 * Ilova ishga tushganда (`ElonUzApp.onCreate`) bu yerga qo'shiladi — klient qurilishidan
 * OLDIN. Network moduli Chucker'ga bevosita bog'liq emas: faqat `okhttp3.Interceptor`
 * turini biladi, aniq interceptor esa app moduldan (Context bilan) keladi.
 */
object OkHttpInterceptors {
    val interceptors: MutableList<Interceptor> = mutableListOf()
}

actual fun platformHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(OkHttp) {
        config()
        engine {
            // Ro'yxatdagi barcha interceptor'lar (Chucker va h.k.) OkHttp'ga qo'shiladi.
            OkHttpInterceptors.interceptors.forEach { addInterceptor(it) }
        }
    }
