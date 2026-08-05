package dev.feature.discounts.domain.repository

import dev.feature.discounts.domain.model.District
import dev.feature.discounts.domain.model.MetroStation
import dev.feature.discounts.domain.model.Region

/**
 * Geografik ma'lumotnoma — viloyat/tuman (`GET /geo/regions`) va Toshkent metro bekatlari
 * (`GET /geo/metro-stations`).
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

    /**
     * Toshkent metro bekatlari — filial formasidagi mo'ljal maydonini to'ldirish uchun.
     *
     * ⚠️ Bu **tanlov emas, taklif**: backendда `Branch.metroStation` erkin matn bo'lib
     * qoladi (FK emas), chunki bazaga hali kirmagan yangi bekat filial saqlashni buzib
     * qo'yardi (`DISCOUNTS_BUSINESS_API_RESPONSE.md` §7). Shuning uchun ro'yxat yuklanmasa
     * ham forma ishlaydi — foydalanuvchi bekat nomini qo'lda yozadi.
     *
     * Xato bo'lsa **bo'sh ro'yxat** qaytadi (klientda zaxira katalog yo'q: 50 ta bekatni
     * ilovaga qotirib qo'yish ularni birinchi yangi bekat ochilishida eskirtiradi).
     */
    suspend fun metroStations(): List<MetroStation>
}
