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
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
    /** Asosiy filial (birinchi) — e'lonlar odatda shuni oladi. */
    val primaryBranch: ListingBranch? get() = branches.firstOrNull()

    /** Kiritish yakunlanganmi (nom + telefon + kamida bitta joylashuv). */
    val isComplete: Boolean
        get() = name.isNotBlank() && phone.isNotBlank() && branches.isNotEmpty()
}
