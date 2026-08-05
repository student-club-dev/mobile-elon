package dev.feature.discounts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.common.Resource
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.RedemptionCheck
import dev.feature.discounts.domain.usecase.ConfirmRedemptionUseCase
import dev.feature.discounts.domain.usecase.VerifyRedemptionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Kassir oqimi: talaba ko'rsatgan kodni tekshirish va chegirmani qo'llash.
 *
 * **Ikki qadam ataylab.** `verify` hech narsani o'zgartirmaydi — kassir kodni xavfsiz
 * tekshirib, talabaning ismi va qo'llanadigan narxni ko'radi; `confirm` esa foydalanishni
 * hisobga oladi va limitni kamaytiradi. Bitta tugmaga birlashtirilsa, xato bosilgan kod
 * ham darrov "ishlatilgan" bo'lib qolardi va uni qaytarib bo'lmasdi.
 */
data class RedeemUiState(
    /** Kassir oqimi ochilgan e'lon (`null` — oyna yopiq). */
    val listing: Listing? = null,
    val code: String = "",
    val verifying: Boolean = false,
    val confirming: Boolean = false,
    /**
     * Tekshiruv natijasi. `null` — hali tekshirilmagan; `isValid = false` — kod yaroqsiz va
     * sababi [RedemptionCheck.invalidReason] da.
     */
    val check: RedemptionCheck? = null,
    /**
     * Tanlangan filial — `confirm` da yuboriladi (`ConfirmRedemptionRequestDto.branchId`).
     * E'lon bitta filialda amal qilsa avtomatik tanlanadi, ko'p bo'lsa kassir ko'rsatadi.
     */
    val branchId: String? = null,
    /** Chek summasi — ixtiyoriy, faqat statistikadagi umumiy summaga qo'shiladi. */
    val amount: String = "",
    /** So'rov rad etilgan bo'lsa sababi (tarmoq, 403, chegara). */
    val error: String? = null,
    /** Chegirma qo'llandi — oyna muvaffaqiyat holatiga o'tadi. */
    val confirmed: Boolean = false,
) {
    /** E'lon filiallari — kassir qaysi biridaligini shu ro'yxatdan tanlaydi. */
    val branches: List<ListingBranch> get() = listing?.branches.orEmpty()

    /** Tasdiqlash mumkinmi: kod tekshirildi, haqiqiy va hali qo'llanmagan. */
    val canConfirm: Boolean
        get() = check?.isValid == true && !confirming && !confirmed
}

class RedeemViewModel(
    private val verifyRedemption: VerifyRedemptionUseCase,
    private val confirmRedemption: ConfirmRedemptionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RedeemUiState())
    val state: StateFlow<RedeemUiState> = _state.asStateFlow()

    /**
     * Oynani ochadi. Filial bitta bo'lsa **avtomatik tanlanadi**: kassirdan yagona variantni
     * tanlashni so'rash ortiqcha qadam bo'lardi.
     */
    fun open(listing: Listing) {
        _state.value = RedeemUiState(
            listing = listing,
            branchId = listing.branches.singleOrNull()?.id,
        )
    }

    fun close() {
        _state.value = RedeemUiState()
    }

    /**
     * Kod o'zgarganda oldingi tekshiruv natijasi **tozalanadi**: aks holda kassir yangi kod
     * yozib turib, eski koddan qolgan "haqiqiy" yozuvini ko'rib tasdiqlab yuborishi mumkin edi.
     */
    fun onCode(value: String) = _state.update {
        it.copy(code = value, check = null, error = null, confirmed = false)
    }

    fun onBranch(id: String) = _state.update { it.copy(branchId = id, error = null) }

    fun onAmount(value: String) = _state.update {
        it.copy(amount = value.filter { c -> c.isDigit() }.take(MAX_AMOUNT_DIGITS), error = null)
    }

    fun verify() {
        val listing = _state.value.listing ?: return
        if (_state.value.verifying) return
        _state.update { it.copy(verifying = true, error = null, check = null) }
        viewModelScope.launch {
            when (val res = verifyRedemption(listing.id, _state.value.code)) {
                // Yaroqsiz kod ham MUVAFFAQIYATLI javob: server "bu kod yaramaydi" dedi, bu
                // xato emas. Sababi `check.invalidReason` da va ekran uni aniq matn bilan
                // ko'rsatadi ("allaqachon foydalanilgan" / "muddati tugagan" ...).
                is Resource.Success -> _state.update { it.copy(verifying = false, check = res.data) }
                is Resource.Error -> _state.update { it.copy(verifying = false, error = res.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun confirm() {
        val s = _state.value
        val listing = s.listing ?: return
        if (!s.canConfirm) return
        _state.update { it.copy(confirming = true, error = null) }
        viewModelScope.launch {
            val res = confirmRedemption(
                listingId = listing.id,
                code = s.code,
                branchId = s.branchId,
                amount = s.amount.toLongOrNull(),
            )
            when (res) {
                is Resource.Success -> _state.update { it.copy(confirming = false, confirmed = true) }
                is Resource.Error -> _state.update { it.copy(confirming = false, error = res.message) }
                Resource.Loading -> Unit
            }
        }
    }

    private companion object {
        /**
         * Chek summasidagi maksimal raqamlar soni. 12 xona ≈ 999 milliard so'm — har qanday
         * haqiqiy chekdan katta, lekin `Long` chegarasidan uzoq (`toLongOrNull` `null`
         * qaytarib, summa jimgina yo'qolib qolmasin).
         */
        const val MAX_AMOUNT_DIGITS = 12
    }
}
