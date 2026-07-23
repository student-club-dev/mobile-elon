package dev.feature.discounts.domain.repository

import dev.feature.discounts.domain.model.District
import dev.feature.discounts.domain.model.Region

/**
 * Viloyat/tuman ma'lumotnomasi (`GET /v1/regions`, `GET /v1/districts`).
 *
 * Nega backend muhim: filial `regionId`/`districtId` bilan saqlanadi va backend ularni
 * **o'z ro'yxati** bo'yicha tekshiradi. Klientdagi statik [dev.feature.discounts.domain.model.GeoCatalog]
 * faqat zaxira — tarmoq bo'lmasa forma baribir ishlaydi.
 */
interface RegionRepository {

    /** Barcha viloyatlar (tumanlari bilan). Xato bo'lsa klient katalogi qaytadi. */
    suspend fun regions(): List<Region>

    /** Bitta viloyatning tumanlari. */
    suspend fun districts(regionId: String): List<District>
}
