package dev.feature.discounts.domain.model

/** Biznes turi ichidagi bo'lim: "Pitsa", "PS5", "IELTS kurslari". */
data class ListingCategory(val key: String, val label: String)

/**
 * Turga xos maydonning kiritish usuli — backenddagi `AttributeField.type` bilan bir xil.
 */
enum class AttributeKind {
    TEXT,
    NUMBER,
    BOOLEAN,
    /** Bir nechta variant tanlanadi (masalan futbolkada mavjud razmerlar: S, M, L). */
    MULTI_SELECT,
    /** [AttributeSpec.options] dan bittasi tanlanadi. */
    SELECT,
    /** Vergul bilan ajratilgan ro'yxat: "Mozzarella, Pepperoni". */
    TAGS,
}

/**
 * Turga xos maydon tavsifi. Forma **shu ro'yxatdan dinamik quriladi** — ilovada
 * har bir biznes turi uchun alohida forma yozilmaydi.
 *
 * Manba — `GET /v1/business/types/{type}/categories` javobidagi `fields[]`
 * (backendda maydonlar **tur darajasida**: bir turdagi barcha kategoriyalar bir xil
 * ro'yxatni oladi). Internet yo'q bo'lganda [ListingCatalog] zaxira nusxani beradi.
 */
data class AttributeSpec(
    val key: String,
    val label: String,
    val kind: AttributeKind,
    val hint: String = "",
    val options: List<String> = emptyList(),
    val required: Boolean = false,
    /** Raqamli maydon uchun o'lchov birligi ("gramm", "oy", "daqiqa"). */
    val suffix: String? = null,
)
