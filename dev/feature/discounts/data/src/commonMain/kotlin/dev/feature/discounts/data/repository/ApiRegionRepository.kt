package dev.feature.discounts.data.repository

import dev.core.network.generated.api.GeoApi
import dev.core.network.generated.model.DistrictDto
import dev.core.network.generated.model.RegionDto
import dev.feature.discounts.domain.model.District
import dev.feature.discounts.domain.model.GeoCatalog
import dev.feature.discounts.domain.model.Region
import dev.feature.discounts.domain.repository.RegionRepository
import io.ktor.client.call.body

/**
 * Viloyat/tuman ma'lumotnomasi **backenddan** (`GET /v1/regions` + `GET /v1/districts`),
 * xato bo'lsa klientdagi statik [GeoCatalog] dan.
 *
 * Ro'yxat kamdan-kam o'zgargani uchun bir marta olinib **xotirada keshlanadi** — forma har
 * ochilganda so'rov ketmaydi.
 *
 * Backendni afzal ko'rishimizning sababi: filial `regionId`/`districtId` bilan saqlanadi va
 * ularni server o'z ro'yxati bo'yicha tekshiradi. Statik katalog id'lari mos kelmasa filial
 * saqlanmasdi, shuning uchun haqiqat manbai — server.
 */
class ApiRegionRepository(
    private val api: GeoApi,
) : RegionRepository {

    private var cached: List<Region>? = null

    override suspend fun regions(): List<Region> {
        cached?.let { return it }
        val loaded = runCatching {
            val regions: List<RegionDto> = api.getRegions().body()
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

    override suspend fun districts(regionId: String): List<District> =
        regions().firstOrNull { it.id == regionId }?.districts.orEmpty()
}
