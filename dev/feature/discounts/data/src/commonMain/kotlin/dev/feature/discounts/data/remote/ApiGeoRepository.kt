package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.core.common.errorOf
import dev.core.common.error.toAppException
import dev.core.common.network.NetworkConnectivity
import dev.core.network.generated.api.GeoApi
import dev.core.network.generated.model.GeocodeRequestDto
import dev.core.network.generated.model.GeocodeResultDto
import dev.core.network.generated.model.ReverseGeocodeRequestDto
import dev.core.network.generated.model.ReverseGeocodeResponseDto
import dev.feature.discounts.domain.repository.GeoRepository
import dev.feature.discounts.domain.repository.PlaceSuggestion
import dev.feature.discounts.domain.repository.ResolvedAddress
import io.ktor.client.call.body

/**
 * Geokodlashning **backend** implementatsiyasi — `POST /geo/geocode` va `/geo/reverse-geocode`.
 *
 * Nominatim'dan afzalligi: backend `regionId`/`districtId` ni ham qaytaradi, shuning uchun
 * filial viloyat/tumanga avtomatik bog'lanadi (Nominatim faqat matnli manzil beradi).
 *
 * Xato **yutilmaydi** — [FallbackGeoRepository] uni ushlab Nominatim'ga o'tadi.
 */
class ApiGeoRepository(
    private val api: GeoApi,
    private val connectivity: NetworkConnectivity,
) : GeoRepository {

    override suspend fun reverseGeocode(lat: Double, lng: Double): Resource<ResolvedAddress> = try {
        val body: ReverseGeocodeResponseDto = api.reverseGeocode(ReverseGeocodeRequestDto(lat, lng)).body()
        val address = body.address
        if (address.isNullOrBlank()) {
            errorOf(dev.core.common.error.AppException.NotFound())
        } else {
            Resource.Success(
                ResolvedAddress(
                    address = address,
                    regionId = body.regionId,
                    districtId = body.districtId,
                ),
            )
        }
    } catch (e: Exception) {
        errorOf(e.toAppException(connectivity.isOnline()))
    }

    override suspend fun search(query: String): Resource<List<PlaceSuggestion>> = try {
        val body: List<GeocodeResultDto> = api.geocode(GeocodeRequestDto(query)).body()
        Resource.Success(body.map { it.toSuggestion() })
    } catch (e: Exception) {
        errorOf(e.toAppException(connectivity.isOnline()))
    }
}

/**
 * Backend bitta `formattedAddress` beradi, UI esa sarlavha + tavsif kutadi — birinchi
 * bo'lak sarlavha (odatda joy nomi yoki ko'cha), qolgani tavsif bo'ladi.
 */
private fun GeocodeResultDto.toSuggestion(): PlaceSuggestion {
    val parts = formattedAddress.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    return PlaceSuggestion(
        title = parts.firstOrNull() ?: formattedAddress,
        subtitle = parts.drop(1).joinToString(", "),
        lat = lat,
        lng = lng,
    )
}
