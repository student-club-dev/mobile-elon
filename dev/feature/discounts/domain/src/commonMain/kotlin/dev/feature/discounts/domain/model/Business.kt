package dev.feature.discounts.domain.model

/**
 * Biznes — egasi **bir marta** yaratadi (nom, telefon, lokatsiya), so'ng shu biznesga
 * e'lon/chegirmalar qo'shadi. E'lonlar biznesning nomi va joylashuvini meros oladi.
 *
 * Bir egaga bitta biznes — hujjat `businesses/{ownerId}` (uid) da saqlanadi.
 */
data class Business(
    /** Hujjat id'si — hozircha ownerId (uid) bilan bir xil. */
    val id: String,
    val ownerId: String,
    val name: String,
    val phone: String,
    val businessType: BusinessType? = null,
    /**
     * Logo (`BusinessDto.logoUrl`) — `POST /v1/media/upload` (purpose `LOGO`) qaytargan ochiq
     * URL. `null` — logo qo'yilmagan, kartada tur ikonkasi ko'rsatiladi.
     */
    val logoUrl: String? = null,
    /**
     * Joylashuv(lar) — xaritadan tanlangan. E'lonlar shu filiallarni oladi.
     *
     * Bo'sh ro'yxat **ikki ma'noli**: biznes ro'yxatida u "hali so'ralmagan" degani
     * (filiallar alohida so'rov bilan, biznes ochilganda olinadi), biznes ochilganda esa —
     * haqiqatan filial yo'q degani.
     */
    val branches: List<ListingBranch> = emptyList(),
    /**
     * Onlayn biznes (`BusinessDto.isOnlineOnly`) — spec bo'yicha unda **filial ham,
     * lokatsiya ham talab qilinmaydi** (masalan onlayn kurs). E'lon formasi shunda filial
     * tanlashni majburiy qilmaydi.
     */
    val isOnlineOnly: Boolean = false,
    /** Serverdagi e'lonlar soni (`BusinessDto.listingsCount`) — ro'yxatda ko'rsatiladi. */
    val listingsCount: Int = 0,
    /**
     * Moderatsiya holati (`BusinessDto.status`) — biznes ochilganda ko'rsatiladi.
     * `null` — hali serverdan olinmagan (namuna biznes yoki ro'yxat elementi).
     */
    val status: BusinessStatus? = null,
    /**
     * Moderator ko'rsatgan rad etish sababi (`BusinessDto.rejectionReason`) — faqat
     * [BusinessStatus.REJECTED] da to'ladi. Busiz foydalanuvchi "rad etildi" degan xabarni
     * ko'rib, nimani tuzatishni bilmasdi.
     */
    val rejectionReason: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
    /** Asosiy filial (birinchi) — e'lonlar odatda shuni oladi. */
    val primaryBranch: ListingBranch? get() = branches.firstOrNull()

    /** Kiritish yakunlanganmi (nom + telefon + kamida bitta joylashuv). */
    val isComplete: Boolean
        get() = name.isNotBlank() && phone.isNotBlank() && branches.isNotEmpty()
}

/** Biznesning moderatsiya holati — serverdan (`BusinessStatusDto`). */
enum class BusinessStatus(val label: String) {
    DRAFT("Qoralama"),
    PENDING_REVIEW("Moderatsiyada"),
    APPROVED("Tasdiqlangan"),
    REJECTED("Rad etilgan"),
    BLOCKED("Bloklangan"),
    ARCHIVED("Arxivlangan"),
    ;

    /**
     * Shu holatdan moderatsiyaga yuborsa bo'ladimi (`POST /business/{id}/submit`).
     *
     * Backend faqat `DRAFT` va `REJECTED` dan o'tishga ruxsat beradi, boshqasidan
     * `409 INVALID_STATUS_TRANSITION` qaytaradi — shuning uchun tugma faqat shu ikkisida
     * ko'rsatiladi va foydalanuvchi qaytariladigan xatoga urilmaydi.
     */
    val canSubmit: Boolean get() = this == DRAFT || this == REJECTED

    companion object {
        /** Server kaliti bo'yicha topadi — noma'lum qiymat `null`. */
        fun fromKey(key: String): BusinessStatus? = entries.firstOrNull { it.name == key }
    }
}
