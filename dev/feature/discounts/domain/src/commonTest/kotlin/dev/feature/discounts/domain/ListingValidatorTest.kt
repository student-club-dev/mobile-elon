package dev.feature.discounts.domain

import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.GeoCatalog
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.ListingDiscount
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.domain.model.Geo
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingRedemption
import dev.feature.discounts.domain.model.ListingValidator
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.RedemptionMethod
import dev.feature.discounts.domain.model.formatSum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Chegirma formulasi va publish shartlari — `DISCOUNTS_BUSINESS_API.md` §3.5 va §6.1. */
class ListingValidatorTest {

    private fun validListing(
        discount: ListingDiscount = ListingDiscount(DiscountType.PERCENT, 20),
        originalPrice: Long = 55_000,
        images: List<String> = listOf("data:image/jpeg;base64,AAA"),
        branches: List<ListingBranch> = listOf(
            ListingBranch(
                id = "br1",
                lat = 41.2856,
                lng = 69.2034,
                address = "Chilonzor 9-kvartal, 42-uy",
                name = "Chilonzor filiali",
            ),
        ),
    ) = Listing(
        id = "lst_1",
        ownerId = "u1",
        businessType = BusinessType("NATIONAL_FOOD"),
        businessName = "Chaykhana Navruz",
        categoryKey = "PIZZA",
        title = "Pepperoni pitsa",
        images = images,
        priceUnit = PriceUnit.PER_ITEM,
        originalPrice = originalPrice,
        discount = discount,
        redemption = ListingRedemption(RedemptionMethod.QR),
        branches = branches,
        // PIZZA kategoriyasida `size` — katalogда majburiy maydon.
        attributes = mapOf("size" to "30 sm"),
        validFrom = 1_000,
        validTo = 2_000,
        createdAt = 0,
        updatedAt = 0,
    )

    @Test
    fun `to'g'ri e'lon xatosiz o'tadi`() {
        assertEquals(emptyList(), ListingValidator.validate(validListing()))
    }

    @Test
    fun `foiz chegirma yakuniy narxni to'g'ri hisoblaydi`() {
        // 55 000 dan 20% → 44 000 (spec'dagi misol).
        assertEquals(44_000, ListingDiscount(DiscountType.PERCENT, 20).finalPrice(55_000))
        assertEquals(45_000, ListingDiscount(DiscountType.FIXED_AMOUNT, 10_000).finalPrice(55_000))
        assertEquals(40_000, ListingDiscount(DiscountType.SPECIAL_PRICE, 40_000).finalPrice(55_000))
        // 1+1 — narx o'zgarmaydi.
        assertEquals(55_000, ListingDiscount(DiscountType.FREE_ITEM, 0).finalPrice(55_000))
    }

    @Test
    fun `yakuniy narx hech qachon manfiy bo'lmaydi`() {
        assertEquals(0, ListingDiscount(DiscountType.FIXED_AMOUNT, 99_000).finalPrice(55_000))
    }

    @Test
    fun `90 foizdan yuqori chegirma rad etiladi`() {
        val errors = ListingValidator.validate(
            validListing(discount = ListingDiscount(DiscountType.PERCENT, 95)),
        )
        assertTrue(errors.any { it.field == ListingField.DISCOUNT })
    }

    @Test
    fun `summa chegirmasi narxdan oshib ketolmaydi`() {
        val errors = ListingValidator.validate(
            validListing(discount = ListingDiscount(DiscountType.FIXED_AMOUNT, 60_000)),
        )
        assertTrue(errors.any { it.field == ListingField.DISCOUNT })
    }

    @Test
    fun `rasm va kamida bitta filial majburiy`() {
        val errors = ListingValidator.validate(validListing(images = emptyList(), branches = emptyList()))
        assertTrue(errors.any { it.field == ListingField.IMAGES })
        assertTrue(errors.any { it.field == ListingField.LOCATION })
    }

    /**
     * Onlayn biznes (`isOnlineOnly`) yoki filialsiz biznes — spec bo'yicha bo'sh `branchIds`
     * to'g'ri (= barcha faol filiallar). Bunday e'lon to'silmasligi kerak edi: filial bu
     * formada yaratilmaydi, ya'ni foydalanuvchida chiqish yo'li yo'q.
     */
    @Test
    fun `filial taklif qilinmasa tanlov majburiy emas`() {
        val errors = ListingValidator.validate(
            validListing(branches = emptyList()),
            requireBranch = false,
        )
        assertTrue(errors.none { it.field == ListingField.LOCATION }, "filialsiz e'lon to'sildi: $errors")
    }

    @Test
    fun `koordinata O'zbekiston hududida bo'lishi kerak`() {
        // Parij — O'zbekistondan tashqarida.
        val errors = ListingValidator.validate(
            validListing(
                branches = listOf(ListingBranch("br1", lat = 48.85, lng = 2.35, address = "Paris")),
            ),
        )
        assertTrue(errors.any { it.field == ListingField.LOCATION })
    }

    /**
     * Yaqin joylashgan ikki filial (masalan bitta savdo markazidagi ikkita nuqta) e'lonni
     * TO'SMAYDI. Dublikat tekshiruvi filial YARATISHGA tegishli; e'lon formasi esa serverdagi
     * tayyor filiallardan tanlaydi va ularning koordinatasini o'zgartira olmaydi.
     */
    @Test
    fun `yaqin joylashgan ikki filial e'lonni to'smaydi`() {
        val errors = ListingValidator.validate(
            validListing(
                branches = listOf(
                    ListingBranch("br1", 41.2856, 69.2034, "Chilonzor 9-kvartal"),
                    ListingBranch("br2", 41.2857, 69.2035, "Chilonzor 9-kvartal, yonida"),
                ),
            ),
        )
        assertTrue(errors.none { it.field == ListingField.LOCATION }, "yaqin filiallar to'sildi: $errors")
    }

    @Test
    fun `bir nechta filial qabul qilinadi`() {
        val errors = ListingValidator.validate(
            validListing(
                branches = listOf(
                    ListingBranch("br1", 41.2856, 69.2034, "Chilonzor filiali"),
                    ListingBranch("br2", 41.3260, 69.2280, "Yunusobod filiali"),
                ),
            ),
        )
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `eng yaqin filial masofasi bilan topiladi`() {
        val listing = validListing(
            branches = listOf(
                // Yunusobod — talabaga uzoqroq
                ListingBranch("br-far", 41.3600, 69.2890, "Yunusobod filiali"),
                // Chilonzor — talabaga yaqin
                ListingBranch("br-near", 41.2856, 69.2034, "Chilonzor filiali"),
            ),
        )

        // Talaba Chilonzor metrosi yonida.
        val nearest = listing.nearestBranch(userLat = 41.2830, userLng = 69.2050)
        assertEquals("br-near", nearest?.branch?.id)
        assertTrue((nearest?.distanceMeters ?: 0.0) < 1000, "Chilonzor filiali 1 km dan yaqin bo'lishi kerak")
        assertEquals("m", nearest?.distanceLabel()?.takeLast(1))

        // Joylashuv noma'lum — masofasiz, lekin ro'yxat baribir ishlaydi.
        val unknown = listing.nearestBranch(null, null)
        assertEquals("br-far", unknown?.branch?.id) // birinchi filial
        assertEquals(null, unknown?.distanceLabel())
    }

    @Test
    fun `masofa haversine bilan to'g'ri hisoblanadi`() {
        // Chilonzor metrosi -> Mustaqillik maydoni: taxminan 5-6 km.
        val meters = Geo.distanceMeters(41.2755, 69.2044, 41.3111, 69.2797)
        assertTrue(meters in 5_000.0..8_000.0, "kutilgan 5-8 km, olindi: ${meters.toInt()} m")

        // Bir xil nuqta -> 0.
        assertEquals(0.0, Geo.distanceMeters(41.3, 69.2, 41.3, 69.2))
    }

    @Test
    fun `1+1 aksiyasi shartsiz o'tmaydi`() {
        val errors = ListingValidator.validate(
            validListing(discount = ListingDiscount(DiscountType.FREE_ITEM, 0, conditions = null)),
        )
        assertTrue(errors.any { it.field == ListingField.DISCOUNT })
    }

    @Test
    fun `zaxira katalog backenddagi 27 turni to'liq qamraydi`() {
        // Ro'yxat backend `catalog-seed.json` dan ko'chirilgan — soni kamayib ketsa,
        // demak ko'chirish chala bo'lgan va oflayn foydalanuvchi turni topolmaydi.
        assertEquals(27, ListingCatalog.types.size)
        ListingCatalog.types.forEach { type ->
            assertTrue(ListingCatalog.categories(type).size > 1, "${'$'}{type.key}: kategoriya yo'q")
            assertTrue(ListingCatalog.attributes(type).isNotEmpty(), "${'$'}{type.key}: maydon yo'q")
            assertTrue(ListingCatalog.priceUnits(type).isNotEmpty(), "${'$'}{type.key}: narx birligi yo'q")
        }
    }

    @Test
    fun `har bir turda ALL birinchi va OTHER oxirgi kategoriya`() {
        // Backend shartnomasi (CATALOG_HANDOFF.md §2): ro'yxat doim shu ikkisi bilan
        // o'ralgan — forma "hammasiga" chipini birinchi, "Boshqa"ni oxirgi ko'rsatadi.
        ListingCatalog.types.forEach { type ->
            val keys = ListingCatalog.categories(type).map { it.key }
            assertEquals(ListingCatalog.ALL_KEY, keys.first(), "${'$'}{type.key}: birinchisi ALL emas")
            assertEquals(ListingCatalog.OTHER_KEY, keys.last(), "${'$'}{type.key}: oxirgisi OTHER emas")
        }
    }

    @Test
    fun `geo katalogda 14 viloyat va barqaror id'lar bor`() {
        assertEquals(14, GeoCatalog.regions().size)
        assertEquals("Chilonzor", GeoCatalog.district("TOSHKENT_SHAHRI", "CHILONZOR")?.name)
        // Apostrof id'dan tushib qoladi: "Mirzo Ulug'bek" → MIRZO_ULUGBEK
        assertEquals("Mirzo Ulug'bek", GeoCatalog.district("TOSHKENT_SHAHRI", "MIRZO_ULUGBEK")?.name)
    }

    @Test
    fun `narx uch xonali guruhlarga ajratiladi`() {
        assertEquals("55 000", 55_000L.formatSum())
        assertEquals("999", 999L.formatSum())
        assertEquals("1 250 000", 1_250_000L.formatSum())
    }
}
