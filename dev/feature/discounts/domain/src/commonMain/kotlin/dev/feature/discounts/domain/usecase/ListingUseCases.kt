package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.feature.discounts.domain.model.AttributeSpec
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingError
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.ListingValidator
import dev.feature.discounts.domain.repository.GeoRepository
import dev.feature.discounts.domain.repository.PlaceSuggestion
import dev.feature.discounts.domain.repository.ListingRepository
import kotlinx.coroutines.flow.Flow

/** Biznes egasining e'lonlari (barcha statuslar) — "Mening e'lonlarim" ekrani. */
class ObserveMyListingsUseCase(private val repository: ListingRepository) {
    operator fun invoke(ownerId: String): Flow<List<Listing>> = repository.observeMyListings(ownerId)
}

/** Qoralama sifatida saqlaydi — validatsiyasiz (yarim to'ldirilgan forma ham saqlanadi). */
class SaveDraftUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(listing: Listing): Resource<Listing> =
        repository.save(listing.copy(status = ListingStatus.DRAFT))
}

/**
 * E'lonni publish qiladi. **Avval validatsiya** — xato bo'lsa masofaviy manbaga bormaydi.
 * Xatolar maydonlarga bog'langan holda qaytadi ([ListingError]), UI ularni forma ostida ko'rsatadi.
 */
class PublishListingUseCase(private val repository: ListingRepository) {

    sealed interface Result {
        data class Success(val listing: Listing) : Result
        data class Invalid(val errors: List<ListingError>) : Result
        data class Failed(val message: String) : Result
    }

    /**
     * [fields] — formadagi maydonlar (serverdan kelgan kategoriya javobidan). Berilmasa
     * validator zaxira katalogga tushadi.
     *
     * [requireBranch] — biznes filial taklif qiladimi. Onlayn biznesda (`isOnlineOnly`) yoki
     * filialsiz biznesda `false`: bo'sh `branchIds` backend uchun to'g'ri (= barcha faol
     * filiallar), shuning uchun e'lonni to'sib qo'yish noo'rin bo'lardi.
     */
    suspend operator fun invoke(
        listing: Listing,
        fields: List<AttributeSpec>? = null,
        requireBranch: Boolean = true,
    ): Result {
        val errors = fields
            ?.let { ListingValidator.validate(listing, it, requireBranch) }
            ?: ListingValidator.validate(listing, requireBranch = requireBranch)
        if (errors.isNotEmpty()) return Result.Invalid(errors)

        return when (val res = repository.submit(listing)) {
            is Resource.Success -> Result.Success(res.data)
            is Resource.Error -> Result.Failed(res.message)
            Resource.Loading -> Result.Failed("E'lonni yuborib bo'lmadi")
        }
    }
}

/** E'lonni to'xtatib turish / qayta yoqish (ACTIVE ⇄ PAUSED). */
class ToggleListingPausedUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(listing: Listing): Resource<Unit> {
        val next = when (listing.status) {
            ListingStatus.ACTIVE -> ListingStatus.PAUSED
            ListingStatus.PAUSED -> ListingStatus.ACTIVE
            else -> return Resource.Error("Bu holatda to'xtatib bo'lmaydi: ${listing.status.label}")
        }
        return repository.updateStatus(listing.id, next)
    }
}

class DeleteListingUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> = repository.delete(id)
}

/**
 * E'lon rasmini yuklaydi. Hajm [MAX_IMAGE_BYTES] dan oshsa masofaviy manbaga bormaydi —
 * spec ham 5 MB chegara qo'yadi.
 */
class UploadListingImageUseCase(private val repository: ListingRepository) {

    suspend operator fun invoke(bytes: ByteArray, fileName: String): Resource<String> {
        if (bytes.isEmpty()) return Resource.Error("Rasm bo'sh")
        if (bytes.size > MAX_IMAGE_BYTES) return Resource.Error("Rasm juda katta (maks. 5 MB)")
        return repository.uploadImage(bytes, fileName)
    }

    companion object {
        const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
    }
}

/** Tahrirlash uchun mavjud e'lonni yuklaydi. */
class GetListingUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(id: String): Listing? = repository.byId(id)
}

/**
 * Joy qidirish (xaritadagi qidiruv maydoni). Juda qisqa so'rov yuborilmaydi — Nominatim
 * uchun ham, foydalanuvchi uchun ham ma'nosiz natijalar chiqadi.
 */
class SearchPlacesUseCase(private val geoRepository: GeoRepository) {

    /**
     * [nearLat]/[nearLng] — xaritaning joriy markazi. Berilsa natijalar shu atrofga
     * yaqinlashtiriladi (qarang: [GeoRepository.search]).
     */
    /**
     * Xato **yutilmaydi**. Ilgari u bo'sh ro'yxatga aylanardi va ekran "Hech narsa topilmadi"
     * deb ko'rsatardi — geokoderga umuman yetib borilmagan holatda ham. Foydalanuvchi esa
     * qidiruv so'zini o'zgartirib ovora bo'lardi.
     */
    suspend operator fun invoke(
        query: String,
        nearLat: Double? = null,
        nearLng: Double? = null,
    ): Resource<List<PlaceSuggestion>> {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) return Resource.Success(emptyList())
        return geoRepository.search(trimmed, nearLat, nearLng)
    }

    companion object {
        const val MIN_QUERY_LENGTH = 3
    }
}

/**
 * Xaritada tanlangan nuqtadan filial yaratadi: manzil teskari geokodlash bilan avtomatik
 * to'ladi. Geokodlash ishlamasa (internet yo'q) — filial baribir yaratiladi, manzil o'rniga
 * koordinata yoziladi va foydalanuvchi uni tahrirlay oladi. Nuqta yo'qolib qolmasligi kerak.
 */
class CreateBranchFromPointUseCase(private val geoRepository: GeoRepository) {

    suspend operator fun invoke(id: String, lat: Double, lng: Double): ListingBranch {
        val resolved = (geoRepository.reverseGeocode(lat, lng) as? Resource.Success)?.data
        return ListingBranch(
            id = id,
            lat = lat,
            lng = lng,
            address = resolved?.address?.takeIf { it.isNotBlank() }
                ?: "${lat.round5()}, ${lng.round5()}",
            regionId = resolved?.regionId,
            districtId = resolved?.districtId,
        )
    }

    /** Koordinatani o'qish uchun qisqartiradi (5 xona ≈ 1 metr aniqlik). */
    private fun Double.round5(): Double = kotlin.math.round(this * 100_000) / 100_000
}
