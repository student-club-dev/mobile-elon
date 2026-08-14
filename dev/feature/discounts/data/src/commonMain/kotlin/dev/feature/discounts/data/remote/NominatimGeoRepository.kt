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
 * MUHIM: bu klient ilovaning umumiy Ktor klienti EMAS — u har so'rovga sessiya Bearer
 * tokenini qo'shadi va bazaviy manzili `api.elon.uz`. Nominatim'ga o'z klienti kerak.
 */
class NominatimGeoRepository(
    private val httpClient: HttpClient,
) : GeoRepository {

    /**
     * Bir seans ichidagi javoblar keshi.
     *
     * Foydalanuvchi qidiruv matnini tahrirlaganda ("chilonzo" → "chilonzor" → "chilonzo")
     * yoki xaritani oldingi nuqtaga qaytarganda aynan bir xil so'rov takrorlanadi. Nominatim
     * qoidasi soniyasiga 1 ta so'rovni so'raydi, shuning uchun takrorini tarmoqqa
     * chiqarmaymiz. Kesh kichik va jarayon bilan birga o'chadi.
     */
    private val searchCache = LruCache<String, List<PlaceSuggestion>>(MAX_CACHE)
    private val reverseCache = LruCache<String, ResolvedAddress>(MAX_CACHE)

    override suspend fun search(
        query: String,
        nearLat: Double?,
        nearLng: Double?,
    ): Resource<List<PlaceSuggestion>> {
        val key = "${query.trim().lowercase()}|${nearLat.round()}|${nearLng.round()}"
        searchCache[key]?.let { return Resource.Success(it) }

        return try {
            val results: List<NominatimSearchResult> = httpClient.get("$BASE_URL/search") {
                parameter("q", query)
                parameter("format", "jsonv2")
                // Faqat O'zbekiston — talaba chegirmasi boshqa davlatda bo'lmaydi va
                // qidiruv ancha aniqroq ishlaydi.
                parameter("countrycodes", "uz")
                parameter("addressdetails", 1)
                parameter("limit", 8)
                parameter("accept-language", ACCEPT_LANGUAGE)
                header("User-Agent", USER_AGENT)

                // Xarita ko'rinayotgan hududni ustun qo'yamiz. `bounded=0` — quti tashqarisidagi
                // natijalar ham qaytadi, lekin pastroq o'rinda. Shunda "Amir Temur ko'chasi"
                // avval shu shahardagisini, keyin boshqalarni ko'rsatadi.
                if (nearLat != null && nearLng != null) {
                    parameter("viewbox", viewbox(nearLat, nearLng))
                    parameter("bounded", 0)
                }
            }.body()

            val suggestions = results.mapNotNull { it.toSuggestion() }
            searchCache[key] = suggestions
            Resource.Success(suggestions)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Qidirib bo'lmadi", e)
        }
    }

    override suspend fun reverseGeocode(lat: Double, lng: Double): Resource<ResolvedAddress> {
        val key = "${lat.round()}|${lng.round()}"
        reverseCache[key]?.let { return Resource.Success(it) }

        return try {
            val response: NominatimResponse = httpClient.get("$BASE_URL/reverse") {
                parameter("lat", lat)
                parameter("lon", lng)
                parameter("format", "jsonv2")
                parameter("zoom", 18) // ko'cha/uy darajasi
                // Aniq talab qilamiz: tuman aynan `address` bo'laklaridan olinadi.
                parameter("addressdetails", 1)
                parameter("accept-language", ACCEPT_LANGUAGE)
                header("User-Agent", USER_AGENT)
            }.body()

            val resolved = response.toResolved()
            reverseCache[key] = resolved
            Resource.Success(resolved)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Manzilni aniqlab bo'lmadi", e)
        }
    }

    /**
     * Nuqta atrofidagi ~40 km li quti: `min_lon,min_lat,max_lon,max_lat`.
     * Bir shahar va uning atrofini qamraydi — foydalanuvchi qidirayotgan hudud shu.
     */
    private fun viewbox(lat: Double, lng: Double): String {
        val d = VIEWBOX_DEGREES
        return "${lng - d},${lat - d},${lng + d},${lat + d}"
    }

    /** Kesh kaliti uchun — ~11 m aniqlik yetarli, mikroharakatlar yangi so'rov chiqarmasin. */
    private fun Double?.round(): String =
        if (this == null) "-" else ((this * 10_000).toLong() / 10_000.0).toString()

    private companion object {
        const val BASE_URL = "https://nominatim.openstreetmap.org"
        const val USER_AGENT = "ElonUz/1.0 (https://elon.uz)"

        /**
         * Nominatim'da o'zbekcha nomlar to'liq emas — ko'p joyning faqat ruscha yoki inglizcha
         * nomi bor. Ro'yxat berilganda mavjud bo'lgan birinchisi tanlanadi, ya'ni bo'sh nom
         * o'rniga hech bo'lmasa ruscha nom qaytadi.
         */
        const val ACCEPT_LANGUAGE = "uz,ru;q=0.8,en;q=0.5"

        const val VIEWBOX_DEGREES = 0.35
        const val MAX_CACHE = 40
    }
}

/**
 * Eng kam ishlatilganini chiqarib tashlovchi oddiy kesh (`LinkedHashMap` ning
 * `accessOrder` rejimi KMP'da yo'q, shuning uchun qo'lda).
 */
private class LruCache<K, V>(private val maxSize: Int) {
    private val entries = LinkedHashMap<K, V>()

    operator fun get(key: K): V? {
        val value = entries.remove(key) ?: return null
        entries[key] = value // eng oxirgi ishlatilgan bo'lib qayta qo'yiladi
        return value
    }

    operator fun set(key: K, value: V) {
        entries.remove(key)
        entries[key] = value
        while (entries.size > maxSize) {
            entries.remove(entries.keys.first())
        }
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

/**
 * Nominatim manzil bo'laklari.
 *
 * MUHIM — `city_district` va `state_district`: O'zbekiston tumanlari Nominatim'da aynan shu
 * maydonlarda keladi (Toshkent tumanlari — `city_district`, viloyat tumanlari —
 * `state_district`/`county`). Ilgari ular umuman o'qilmasdi, shuning uchun xaritadan joy
 * tanlanganда tuman deyarli hech qachon aniqlanmas va foydalanuvchi uni har safar qo'lda
 * tanlashga majbur bo'lardi.
 */
@Serializable
private data class NominatimAddress(
    val road: String? = null,
    @SerialName("house_number") val houseNumber: String? = null,
    val neighbourhood: String? = null,
    val suburb: String? = null,
    @SerialName("city_district") val cityDistrict: String? = null,
    @SerialName("state_district") val stateDistrict: String? = null,
    val district: String? = null,
    val municipality: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val county: String? = null,
    val region: String? = null,
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
    val candidates = listOfNotNull(
        address?.state,
        address?.region,
        address?.city,
    )
    return GeoCatalog.regions().firstOrNull { region ->
        candidates.any { it.matchesPlace(region.name) }
    }?.id
}

/**
 * Tumanni topadi. Nomzodlar tartibi muhim: eng aniq maydon (`city_district`) birinchi,
 * eng umumiysi (`suburb` — mahalla nomi bo'lishi mumkin) oxirida.
 */
private fun matchDistrict(address: NominatimAddress?, regionId: String?): String? {
    if (regionId == null) return null
    val candidates = listOfNotNull(
        address?.cityDistrict,
        address?.stateDistrict,
        address?.district,
        address?.county,
        address?.municipality,
        address?.town,
        address?.village,
        address?.suburb,
    )
    val districts = GeoCatalog.districts(regionId)
    // Nomzodlar bo'yicha yuramiz (ro'yxat bo'yicha emas): birinchi, eng ishonchli maydon
    // mos kelsa o'shani olamiz. Aks holда `suburb` dagi mahalla nomi tasodifan boshqa
    // tumanga tushib qolishi mumkin edi.
    for (candidate in candidates) {
        districts.firstOrNull { candidate.matchesPlace(it.name) }?.let { return it.id }
    }
    return null
}

/**
 * Joy nomlarini taqqoslaydi.
 *
 * Bir xil tuman uch xil yozilishi mumkin: "Yunusobod tumani" (katalog), "Yunusabad District"
 * (Nominatim inglizchasi), "Юнусабадский район" (ruschasi). Shu sabab avval umumiy
 * qo'shimchalar ("tumani", "district", "shahri", "район"…) olib tashlanadi, keyin qolgan
 * o'zak **ikki tomonlama** solishtiriladi — qaysi nom uzunroq ekani oldindan noma'lum.
 */
private fun String.matchesPlace(catalogName: String): Boolean {
    val a = placeStem()
    val b = catalogName.placeStem()
    if (a.isEmpty() || b.isEmpty()) return false
    // Juda qisqa o'zak (masalan "olot") tasodifiy mos kelmasin.
    if (a.length < MIN_STEM || b.length < MIN_STEM) return a == b
    return a.contains(b) || b.contains(a)
}

/** Qo'shimchalarsiz, kichik harfli, apostroflarsiz o'zak ("Qo'qon" ≈ "Qoqon" ≈ "qo`qon"). */
private fun String.placeStem(): String {
    var value = lowercase()
    PLACE_SUFFIXES.forEach { value = value.replace(it, " ") }
    return value.filter { it.isLetterOrDigit() }
}

/** Nomdagi ma'no tashimaydigan qo'shimchalar — uch tilda ham. */
private val PLACE_SUFFIXES = listOf(
    "tumani", "tuman", "shahri", "shahar", "viloyati", "viloyat",
    "district", "region", "city", "province",
    "район", "районы", "область", "город", "городской",
)

/** Shundan qisqa o'zaklar faqat to'liq tengligida hisoblanadi. */
private const val MIN_STEM = 4
