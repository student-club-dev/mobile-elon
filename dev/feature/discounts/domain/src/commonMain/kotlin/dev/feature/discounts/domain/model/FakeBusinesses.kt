package dev.feature.discounts.domain.model

/**
 * Backend javob bermaganда ishlatiladigan **namuna bizneslar**.
 *
 * Maqsad — backend hali yozilmagan/o'chgan bo'lsa ham ekran ishlashda davom etsin va
 * e'lon qo'yish oqimini sinab ko'rish mumkin bo'lsin. Real ma'lumot kelishi bilan bu
 * zaxira ishlatilmaydi.
 *
 * Koordinatalar — Toshkent markazi atrofida ([ListingBranch.UZ_LAT_RANGE] ichida).
 */
object FakeBusinesses {

    /** Namuna ro'yxat — bosh ekranда ko'rsatiladi. */
    fun sample(ownerId: String = FAKE_OWNER_ID): List<Business> = listOf(
        Business(
            id = "fake-cafe",
            ownerId = ownerId,
            name = "Bon Appetit",
            phone = "+998901234567",
            // Turlar backend katalogidan (`catalog-seed.json`) — namuna ham serverda
            // haqiqatan mavjud kalitlarni ishlatishi kerak, aks holda undan yaratilgan
            // e'lon serverda rad etilardi.
            businessType = BusinessType("NATIONAL_FOOD"),
            branches = listOf(
                ListingBranch(
                    id = "fake-cafe-branch",
                    lat = 41.311081,
                    lng = 69.240562,
                    address = "Amir Temur ko'chasi, 12",
                    name = "Markaziy filial",
                    landmark = "Hilton mehmonxonasi yonida",
                ),
            ),
        ),
        Business(
            id = "fake-game-club",
            ownerId = ownerId,
            name = "Cyber Arena",
            phone = "+998901112233",
            businessType = BusinessType("CYBER_CLUB"),
            branches = listOf(
                ListingBranch(
                    id = "fake-game-club-branch",
                    lat = 41.326355,
                    lng = 69.228783,
                    address = "Chilonzor 9-kvartal, 42-uy",
                    name = "Chilonzor filiali",
                ),
            ),
        ),
    )

    /** Id bo'yicha namuna biznes — topilmasa birinchisi. */
    fun byId(id: String): Business =
        sample().firstOrNull { it.id == id } ?: sample().first()

    private const val FAKE_OWNER_ID = "fake-owner"
}
