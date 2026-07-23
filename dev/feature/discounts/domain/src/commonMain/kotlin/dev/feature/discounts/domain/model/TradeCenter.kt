package dev.feature.discounts.domain.model

/** Savdo markazi — filial shu markaz ichida joylashgan bo'lishi mumkin. */
data class TradeCenter(
    val id: String,
    val name: String,
    val slug: String,
)

/** Savdo markazi maydonining turi — forma qanday input ko'rsatishini belgilaydi. */
enum class TradeCenterFieldKind { TEXT, NUMBER }

/**
 * Savdo markazining dinamik maydoni ("Qator", "Do'kon raqami"...).
 *
 * Har bir markazning o'z to'plami bor, shuning uchun forma qattiq kodlanmaydi —
 * `GET /v1/trade-centers/{id}` bergan ro'yxat bo'yicha quriladi.
 */
data class TradeCenterField(
    val id: String,
    val label: String,
    val kind: TradeCenterFieldKind,
    val required: Boolean,
    val sortOrder: Int,
)

/** Markaz + uning maydonlari (`GET /v1/trade-centers/{id}`). */
data class TradeCenterDetail(
    val center: TradeCenter,
    val fields: List<TradeCenterField>,
)
