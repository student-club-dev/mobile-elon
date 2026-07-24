package dev.feature.discounts.domain.model

/**
 * Biznes e'lonlarining bitta sahifasi (`GET /business/{id}/listings` → `ListingPageDto`).
 *
 * Server paginatsiyalab qaytaradi (yangi birinchi). [hasNext] — keyingi sahifa bormi;
 * ekran cheksiz scroll'да shunga qarab [page]+1 ni so'raydi.
 */
data class ListingPage(
    val items: List<Listing>,
    val page: Int,
    val size: Int,
    val total: Long,
    val hasNext: Boolean,
) {
    companion object {
        /** Bo'sh sahifa — offline zaxira yoki natijasiz javob uchun. */
        val EMPTY = ListingPage(items = emptyList(), page = 1, size = 0, total = 0, hasNext = false)
    }
}
