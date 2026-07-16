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
    /** Joylashuv(lar) — xaritadan tanlangan. E'lonlar shu filiallarni oladi. */
    val branches: List<ListingBranch> = emptyList(),
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
    /** Asosiy filial (birinchi) — e'lonlar odatda shuni oladi. */
    val primaryBranch: ListingBranch? get() = branches.firstOrNull()

    /** Kiritish yakunlanganmi (nom + telefon + kamida bitta joylashuv). */
    val isComplete: Boolean
        get() = name.isNotBlank() && phone.isNotBlank() && branches.isNotEmpty()
}
