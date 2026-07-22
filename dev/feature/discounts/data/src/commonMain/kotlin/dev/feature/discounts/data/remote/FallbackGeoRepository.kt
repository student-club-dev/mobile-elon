package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.feature.discounts.domain.repository.GeoRepository
import dev.feature.discounts.domain.repository.PlaceSuggestion
import dev.feature.discounts.domain.repository.ResolvedAddress
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Backend + zaxira: avval [api] (`/geo/geocode`, `/geo/reverse-geocode`), u xato bersa
 * [nominatim] (OpenStreetMap, tekin).
 *
 * Zaxira sifatida namuna ma'lumot emas, **haqiqiy** geokodlash ishlatiladi — noto'g'ri manzil
 * filialni xato joyga qo'yadi, bu esa e'lonni talabaga ko'rinmas qiladi. Ikkalasi ham
 * ishlamasa xato qaytadi va `CreateBranchFromPointUseCase` manzil o'rniga koordinata yozadi.
 */
class FallbackGeoRepository(
    private val api: ApiGeoRepository,
    private val nominatim: NominatimGeoRepository,
) : GeoRepository {

    override suspend fun reverseGeocode(lat: Double, lng: Double): Resource<ResolvedAddress> =
        tryApi { api.reverseGeocode(lat, lng) } ?: nominatim.reverseGeocode(lat, lng)

    override suspend fun search(
        query: String,
        nearLat: Double?,
        nearLng: Double?,
    ): Resource<List<PlaceSuggestion>> {
        val fromApi = tryApi { api.search(query, nearLat, nearLng) }
        // Bo'sh natija ham "topilmadi" degani — bunda Nominatim'ni sinab ko'rish foydali.
        return fromApi?.takeIf { it.data.isNotEmpty() } ?: nominatim.search(query, nearLat, nearLng)
    }

    /**
     * Backendni sinaydi; xato bersa yoki [API_TIMEOUT_MS] ichida ulgurmasa `null` qaytaradi —
     * chaqiruvchi darrov Nominatim'ga o'tadi.
     *
     * Vaqt chegarasi klientnikidan ataylab qisqa: bu yerda foydalanuvchi tugmani bosib turibdi
     * va manzilni kutyapti, shuning uchun sekin backendni to'liq kutishdan ko'ra zaxiraga
     * o'tgan ma'qul.
     */
    private suspend fun <T> tryApi(call: suspend () -> Resource<T>): Resource.Success<T>? =
        withTimeoutOrNull(API_TIMEOUT_MS) {
            runCatching { call() }.getOrNull() as? Resource.Success
        }

    private companion object {
        const val API_TIMEOUT_MS = 4_000L
    }
}
