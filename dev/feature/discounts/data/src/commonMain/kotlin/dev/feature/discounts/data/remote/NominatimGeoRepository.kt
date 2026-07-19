package dev.feature.discounts.data.remote

import dev.core.common.Resource
import dev.feature.discounts.domain.model.GeoCatalog
import dev.feature.discounts.domain.repository.GeoRepository
import dev.feature.discounts.domain.repository.PlaceSuggestion
import dev.feature.discounts.domain.repository.ResolvedAddress
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Teskari geokodlash — **OpenStreetMap Nominatim** (tekin, API kalit talab qilmaydi).
 *
 * Backend yoqilganda `POST /v1/geo/reverse-geocode` (Yandex Geocoder proksisi) o'rniga
 * qo'yiladi — u O'zbekiston manzillarida aniqroq. Nominatim'ning foydalanish qoidasi
 * `User-Agent` ni talab qiladi va soniyasiga 1 so'rovdan ko'p yubormaslikni so'raydi;
 * bizda so'rov faqat foydalanuvchi xaritada nuqta tanlaganda ketadi, shuning uchun yetarli.
 *
 * MUHIM: bu klient ilovaning umumiy Ktor klienti EMAS — u har so'rovga Firebase Bearer
 * tokenini qo'shadi va bazaviy manzili `api.elon.uz`. Nominatim'ga o'z klienti kerak.
 */
class NominatimGeoRepository(
    private val httpClient: HttpClient,
) : GeoRepository {

    override suspend fun search(query: String): Resource<List<PlaceSuggestion>> = try {
        val results: List<NominatimSearchResult> = httpClient.get("$BASE_URL/search") {
            parameter("q", query)
            parameter("format", "jsonv2")
            // Faqat O'zbekiston — talaba chegirmasi boshqa davlatda bo'lmaydi va
            // qidiruv ancha aniqroq ishlaydi.
            parameter("countrycodes", "uz")
            parameter("addressdetails", 1)
            parameter("limit", 8)
            parameter("accept-language", "uz")
            header("User-Agent", USER_AGENT)
        }.body()

        Resource.Success(results.mapNotNull { it.toSuggestion() })
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Qidirib bo'lmadi", e)
    }

    override suspend fun reverseGeocode(lat: Double, lng: Double): Resource<ResolvedAddress> = try {
        val response: NominatimResponse = httpClient.get("$BASE_URL/reverse") {
            parameter("lat", lat)
            parameter("lon", lng)
            parameter("format", "jsonv2")
            parameter("zoom", 18) // ko'cha/uy darajasi
            parameter("accept-language", "uz")
            header("User-Agent", USER_AGENT)
        }.body()

        Resource.Success(response.toResolved())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Manzilni aniqlab bo'lmadi", e)
    }

    private companion object {
        const val BASE_URL = "https://nominatim.openstreetmap.org"
        const val USER_AGENT = "ElonUz/1.0 (https://elon.uz)"
    }
}

@Serializable
private data class NominatimResponse(
    @SerialName("display_name") val displayName: String? = null,
    val address: NominatimAddress? = null,
)

@Serializable
private data class NominatimSearchResult(
    val lat: String? = null,
    val lon: String? = null,
    val name: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val address: NominatimAddress? = null,
)

private fun NominatimSearchResult.toSuggestion(): PlaceSuggestion? {
    val latitude = lat?.toDoubleOrNull() ?: return null
    val longitude = lon?.toDoubleOrNull() ?: return null
    val full = displayName.orEmpty()

    // `name` — joyning o'z nomi ("Mega Planet"). Bo'sh bo'lsa (oddiy manzil) to'liq
    // nomning birinchi qismini olamiz: "Amir Temur ko'chasi, 12, Yunusobod, ..."
    val title = name?.takeIf { it.isNotBlank() }
        ?: full.substringBefore(",").ifBlank { return null }

    val subtitle = full.removePrefix(title).trimStart(',', ' ')

    return PlaceSuggestion(title = title, subtitle = subtitle, lat = latitude, lng = longitude)
}

@Serializable
private data class NominatimAddress(
    val road: String? = null,
    @SerialName("house_number") val houseNumber: String? = null,
    val neighbourhood: String? = null,
    val suburb: String? = null,
    val city: String? = null,
    val town: String? = null,
    val county: String? = null,
    val state: String? = null,
)

private fun NominatimResponse.toResolved(): ResolvedAddress {
    val a = address

    // Talabaga foydali qism: ko'cha + uy raqami. Nominatim uni bermasa — to'liq nom.
    val street = listOfNotNull(a?.road, a?.houseNumber).joinToString(" ").ifBlank { null }
    val area = a?.neighbourhood ?: a?.suburb
    val short = listOfNotNull(street, area).joinToString(", ")

    return ResolvedAddress(
        address = short.ifBlank { displayName.orEmpty() },
        regionId = matchRegion(a),
        districtId = matchDistrict(a, matchRegion(a)),
    )
}

/**
 * Nominatim viloyat nomini ("Toshkent shahri", "Tashkent") [GeoCatalog] id'siga bog'laydi.
 * Topilmasa `null` — bu xato emas: koordinata baribir bor, viloyat faqat filtr uchun.
 */
private fun matchRegion(address: NominatimAddress?): String? {
    val candidates = listOfNotNull(address?.state, address?.city)
    return GeoCatalog.regions().firstOrNull { region ->
        candidates.any { it.normalize().contains(region.name.take(6).normalize()) }
    }?.id
}

private fun matchDistrict(address: NominatimAddress?, regionId: String?): String? {
    if (regionId == null) return null
    val candidates = listOfNotNull(address?.county, address?.suburb, address?.town, address?.city)
    return GeoCatalog.districts(regionId).firstOrNull { district ->
        candidates.any { it.normalize().contains(district.name.take(5).normalize()) }
    }?.id
}

/** Taqqoslash uchun: kichik harf, apostroflarsiz ("Qo'qon" ≈ "Qoqon" ≈ "qo`qon"). */
private fun String.normalize(): String =
    lowercase().filter { it.isLetterOrDigit() }
