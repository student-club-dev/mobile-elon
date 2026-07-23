package dev.feature.discounts.data.remote

import dev.core.network.appJson
import dev.core.network.generated.model.CreateListingRequestDto
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingDiscount
import dev.feature.discounts.domain.model.ListingRedemption
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.RedemptionMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `POST /v1/business/{id}/listings` tanasi spec bilan mos keladimi.
 *
 * E'lon serverda rad etilsa foydalanuvchi uchun u shunchaki "saqlanmagan"dek ko'rinadi —
 * shuning uchun so'rov shakli shu yerda qulflanadi: majburiy maydonlar bor, server
 * hisoblaydigan maydonlar YO'Q, sanalar ISO-8601, enum qiymatlari spec'dagidek.
 */
class CreateListingRequestTest {

    /** Chiquvchi tananing aynan o'zi (klient `appJson` bilan seriyalaydi). */
    private val body = Json(appJson) { encodeDefaults = true }
        .encodeToString(CreateListingRequestDto.serializer(), listing().toCreateRequest())
        .let { Json.parseToJsonElement(it).jsonObject }

    @Test
    fun `spec talab qiladigan maydonlarning hammasi bor`() {
        // CreateListingRequestDto.required — elon-uz.json dan.
        listOf(
            "categoryKey", "title", "images", "priceUnit", "originalPrice",
            "discount", "redemption", "validFrom", "validTo",
        ).forEach { assertTrue(it in body, "majburiy maydon yo'q: $it") }
    }

    /** `finalPrice`, `status`, `usedCount`, `viewsCount` — server hisoblaydi (spec §3.5). */
    @Test
    fun `server hisoblaydigan maydonlar yuborilmaydi`() {
        listOf("finalPrice", "status", "usedCount", "viewsCount", "id").forEach {
            assertFalse(it in body, "server maydoni yuborilyapti: $it")
        }
        assertFalse("usedCount" in body["redemption"]!!.jsonObject, "redemption.usedCount yuborilyapti")
    }

    @Test
    fun `sanalar ISO-8601 da`() {
        assertEquals("2023-11-14T22:13:20Z", body["validFrom"]!!.jsonPrimitive.content)
        assertTrue(body["validTo"]!!.jsonPrimitive.content.endsWith("Z"))
    }

    @Test
    fun `enum qiymatlari spec nomlari bilan ketadi`() {
        assertEquals("PER_HOUR", body["priceUnit"]!!.jsonPrimitive.content)
        assertEquals("PERCENT", body["discount"]!!.jsonObject["type"]!!.jsonPrimitive.content)
        assertEquals("STUDENT_ID", body["redemption"]!!.jsonObject["method"]!!.jsonPrimitive.content)
    }

    /** Filial e'longa `branchIds` orqali bog'lanadi — obyekt sifatida emas. */
    @Test
    fun `filiallar id ro'yxati bo'lib ketadi`() {
        assertEquals("[\"br-1\"]", body["branchIds"].toString())
        assertFalse("branches" in body)
    }

    private fun listing() = Listing(
        id = "lst-1",
        ownerId = "u1",
        businessId = "biz-1",
        businessType = BusinessType("TENNIS"),
        businessName = "Kort",
        categoryKey = "OUTDOOR",
        title = "Kort ijarasi",
        images = listOf("https://cdn/x.jpg"),
        priceUnit = PriceUnit.PER_HOUR,
        originalPrice = 100_000,
        discount = ListingDiscount(DiscountType.PERCENT, 20),
        redemption = ListingRedemption(RedemptionMethod.STUDENT_ID, perUserLimit = 1),
        branches = listOf(ListingBranch(id = "br-1", lat = 41.3, lng = 69.2, address = "Amir Temur 1")),
        validFrom = 1_700_000_000_000,
        validTo = 1_702_000_000_000,
        attributes = mapOf("courtSurface" to "Gruntli"),
        createdAt = 0,
        updatedAt = 0,
    )
}
