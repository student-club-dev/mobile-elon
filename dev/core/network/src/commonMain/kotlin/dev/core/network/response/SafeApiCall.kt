package dev.core.network.response

import dev.core.common.Resource
import dev.core.common.errorOf
import dev.core.common.error.AppException
import dev.core.common.error.toAppException
import dev.core.common.network.NetworkConnectivity
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException

/**
 * Barcha API so'rovlari uchun **yagona xavfsiz o'ram** (IYM-business naqshi).
 *
 * Bitta joyда: internet tekshiruvi → so'rov → konvert [check] → barcha istisnolarni typed
 * [AppException] ga aylantirish. Har data-source shuni chaqiradi, o'zi try/catch yozmaydi:
 *
 * ```
 * suspend fun getProfile() = safeApiCall(connectivity) { api.getProfile() } // BaseResponse<Profile>
 * ```
 *
 * [call] backend'ning standart konvertini ([BaseResponse]) qaytarishi kutiladi.
 */
suspend fun <T> safeApiCall(
    connectivity: NetworkConnectivity? = null,
    call: suspend () -> BaseResponse<T>,
): Resource<T> = runSafely(connectivity) { call().check() }

/**
 * Konvertsiz (raw) variant — generatsiya qilingan API to'g'ridan-to'g'ri modelni qaytarganда
 * yoki uchinchi-tomon xizmatlar uchun. Xato-ishlash aynan bir xil.
 */
suspend fun <T> safeCall(
    connectivity: NetworkConnectivity? = null,
    call: suspend () -> T,
): Resource<T> = runSafely(connectivity, call)

private suspend fun <T> runSafely(
    connectivity: NetworkConnectivity?,
    block: suspend () -> T,
): Resource<T> {
    // Internet yo'q bo'lsa — so'rov qilmasdan aniq xato.
    if (connectivity?.isOnline() == false) return errorOf(AppException.NoInternet())
    return try {
        Resource.Success(block())
    } catch (e: CancellationException) {
        throw e // korutina bekori — uzatiladi
    } catch (e: AppException) {
        errorOf(e) // checker allaqachon typed tashlagan
    } catch (e: ClientRequestException) {
        errorOf(e.response.status.toAppException(e)) // 4xx
    } catch (e: ServerResponseException) {
        errorOf(AppException.Server(e.response.status.value, e)) // 5xx
    } catch (e: Throwable) {
        // Tarmoq/timeout/parse — matn va joriy internet holatiga qarab.
        errorOf(e.toAppException(connectivity?.isOnline() ?: true))
    }
}

/** HTTP status kodini typed [AppException] ga aylantiradi. */
fun HttpStatusCode.toAppException(cause: Throwable? = null): AppException = when (value) {
    401 -> AppException.Unauthorized(cause)
    403 -> AppException.PermissionDenied(cause)
    404 -> AppException.NotFound(cause)
    408 -> AppException.Timeout(cause)
    in 400..499 -> AppException.Validation(description.ifBlank { "So'rov noto'g'ri." }, cause)
    in 500..599 -> AppException.Server(value, cause)
    else -> AppException.Unknown(cause = cause)
}
