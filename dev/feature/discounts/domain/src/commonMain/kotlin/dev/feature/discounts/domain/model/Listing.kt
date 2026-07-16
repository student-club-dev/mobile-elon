package dev.feature.discounts.domain.model

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Chegirma e'loni — Chegirmalar bo'limining markaziy modeli.
 *
 * Spetsifikatsiya: `DISCOUNTS_BUSINESS_API.md` (§3.4) va `openapi/student-clubs.json`
 * (`ListingDto`). Bu yerdagi maydonlar API bilan bir xil nom bilan atalgan — backend
 * yoqilganda mapper'lar to'g'ridan-to'g'ri mos tushadi.
 *
 * **Chegirmasiz e'lon bo'lmaydi** — [discount] majburiy.
 */
data class Listing(
    val id: String,
    val ownerId: String,
    /** Backenddagi biznes id'si. Offline rejimda `null`. */
    val businessId: String? = null,
    val businessType: BusinessType,
    /** Biznes nomi ("Chaykhana Navruz"). Offline rejimda e'lon bilan birga kiritiladi. */
    val businessName: String,
    val categoryKey: String,
    /** `categoryKey == ListingCatalog.OTHER_KEY` bo'lganda majburiy. */
    val customCategoryName: String? = null,

    val title: String,
    val description: String? = null,
    /** Rasmlar. Offline rejimda `data:` URI, backend bilan — CDN havolasi. Birinchisi — muqova. */
    val images: List<String> = emptyList(),

    val priceUnit: PriceUnit,
    /** Chegirmasiz asl narx, butun so'mda (tiyinsiz). */
    val originalPrice: Long,
    val currency: String = "UZS",

    val discount: ListingDiscount,
    val redemption: ListingRedemption,
    /**
     * Filiallar — e'lon shu manzillarning **hammasida** amal qiladi.
     * Har biri xaritadan tanlanadi, shuning uchun koordinatasi bor (qo'lda kiritilmaydi).
     */
    val branches: List<ListingBranch> = emptyList(),

    /** Epoch millisekund. */
    val validFrom: Long,
    val validTo: Long,

    /** Turga xos maydonlar — kalitlar [ListingCatalog.attributes] dan keladi. */
    val attributes: Map<String, String> = emptyMap(),
    val optionGroups: List<OptionGroup> = emptyList(),

    val status: ListingStatus = ListingStatus.DRAFT,
    val rejectionReason: String? = null,
    val viewsCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
) {
    /** Chegirmadan keyingi narx — [ListingDiscount.finalPrice] ning qisqartmasi. */
    val finalPrice: Long get() = discount.finalPrice(originalPrice)

    /** `true` — chegirma e'loni, `false` — oddiy e'lon (attributes ichidagi belgiga qarab). */
    val isDiscount: Boolean get() = attributes[ListingCatalog.REGULAR_KEY] != "1"

    /** Ro'yxatda ko'rsatiladigan kategoriya nomi. */
    val categoryLabel: String
        get() = customCategoryName
            ?: ListingCatalog.category(businessType, categoryKey)?.label
            ?: categoryKey

    /**
     * Talabaning joylashuviga eng yaqin filial va unga bo'lgan masofa.
     * Talaba koordinatasi noma'lum bo'lsa (`null`) — birinchi filial, masofasiz.
     */
    fun nearestBranch(userLat: Double?, userLng: Double?): NearestBranch? {
        if (branches.isEmpty()) return null
        if (userLat == null || userLng == null) {
            return NearestBranch(branches.first(), distanceMeters = null)
        }
        return branches
            .map { branch ->
                NearestBranch(
                    branch = branch,
                    distanceMeters = Geo.distanceMeters(userLat, userLng, branch.lat, branch.lng),
                )
            }
            .minBy { it.distanceMeters ?: Double.MAX_VALUE }
    }
}

/** Eng yaqin filial va unga bo'lgan masofa (talaba koordinatasi bo'lmasa — `null`). */
data class NearestBranch(val branch: ListingBranch, val distanceMeters: Double?) {

    /** "640 m" yoki "2.4 km". Masofa noma'lum bo'lsa — `null`. */
    fun distanceLabel(): String? {
        val meters = distanceMeters ?: return null
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            val km = meters / 1000.0
            // 10 km dan keyin o'ndan keyingi raqam ortiqcha aniqlik — "12 km" yetarli.
            if (km >= 10) "${km.toInt()} km" else "${(km * 10).toInt() / 10.0} km"
        }
    }
}

/** E'lon holati. Oqim: DRAFT → PENDING_REVIEW → ACTIVE ⇄ PAUSED. */
enum class ListingStatus(val label: String) {
    DRAFT("Qoralama"),
    PENDING_REVIEW("Moderatsiyada"),
    REJECTED("Rad etilgan"),
    SCHEDULED("Rejalashtirilgan"),
    ACTIVE("Faol"),
    PAUSED("To'xtatilgan"),
    EXPIRED("Muddati tugagan"),
    SOLD_OUT("Limit tugagan"),
    ARCHIVED("Arxivlangan"),
    ;

    /** Talabaga ko'rinadimi. */
    val isVisibleToStudents: Boolean get() = this == ACTIVE
}

/** Narx birligi — biznes turiga qarab odatiy qiymati bor ([BusinessType.defaultPriceUnit]). */
enum class PriceUnit(val label: String, val suffix: String) {
    PER_ITEM("Dona", "dona"),
    PER_HOUR("Soat", "soat"),
    PER_KG("Kilogramm", "kg"),
    PER_MONTH("Oy", "oy"),
    PER_COURSE("Kurs", "kurs"),
    PER_LESSON("Dars", "dars"),
    PER_TICKET("Chipta", "chipta"),
    PER_PERSON("Kishi", "kishi"),
    PER_SESSION("Sessiya", "sessiya"),
}

/** Chegirma turi — `value` maydonining ma'nosini belgilaydi. */
enum class DiscountType(val label: String, val valueLabel: String, val hint: String) {
    /** `value` = foiz (1..90). */
    PERCENT("Foiz chegirma", "Foiz (%)", "20"),

    /** `value` = so'mda ayiriladigan summa. */
    FIXED_AMOUNT("Summa chegirma", "Chegirma (so'm)", "10000"),

    /** `value` = talabaga beriladigan yangi narx. */
    SPECIAL_PRICE("Talaba narxi", "Yangi narx (so'm)", "40000"),

    /** 1+1 kabi aksiya — narx o'zgarmaydi, shartlar matnda. */
    FREE_ITEM("Sovg'a (1+1)", "—", ""),
}

/**
 * Chegirma. [finalPrice] — yakuniy narxni hisoblovchi yagona formula.
 *
 * Backend yoqilganda yakuniy narxni **server** hisoblaydi (klient uni yubormaydi),
 * lekin formula bir xil — shuning uchun offline rejim va server bir xil natija beradi.
 */
data class ListingDiscount(
    val type: DiscountType,
    val value: Long,
    val conditions: String? = null,
    /** `false` — chegirma qo'shimchalar narxiga (`Option.priceDelta`) tarqalmaydi. */
    val appliesToOptions: Boolean = false,
) {
    fun finalPrice(originalPrice: Long): Long = when (type) {
        DiscountType.PERCENT -> originalPrice * (100 - value) / 100
        DiscountType.FIXED_AMOUNT -> originalPrice - value
        DiscountType.SPECIAL_PRICE -> value
        DiscountType.FREE_ITEM -> originalPrice
    }.coerceAtLeast(0)

    /** Kartochkada ko'rsatiladigan yorliq: "−20%", "−10 000 so'm", "1+1". */
    fun badge(): String = when (type) {
        DiscountType.PERCENT -> "−$value%"
        DiscountType.FIXED_AMOUNT -> "−${value.formatSum()} so'm"
        DiscountType.SPECIAL_PRICE -> "${value.formatSum()} so'm"
        DiscountType.FREE_ITEM -> "1+1"
    }
}

/** Talaba chegirmani qanday ishlatadi. */
enum class RedemptionMethod(val label: String, val hint: String) {
    QR("QR kod", "Ilovada QR chiqadi, kassir skanerlaydi"),
    PROMO_CODE("Promokod", "Talaba kodni kassirga aytadi"),
    STUDENT_ID("Talaba ID", "Profilini ko'rsatadi, kod kerak emas"),
    ONLINE_LINK("Onlayn havola", "Tashqi saytda ishlatiladi"),
}

/** Chegirmani ishlatish shartlari va limitlari. */
data class ListingRedemption(
    val method: RedemptionMethod = RedemptionMethod.STUDENT_ID,
    /** Faqat [RedemptionMethod.PROMO_CODE] uchun. */
    val promoCode: String? = null,
    /** Bitta talaba necha marta ishlata oladi (`null` — cheksiz). */
    val perUserLimit: Int? = 1,
    /** Jami necha marta ishlatilishi mumkin (`null` — cheksiz). */
    val totalLimit: Int? = null,
    val usedCount: Int = 0,
)

/**
 * Filial — **xaritada tanlangan nuqta**. Koordinata majburiy: talabaga eng yaqin filialni
 * ko'rsatish uchun ([Listing.nearestBranch]) va backendga `lat`/`lng` yuborish uchun.
 *
 * [address] xaritadan teskari geokodlash bilan avtomatik to'ladi (foydalanuvchi qo'lda yozmaydi),
 * lekin tahrirlash mumkin. [name] — "Chilonzor filiali" kabi ixtiyoriy nom.
 */
data class ListingBranch(
    val id: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val name: String? = null,
    val landmark: String? = null,
    /** Teskari geokodlashdan topilgan bo'lsa — [GeoCatalog] dagi id'lar. */
    val regionId: String? = null,
    val districtId: String? = null,
) {
    /** Koordinata O'zbekiston chegarasida bo'lishi kerak (spec §6.6). */
    val hasValidCoordinates: Boolean
        get() = lat in UZ_LAT_RANGE && lng in UZ_LNG_RANGE

    /** "Chilonzor filiali — Chilonzor 9-kvartal, 42-uy" */
    fun display(): String = listOfNotNull(name?.takeIf { it.isNotBlank() }, address).joinToString(" — ")

    companion object {
        val UZ_LAT_RANGE = 37.0..46.0
        val UZ_LNG_RANGE = 55.0..74.0
    }
}

/** Koordinatalar bilan ishlash. */
object Geo {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /**
     * Ikki nuqta orasidagi masofa (metr) — haversine formulasi.
     *
     * Backend PostGIS `ST_Distance` bilan hisoblaydi; bu esa uning klient tomonidagi
     * ekvivalenti (offline rejimda va serverdan masofa kelmaganda ishlatiladi).
     */
    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = (lat2 - lat1).toRadians()
        val dLng = (lng2 - lng1).toRadians()
        val a = sin(dLat / 2).pow(2) +
            cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLng / 2).pow(2)
        return 2 * EARTH_RADIUS_METERS * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
}

/** Qo'shimcha tanlov turi. */
enum class SelectionType(val label: String) {
    SINGLE("Bittasini tanlaydi"),
    MULTIPLE("Bir nechtasini tanlaydi"),
}

/** Qo'shimcha variant: "35 sm  +12 000". */
data class OptionItem(
    val name: String,
    /** Asosiy narxga qo'shiladigan summa (manfiy ham bo'lishi mumkin). */
    val priceDelta: Long = 0,
    val isAvailable: Boolean = true,
)

/**
 * Qo'shimchalar guruhi — Yandex Eda'dagi "Hajmni tanlang" mantiqi.
 * Kafeda "Hajm", Game Club'da "Zal turi", Kiyimda "O'lcham", Texnikada "Xotira".
 */
data class OptionGroup(
    val name: String,
    val selectionType: SelectionType = SelectionType.SINGLE,
    val isRequired: Boolean = false,
    val options: List<OptionItem> = emptyList(),
)

/** "55000" → "55 000". Narxni o'qish uchun (uch xonali guruhlar, ajratgich — probel). */
fun Long.formatSum(): String {
    val s = toString()
    if (s.length <= 3) return s
    return s.reversed().chunked(3).joinToString(" ").reversed()
}
