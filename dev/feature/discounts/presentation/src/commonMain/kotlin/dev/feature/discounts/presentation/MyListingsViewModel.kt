package dev.feature.discounts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.core.common.error.toAppException
import dev.core.common.network.NetworkConnectivity
import dev.core.domain.usecase.ObserveCurrentUserUseCase
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.usecase.DeleteListingUseCase
import dev.feature.discounts.domain.usecase.GetBusinessListingsUseCase
import dev.feature.discounts.domain.usecase.GetBusinessUseCase
import dev.feature.discounts.domain.usecase.ObserveMyListingsUseCase
import dev.feature.discounts.domain.usecase.ToggleListingPausedUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * "Mening e'lonlarim" ekran holati — loading/content/empty/error, offline banner.
 *
 * Biznes ochilganда e'lonlar **serverdan** paginatsiyalab olinadi (`GET /business/{id}/listings`):
 * [loading] — birinchi sahifa, [loadingMore] — cheksiz scroll'дagi keyingi sahifa, [hasNext] —
 * yana sahifa borligi. [error] `null` bo'lmasa — birinchi yuklashда xato (retry ko'rsatiladi).
 */
data class MyListingsUiState(
    val listings: List<Listing> = emptyList(),
    val loading: Boolean = true,
    /** Keyingi sahifa yuklanmoqda — ro'yxat oxiridagi spinner uchun. */
    val loadingMore: Boolean = false,
    /** Serverda yana sahifa bormi (`ListingPageDto.hasNext`) — cheksiz scroll shunga qaraydi. */
    val hasNext: Boolean = false,
    val error: AppException? = null,
    /**
     * Ochilgan biznes (`GET /business/{id}`) — sarlavhada nomi, e'lonlar soni va holati
     * ko'rsatiladi. Umumiy ro'yxatда (biznesga bog'lanmagan) `null` bo'ladi.
     */
    val business: Business? = null,
    /**
     * Har bir jimgina qayta yuklashда (e'lon qo'shib qaytганда) oshadi — ekran shu o'zgarishga
     * qarab ro'yxatni tepaga suradi (yangi e'lon "eng yangi birinchi" tartibда tepada turadi).
     */
    val reloadTick: Int = 0,
)

/** "Mening e'lonlarim" — biznes egasi yuklagan chegirmalar (barcha statuslar). */
class MyListingsViewModel(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeMyListings: ObserveMyListingsUseCase,
    private val getBusiness: GetBusinessUseCase,
    private val getBusinessListings: GetBusinessListingsUseCase,
    private val togglePaused: ToggleListingPausedUseCase,
    private val deleteListing: DeleteListingUseCase,
    private val connectivity: NetworkConnectivity,
) : ViewModel() {

    private val _state = MutableStateFlow(MyListingsUiState())
    val state: StateFlow<MyListingsUiState> = _state.asStateFlow()

    /** Ochilgan biznes id'si (`null` — umumiy ro'yxat, local bazadan). */
    private var businessId: String? = null

    /**
     * `load` allaqachon (bir marta) chaqirilganmi. Kerak: ekran bir xil `businessId` bilan qayta
     * kompozitsiya bo'lганда (`LaunchedEffect` ikkinchi marta) `load` qaytadan **ishga tushmasligi**
     * uchun — aks holда birinchi so'rov bekor qilinib, `GetBusinessUseCase`dagi `runCatching`
     * `CancellationException`ni yutib `null` qaytarardi va ekran bir zum xato ko'rsatardi.
     */
    private var idInitialized = false

    /** `GET /business/{id}` javobi — e'lonlarni to'liq mapping qilish uchun (tur, filiallar). */
    private var loadedBusiness: Business? = null

    /** Keyingi so'raladigan sahifa raqami (`loadMore` shuni oshiradi). */
    private var nextPage = 1

    /** Server yuklash / local kuzatuv job'lari — qayta yuklashda bekor qilinadi. */
    private var serverJob: Job? = null
    private var localJob: Job? = null

    /** Real-time internet holati — UI "internet yo'q" banner'ini ko'rsatishi uchun. */
    val online: StateFlow<Boolean> = connectivity.online
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), connectivity.isOnline())

    /**
     * Ekran ochilganда `businessId` bilan chaqiriladi. Biznes bo'lsa — serverdan birinchi
     * sahifani yuklaydi (avval `GET /business/{id}` bilan biznes ma'lumotini oladi); `null` —
     * umumiy ro'yxat local bazadan kuzatiladi. Bir xil id qayta yuklanmaydi.
     */
    fun load(id: String?) {
        if (idInitialized && id == businessId) return
        idInitialized = true
        businessId = id
        loadedBusiness = null
        localJob?.cancel().also { localJob = null }
        serverJob?.cancel().also { serverJob = null }
        if (id == null) observeLocal() else refresh()
    }

    /** Umumiy ro'yxat (biznesga bog'lanmagan) — local bazani kuzatadi. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeLocal() {
        localJob = viewModelScope.launch {
            observeCurrentUser()
                .flatMapLatest { user -> observeMyListings((user?.id ?: 0L).toString()) }
                .catch { e -> _state.update { it.copy(loading = false, error = e.toAppException(connectivity.isOnline())) } }
                .collect { list ->
                    _state.update { it.copy(listings = list, loading = false, error = null, hasNext = false) }
                }
        }
    }

    /** Biznes ma'lumoti + birinchi sahifani serverdan qaytadan yuklaydi. */
    private fun refresh() {
        val id = businessId ?: return
        serverJob?.cancel()
        nextPage = 1
        _state.update { it.copy(loading = true, error = null) }
        serverJob = viewModelScope.launch {
            val business = getBusiness(id)
            if (business == null) {
                // `GetBusinessUseCase.runCatching` `CancellationException`ni ham yutadi va `null`
                // qaytaradi. Job bekor qilingan bo'lsa (masalan `retry` yangi so'rov boshlaganда)
                // eski job xato holatini yozib, yangi muvaffaqiyatli natijani ustidan bosmasligi
                // uchun avval bekor qilinganini tekshiramiz.
                ensureActive()
                _state.update {
                    it.copy(loading = false, error = AppException.Unknown("Biznes ma'lumotini yuklab bo'lmadi"))
                }
                return@launch
            }
            loadedBusiness = business
            _state.update { it.copy(business = business) }
            when (val res = getBusinessListings(business, page = 1, size = PAGE_SIZE)) {
                is Resource.Success -> {
                    nextPage = 2
                    _state.update {
                        it.copy(
                            listings = res.data.items,
                            loading = false,
                            error = null,
                            hasNext = res.data.hasNext,
                        )
                    }
                }
                is Resource.Error -> _state.update {
                    it.copy(loading = false, error = res.error ?: AppException.Unknown(res.message))
                }
                Resource.Loading -> Unit
            }
        }
    }

    /**
     * Cheksiz scroll — ro'yxat oxiriga yaqinlashganда keyingi sahifani yuklaydi.
     * Yana sahifa bo'lmasa yoki allaqachon yuklanayotgan bo'lsa — hech narsa qilmaydi.
     */
    fun loadMore() {
        val business = loadedBusiness ?: return
        val s = _state.value
        if (s.loading || s.loadingMore || !s.hasNext) return
        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            when (val res = getBusinessListings(business, page = nextPage, size = PAGE_SIZE)) {
                is Resource.Success -> {
                    nextPage += 1
                    _state.update {
                        it.copy(
                            listings = it.listings + res.data.items,
                            loadingMore = false,
                            hasNext = res.data.hasNext,
                        )
                    }
                }
                // Keyingi sahifa xato bersa ro'yxatni buzmaymiz — mavjud e'lonlar qoladi.
                is Resource.Error, Resource.Loading -> _state.update { it.copy(loadingMore = false) }
            }
        }
    }

    /** Birinchi `onResumed` — dastlabki `load` allaqachon yuklagan, o'tkazib yuboramiz. */
    private var skipNextResume = true

    /**
     * Ekran qайta ko'ringanда (masalan "+ E'lon" bilan e'lon qo'shib qaytganда) ro'yxatni
     * serverdan **jimgina** yangilaydi — yangi/o'zgargan e'lon darrov ko'rinadi. Birinchi resume
     * dastlabki [load] bilan mos keladi, shuning uchun o'tkazib yuboriladi.
     */
    fun onResumed() {
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        reloadListings()
    }

    /**
     * Faqat e'lonlar ro'yxatini serverdan qayta oladi (birinchi sahifa) — biznes sarlavhasini
     * qayta so'ramaydi va yuklash spinnerini ko'rsatmaydi (mavjud ro'yxat ustidan almashtiriladi).
     */
    private fun reloadListings() {
        val business = loadedBusiness ?: return
        serverJob?.cancel()
        nextPage = 1
        serverJob = viewModelScope.launch {
            when (val res = getBusinessListings(business, page = 1, size = PAGE_SIZE)) {
                is Resource.Success -> {
                    nextPage = 2
                    _state.update {
                        it.copy(
                            listings = res.data.items,
                            hasNext = res.data.hasNext,
                            error = null,
                            reloadTick = it.reloadTick + 1,
                        )
                    }
                }
                // Xato bo'lsa mavjud ro'yxatni buzmaymiz.
                is Resource.Error, Resource.Loading -> Unit
            }
        }
    }

    fun retry() {
        if (businessId == null) observeLocal() else refresh()
    }

    fun togglePaused(listing: Listing) {
        viewModelScope.launch {
            if (togglePaused.invoke(listing) is Resource.Success) {
                // Server ro'yxati kuzatilmaydi (bir martalik so'rov), shuning uchun kartani
                // xotiradagi ro'yxatда darrov yangilaymiz.
                val next = when (listing.status) {
                    ListingStatus.ACTIVE -> ListingStatus.PAUSED
                    ListingStatus.PAUSED -> ListingStatus.ACTIVE
                    else -> return@launch
                }
                _state.update { st ->
                    st.copy(listings = st.listings.map { if (it.id == listing.id) it.copy(status = next) else it })
                }
            }
        }
    }

    fun delete(listing: Listing) {
        viewModelScope.launch {
            if (deleteListing(listing.id) is Resource.Success) {
                _state.update { st -> st.copy(listings = st.listings.filterNot { it.id == listing.id }) }
            }
        }
    }

    private companion object {
        /** Bir sahifadagi e'lonlar soni (`GET /business/{id}/listings` `size`, spec default 20). */
        const val PAGE_SIZE = 20
    }
}
