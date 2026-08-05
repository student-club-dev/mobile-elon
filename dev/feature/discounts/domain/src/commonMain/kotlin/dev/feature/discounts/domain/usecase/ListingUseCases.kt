package dev.feature.discounts.domain.usecase

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingError
import dev.feature.discounts.domain.model.ListingPage
import dev.feature.discounts.domain.model.ListingStats
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.ListingValidator
import dev.feature.discounts.domain.model.Redemption
import dev.feature.discounts.domain.model.RedemptionCheck
import dev.feature.discounts.domain.model.RedemptionPage
import dev.feature.discounts.domain.repository.GeoRepository
import dev.feature.discounts.domain.repository.PlaceSuggestion
import dev.feature.discounts.domain.repository.ListingRepository
import kotlinx.coroutines.flow.Flow

/** Biznes egasining e'lonlari (barcha statuslar) — "Mening e'lonlarim" ekrani. */
class ObserveMyListingsUseCase(private val repository: ListingRepository) {
    operator fun invoke(ownerId: String): Flow<List<Listing>> = repository.observeMyListings(ownerId)
}

/**
 * Biznesning e'lonlarini serverdan paginatsiyalab oladi ("Mening e'lonlarim" biznes ochilganда).
 * Sahifa raqami [page] dan boshlab; [ListingPage.hasNext] keyingi sahifa borligini bildiradi.
 */
class GetBusinessListingsUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(
        business: Business,
        status: ListingStatus? = null,
        categoryKey: String? = null,
        page: Int = 1,
        size: Int = 20,
    ): Resource<ListingPage> = repository.listForBusiness(business, status, categoryKey, page, size)
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

        /**
         * Server rad etdi. [limitCode] — chegara kodi (`LISTING_LIMIT_REACHED`,
         * `RATE_LIMITED`), bo'lsa: bu holatda tuzatiladigan maydon yo'q, shuning uchun ekran
         * xatoni forma xatosi sifatida emas, nima qilish kerakligini aytadigan maslahat bilan
         * ko'rsatadi. Boshqa xatolarda `null`.
         */
        data class Failed(val message: String, val limitCode: String? = null) : Result
    }

    /**
     * [requireBranch] — biznes filial taklif qiladimi. Onlayn biznesda (`isOnlineOnly`) yoki
     * filialsiz biznesda `false`: bo'sh `branchIds` backend uchun to'g'ri (= barcha faol
     * filiallar), shuning uchun e'lonni to'sib qo'yish noo'rin bo'lardi.
     */
    suspend operator fun invoke(
        listing: Listing,
        requireBranch: Boolean = true,
        isEdit: Boolean = false,
    ): Result {
        val errors = ListingValidator.validate(listing, requireBranch = requireBranch)
        if (errors.isNotEmpty()) return Result.Invalid(errors)

        // Tahrirlash — `PUT /listings/{id}` (mavjudini yangilaydi); yangi e'lon — yaratib
        // moderatsiyaga yuboradi. Ilgari tahrirlashда ham yaratilardi (serverда dublikat).
        val res = if (isEdit) repository.update(listing) else repository.submit(listing)
        return when (res) {
            is Resource.Success -> Result.Success(res.data)
            is Resource.Error -> Result.Failed(
                message = res.message,
                limitCode = (res.error as? AppException.LimitReached)?.code,
            )
            Resource.Loading -> Result.Failed("E'lonni yuborib bo'lmadi")
        }
    }
}

/**
 * E'lonni to'xtatib turish / qayta yoqish (ACTIVE ⇄ PAUSED).
 *
 * **Serverdagi yangi holatni** qaytaradi, `Unit` emas: `activate` boshlanish sanasi kelajakda
 * bo'lgan e'lonni `ACTIVE` emas, `SCHEDULED` qiladi va kartada aynan shu ko'rinishi kerak.
 */
class ToggleListingPausedUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(listing: Listing): Resource<ListingStatus> = when (listing.status) {
        ListingStatus.ACTIVE -> repository.setPaused(listing.id, paused = true)
        ListingStatus.PAUSED -> repository.setPaused(listing.id, paused = false)
        else -> Resource.Error("Bu holatda to'xtatib bo'lmaydi: ${listing.status.label}")
    }
}

/**
 * Moderatsiyadagi e'lonni qaytarib oladi — u qoralamaga tushadi va tahrirlash mumkin bo'ladi.
 *
 * Faqat `PENDING_REVIEW` da mazmunli: backend boshqa holatdan `409` qaytaradi, shuning uchun
 * so'rov yuborilmasdan oldin to'xtatiladi.
 */
class WithdrawListingUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(listing: Listing): Resource<ListingStatus> =
        if (listing.status == ListingStatus.PENDING_REVIEW) {
            repository.withdraw(listing.id)
        } else {
            Resource.Error("Faqat tekshiruvdagi e'lonni qaytarib olish mumkin")
        }
}

/**
 * E'londan nusxa oladi — server yangi **qoralama** yaratadi.
 *
 * [business] kerak: server javobida biznes nomi/turi va to'liq filiallar yo'q (faqat
 * `branchIds`), shuning uchun to'liq [Listing] ni yig'ish uchun ochilgan biznes konteksti
 * bo'lishi shart ([GetBusinessListingsUseCase] bilan bir xil sabab).
 */
class DuplicateListingUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(listing: Listing, business: Business): Resource<Listing> =
        repository.duplicate(listing.id, business)
}

/** E'lon statistikasi — ko'rishlar, saqlanganlar, foydalanishlar, konversiya, summa. */
class GetListingStatsUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(id: String): Resource<ListingStats> = repository.stats(id)
}

/** Foydalanishlar tarixi (kim, qachon, qancha summaga). */
class GetListingRedemptionsUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(id: String, page: Int = 1, size: Int = 20): Resource<RedemptionPage> =
        repository.redemptions(id, page, size)
}

/**
 * Kassir: talaba ko'rsatgan kodni tekshiradi. Bu qadam hech narsani o'zgartirmaydi, shuning
 * uchun uni xohlagancha takrorlash mumkin — chegirma faqat [ConfirmRedemptionUseCase] da
 * hisobga olinadi.
 *
 * Bo'sh kod serverga yuborilmaydi: javob baribir `INVALID_CODE` bo'lardi.
 */
class VerifyRedemptionUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(listingId: String, code: String): Resource<RedemptionCheck> {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return Resource.Error("Kodni kiriting")
        return repository.verifyRedemption(listingId, trimmed)
    }
}

/**
 * Kassir: chegirmani qo'llaydi va foydalanishni hisobga oladi.
 *
 * [amount] — kassa chekining summasi, **ixtiyoriy**: u faqat statistikadagi umumiy summaga
 * qo'shiladi va chegirmaning o'ziga ta'sir qilmaydi. Nol yoki manfiy qiymat yuborilmaydi —
 * u "kiritilmagan" bilan bir xil ma'noni beradi, lekin hisobotni buzardi.
 */
class ConfirmRedemptionUseCase(private val repository: ListingRepository) {
    suspend operator fun invoke(
        listingId: String,
        code: String,
        branchId: String? = null,
        amount: Long? = null,
    ): Resource<Redemption> {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return Resource.Error("Kodni kiriting")
        return repository.confirmRedemption(
            id = listingId,
            code = trimmed,
            branchId = branchId,
            amount = amount?.takeIf { it > 0 },
        )
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
            // Toshkentda bo'lsa metro mo'ljali ham to'ladi (`nearestMetro`); boshqa shaharda
            // yoki bekat 3 km dan uzoq bo'lsa `null` keladi va maydon bo'sh qoladi.
            metroStation = resolved?.metroStation,
        )
    }

    /** Koordinatani o'qish uchun qisqartiradi (5 xona ≈ 1 metr aniqlik). */
    private fun Double.round5(): Double = kotlin.math.round(this * 100_000) / 100_000
}
