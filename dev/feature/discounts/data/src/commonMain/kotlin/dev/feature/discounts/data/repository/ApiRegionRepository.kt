package dev.feature.discounts.data.repository

import dev.core.network.generated.api.GeoApi
import dev.core.network.generated.model.DistrictDto
import dev.core.network.generated.model.RegionDto
import dev.feature.discounts.domain.model.District
import dev.feature.discounts.domain.model.GeoCatalog
import dev.feature.discounts.domain.model.MetroStation
import dev.feature.discounts.domain.model.Region
import dev.feature.discounts.domain.repository.RegionRepository
import io.ktor.client.call.body

/**
 * Viloyat/tuman ma'lumotnomasi **backenddan**, xato bo'lsa klientdagi statik [GeoCatalog] dan.
 *
 * Ro'yxat kamdan-kam o'zgargani uchun bir marta olinib **xotirada keshlanadi** — forma har
 * ochilganda so'rov ketmaydi.
 *
 * Backendni afzal ko'rishimizning sababi: filial `regionId`/`districtId` bilan saqlanadi va
 * ularni server o'z ro'yxati bo'yicha tekshiradi. Statik katalog id'lari mos kelmasa filial
 * saqlanmasdi, shuning uchun haqiqat manbai — server.
 *
 * ### Qaysi yo'l ishlatiladi
 * Viloyatlar — **kontrakt yo'li** `GET /geo/regions` (`DISCOUNTS_BUSINESS_API_RESPONSE.md` §5.3).
 *
 * Tumanlar — bir so'rovda `GET /districts` orqali. Kontraktdagi
 * `GET /geo/regions/{regionId}/districts` bitta viloyatniki, bu interfeys esa hamma
 * viloyatni tumanlari bilan beradi: kontrakt yo'lidan foydalansak forma ochilishida 14 ta
 * so'rov ketardi. Ikkala yo'l bitta servis ustida ishlaydi, javob DTO'si ham bir xil.
 * Yakka viloyat kerak bo'lganda — [districts] (pastga qarang) kontrakt yo'liga boradi.
 */
class ApiRegionRepository(
    private val api: GeoApi,
) : RegionRepository {

    private var cached: List<Region>? = null

    /** Metro bekatlari alohida keshlanadi — ular faqat Toshkent formasi ochilganda so'raladi. */
    private var cachedMetro: List<MetroStation>? = null

    override suspend fun regions(): List<Region> {
        cached?.let { return it }
        val loaded = runCatching {
            val regions: List<RegionDto> = api.getGeoRegions().body()
            val districts: List<DistrictDto> = api.getDistricts().body()
            require(regions.isNotEmpty()) { "Bo'sh viloyatlar ro'yxati" }
            val byRegion = districts.groupBy { it.regionId }
            regions.map { region ->
                Region(
                    id = region.id,
                    name = region.nameUz,
                    districts = byRegion[region.id].orEmpty().map { District(it.id, it.nameUz) },
                )
            }
        }.getOrElse { GeoCatalog.regions() }
        cached = loaded
        return loaded
    }

    /**
     * Keshda tuman topilmasa kontrakt yo'liga (`GET /geo/regions/{regionId}/districts`)
     * boradi. Bu bo'sh qolmaslik uchun kerak: umumiy `GET /districts` uzilgan bo'lsa kesh
     * viloyatni tumansiz saqlab qo'yadi va forma "tuman tanlang" ro'yxatini bo'sh ko'rsatardi.
     */
    override suspend fun districts(regionId: String): List<District> {
        val cachedDistricts = regions().firstOrNull { it.id == regionId }?.districts.orEmpty()
        if (cachedDistricts.isNotEmpty()) return cachedDistricts
        return runCatching {
            api.getGeoDistricts(regionId).body().map { District(it.id, it.nameUz) }
        }.getOrElse { emptyList() }
    }

    override suspend fun metroStations(): List<MetroStation> {
        cachedMetro?.let { return it }
        val loaded = runCatching {
            api.getMetroStations().body().map {
                MetroStation(id = it.id, name = it.nameUz, line = it.line, lat = it.lat, lng = it.lng)
            }
        }.getOrElse { emptyList() }
        // Bo'sh natija keshlanmaydi: u xato ham bo'lishi mumkin, keshlansa forma bir marta
        // uzilgan tarmoq tufayli butun sessiya davomida bekatsiz qolardi.
        if (loaded.isNotEmpty()) cachedMetro = loaded
        return loaded
    }
}
