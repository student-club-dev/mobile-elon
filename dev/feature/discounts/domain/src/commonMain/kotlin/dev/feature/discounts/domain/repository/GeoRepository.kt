package dev.feature.discounts.domain.repository

import dev.core.common.Resource

/** Xaritadagi nuqtadan topilgan manzil. */
data class ResolvedAddress(
    val address: String,
    val regionId: String? = null,
    val districtId: String? = null,
)

/** Qidiruv natijasi — xaritada topilgan joy. */
data class PlaceSuggestion(
    /** "Mega Planet" yoki "Amir Temur ko'chasi 12" */
    val title: String,
    /** To'liq manzil (tuman, shahar). */
    val subtitle: String,
    val lat: Double,
    val lng: Double,
)

/**
 * Geokodlash — xaritadagi nuqtadan manzil olish va manzil bo'yicha joy qidirish.
 * Shu sabab foydalanuvchi manzilni **qo'lda yozmaydi**: yo xaritadan tanlaydi, yo qidiradi.
 *
 * Backendsiz rejimda OpenStreetMap Nominatim (tekin) ishlatiladi; backend yoqilganda
 * `POST /v1/geo/reverse-geocode` va `/v1/geo/geocode` (Yandex Geocoder proksisi) o'rniga qo'yiladi.
 */
interface GeoRepository {

    suspend fun reverseGeocode(lat: Double, lng: Double): Resource<ResolvedAddress>

    /** Joy nomi yoki manzil bo'yicha qidiruv (faqat O'zbekiston ichida). */
    suspend fun search(query: String): Resource<List<PlaceSuggestion>>
}
