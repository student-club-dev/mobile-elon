package dev.feature.discounts.domain.model

/**
 * E'lon statistikasi (`GET /listings/{id}/stats`) — biznes egasi uchun.
 *
 * Barcha sonlar **serverdan** keladi va hisoblanmaydi: `conversionRate` ni ilova tomonida
 * `redemptions / views` deb chiqarish noto'g'ri bo'lardi, chunki server ikkala sonni ham
 * o'zining chegaralari bo'yicha (masalan takroriy ko'rishlarni birlashtirib) sanaydi.
 */
data class ListingStats(
    val listingId: String,
    /** E'lon sahifasi ochilgan marta. */
    val views: Int,
    /** Nechta talaba saqlab qo'ygan. */
    val favorites: Int,
    /** **Tasdiqlangan** foydalanishlar soni (kassir `redeem/confirm` bosgan). */
    val redemptions: Int,
    /** 0..1 oralig'ida. Ko'rish bo'lmasa server 0 qaytaradi (nolga bo'lish yo'q). */
    val conversionRate: Double,
    /** Tasdiqlangan foydalanishlarning umumiy summasi, butun so'mda. */
    val totalRevenue: Long,
) {
    /** "12%" — foizga aylantirilgan konversiya. */
    val conversionPercent: Int get() = (conversionRate * 100).toInt()
}

/**
 * Bitta foydalanish yozuvi (`GET /listings/{id}/redemptions`) — kim, qachon, qaysi filialda
 * va qancha summaga chegirmadan foydalangani.
 *
 * [studentName] `null` bo'lishi mumkin: talaba ismini ko'rsatmagan bo'lsa server uni bermaydi.
 * Bunda ekran `@username` yoki umumiy "Talaba" yozuvini ko'rsatadi — bo'sh qator emas.
 */
data class Redemption(
    val id: String,
    val listingId: String,
    val branchId: String?,
    val studentId: String,
    val studentName: String?,
    val studentUsername: String?,
    /** Kassa chekining summasi (butun so'm). Kassir kiritmagan bo'lsa `null`. */
    val amount: Long?,
    /** Tasdiqlangan vaqt (epoch ms). Hali tasdiqlanmagan bo'lsa `null`. */
    val redeemedAt: Long?,
) {
    /** Ro'yxatda ko'rsatiladigan nom — ism, bo'lmasa username. */
    fun displayName(): String? = studentName?.takeIf { it.isNotBlank() }
        ?: studentUsername?.takeIf { it.isNotBlank() }?.let { "@$it" }
}

/** Foydalanishlar sahifasi (`RedemptionPageDto`) — [ListingPage] bilan bir xil naqsh. */
data class RedemptionPage(
    val items: List<Redemption>,
    val page: Int,
    val size: Int,
    val total: Int,
    val hasNext: Boolean,
) {
    companion object {
        val EMPTY = RedemptionPage(emptyList(), 1, 0, 0, false)
    }
}

/**
 * Kassirning kod tekshiruvi natijasi (`POST /listings/{id}/redeem/verify`).
 *
 * Bu **hali tasdiqlash emas**: kod haqiqiy bo'lsa kassir chegirmani qo'llaydi va keyin
 * `redeem/confirm` bilan yakunlaydi. Ikki qadam ataylab — birinchisi hech narsani
 * o'zgartirmaydi, shuning uchun kassir kodni xavfsiz tekshirib ko'ra oladi.
 */
data class RedemptionCheck(
    val isValid: Boolean,
    /** Yaroqsizlik sababi — faqat [isValid] `false` bo'lganda. */
    val invalidReason: RedemptionInvalidReason?,
    val studentName: String?,
    val studentUsername: String?,
    /** Qo'llanadigan yakuniy narx (so'm) — kassir chekda shuni ko'rsatadi. */
    val finalPrice: Long?,
    val originalPrice: Long?,
)

/** Kod nega yaroqsiz (`VerifyRedemptionResponseDto.invalidReason`). */
enum class RedemptionInvalidReason {
    INVALID_CODE,
    ALREADY_REDEEMED,
    EXPIRED,
    LIMIT_REACHED,
    ;

    companion object {
        /** Server kaliti bo'yicha topadi — noma'lum qiymat `null` (ekran umumiy matn beradi). */
        fun fromKey(key: String?): RedemptionInvalidReason? = entries.firstOrNull { it.name == key }
    }
}
