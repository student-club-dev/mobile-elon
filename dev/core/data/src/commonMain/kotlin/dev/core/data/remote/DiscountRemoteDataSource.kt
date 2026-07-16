package dev.core.data.remote

import dev.core.common.Resource
import dev.core.common.network.NetworkConnectivity
import dev.core.data.dto.DiscountsResponseDto
import dev.core.network.response.BaseResponse
import dev.core.network.response.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Chegirmalar uchun masofaviy (backend) manba — B4 offline-first shablonining tarmoq qismi.
 *
 * Repository shu interfeys orqali serverdan oladi va local DB'ga yozadi. Ktor klientiga
 * Firebase ID token allaqachon avtomatik qo'shiladi (B3). Boshqa domenlar (Jobs, Students...)
 * aynan shu shakldan nusxa oladi.
 */
interface DiscountRemoteDataSource {
    suspend fun fetchDiscounts(): Resource<DiscountsResponseDto>
}

/**
 * Ktor implementatsiyasi — **standart response naqshi** (IYM-business):
 * so'rov [safeApiCall] bilan o'raladi (internet tekshiruvi + typed xatolar), backend esa
 * [BaseResponse] konvertida (`status` + `result`) qaytaradi. Boshqa data-source'lar shundan
 * nusxa oladi — try/catch va status-tekshiruv takrorlanmaydi.
 */
class KtorDiscountRemoteDataSource(
    private val client: HttpClient,
    private val connectivity: NetworkConnectivity,
) : DiscountRemoteDataSource {

    override suspend fun fetchDiscounts(): Resource<DiscountsResponseDto> =
        safeApiCall(connectivity) {
            client.get("discounts").body<BaseResponse<DiscountsResponseDto>>()
        }
}
