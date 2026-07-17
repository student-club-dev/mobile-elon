package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.feature.discounts.domain.repository.GeoRepository
import dev.feature.discounts.domain.repository.PlaceSuggestion
import dev.feature.discounts.domain.repository.ResolvedAddress

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
        runCatching { api.reverseGeocode(lat, lng) }.getOrNull() as? Resource.Success
            ?: nominatim.reverseGeocode(lat, lng)

    override suspend fun search(query: String): Resource<List<PlaceSuggestion>> {
        val fromApi = runCatching { api.search(query) }.getOrNull() as? Resource.Success
        // Bo'sh natija ham "topilmadi" degani — bunda Nominatim'ni sinab ko'rish foydali.
        return fromApi?.takeIf { it.data.isNotEmpty() } ?: nominatim.search(query)
    }
}
