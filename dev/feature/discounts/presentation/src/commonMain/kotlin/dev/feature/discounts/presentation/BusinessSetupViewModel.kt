package dev.feature.discounts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.common.Resource
import dev.core.domain.repository.SettingsRepository
import dev.core.common.text.TextScript
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.GeoCatalog
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.repository.PlaceSuggestion
import dev.feature.discounts.domain.usecase.CreateBranchFromPointUseCase
import dev.feature.discounts.domain.usecase.GetBusinessTypesUseCase
import dev.feature.discounts.domain.usecase.ObserveMyBusinessesUseCase
import dev.feature.discounts.domain.usecase.SaveBusinessUseCase
import dev.feature.discounts.domain.usecase.SearchPlacesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

// ===========================================================================
// Bosh ekran — Mening bizneslarim ro'yxati
// ===========================================================================

data class MyBusinessesUiState(
    val loading: Boolean = true,
    val businesses: List<Business> = emptyList(),
)

class MyBusinessesViewModel(
    observeMyBusinesses: ObserveMyBusinessesUseCase,
    private val deleteBusiness: dev.feature.discounts.domain.usecase.DeleteBusinessUseCase,
) : ViewModel() {
    val state: StateFlow<MyBusinessesUiState> = observeMyBusinesses()
        .map { MyBusinessesUiState(loading = false, businesses = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyBusinessesUiState())

    fun delete(id: String) {
        viewModelScope.launch { deleteBusiness(id) }
    }
}

// ===========================================================================
// Biznes qo'shish — nom, telefon, TUR (majburiy), lokatsiya
// ===========================================================================

data class AddBusinessUiState(
    /** Tahrirlanayotgan biznes id'si (`null` — yangi biznes). */
    val editId: String? = null,
    /** Tahrirlashда — biznesning asl yaratilgan vaqti (saqlashда qayta yozilmasligi uchun). */
    val editCreatedAt: Long? = null,
    val name: String = "",
    val phone: String = "",
    val businessType: BusinessType? = null,
    /** Foydalanuvchi jinsi — tur ro'yxatini filtrlaydi (Sartaroshxona/BeautySalon). */
    val gender: Gender? = null,
    val branch: ListingBranch? = null,
    /**
     * Viloyat — xaritadan joy tanlanganda teskari geokodlashdan avtomatik to'ladi, lekin
     * foydalanuvchi tuzatishi mumkin: Nominatim viloyat nomini har doim ham [GeoCatalog]
     * bilan bog'lay olmaydi (`matchRegion` `null` qaytarishi mumkin) va bunda chegirma
     * viloyat bo'yicha filtrga tushmay qolardi.
     */
    val regionId: String? = null,
    val regionPickerOpen: Boolean = false,
    val pickingOnMap: Boolean = false,
    val resolvingAddress: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<PlaceSuggestion> = emptyList(),
    val searching: Boolean = false,
    /** Kamida bir marta qidirildi — bo'sh natijani "topilmadi" deb ko'rsatish uchun. */
    val searched: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
    /**
     * Backenddan kelgan biznes turlari (`GET /business/types?gender=`). Backend javob
     * bermasa UseCase klient katalogini beradi — ro'yxat hech qachon bo'sh qolmaydi.
     */
    val availableTypes: List<BusinessTypeInfo> = emptyList(),
) {
    val editing: Boolean get() = editId != null
    val phoneDigits: String get() = phone.filter { it.isDigit() }.take(9)
    /** Tanlangan viloyat nomi — ko'rsatish uchun. */
    val regionName: String? get() = GeoCatalog.region(regionId)?.name

    val canSave: Boolean
        get() = name.isNotBlank() && phoneDigits.length == 9 && businessType != null &&
            branch != null && regionId != null && !saving
}

/**
 * Biznes qo'shishда oldindan tanlanadigan tur. Kiyim-kechak — eng ko'p uchraydigan tur,
 * shuning uchun forma darrov ishlashga tayyor holatда ochiladi.
 */
private val DEFAULT_BUSINESS_TYPE = BusinessType.CLOTHING

class AddBusinessViewModel(
    private val saveBusiness: SaveBusinessUseCase,
    private val searchPlaces: SearchPlacesUseCase,
    private val createBranch: CreateBranchFromPointUseCase,
    private val settings: SettingsRepository,
    private val getBusiness: dev.feature.discounts.domain.usecase.GetBusinessUseCase,
    private val getBusinessTypes: GetBusinessTypesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AddBusinessUiState())
    val state: StateFlow<AddBusinessUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    /** Tahrirlash: mavjud biznesni yuklab, forma maydonlarini to'ldiradi. */
    fun loadForEdit(businessId: String) {
        if (_state.value.editId == businessId) return
        viewModelScope.launch {
            val biz = getBusiness(businessId) ?: return@launch
            _state.update {
                it.copy(
                    editId = biz.id,
                    editCreatedAt = biz.createdAt,
                    name = biz.name,
                    phone = biz.phone.removePrefix("+998"),
                    businessType = biz.businessType,
                    branch = biz.branches.firstOrNull(),
                    regionId = biz.branches.firstOrNull()?.regionId,
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            settings.observeValue(SettingsRepository.KEY_GENDER).collect { code ->
                val g = when (code) {
                    "MALE" -> Gender.MALE
                    "FEMALE" -> Gender.FEMALE
                    else -> null
                }
                _state.update { it.copy(gender = g) }
                loadTypes(g)
            }
        }
    }

    /** Turlarni backenddan oladi (xato bo'lsa UseCase fake beradi). */
    private fun loadTypes(gender: Gender?) {
        viewModelScope.launch {
            val types = getBusinessTypes(gender)
            _state.update { state ->
                state.copy(
                    availableTypes = types,
                    // Yangi biznesда tur oldindan tanlangan bo'ladi — foydalanuvchi ko'pincha
                    // uni o'zgartirmaydi va bo'sh tanlov saqlashga to'sqinlik qilardi.
                    // Tahrirlashда yoki foydalanuvchi allaqachon tanlaganда tegilmaydi.
                    businessType = state.businessType
                        ?: types.firstOrNull { it.type == DEFAULT_BUSINESS_TYPE }?.type
                        ?: types.firstOrNull()?.type,
                )
            }
        }
    }

    /**
     * Biznes nomi. Maydon `AppFieldType.LatinText` bilan kelgani uchun kirill bu yerga
     * yetib kelmaydi; `stripCyrillic` — dasturiy to'ldirishlar (backenddan prefill) uchun
     * qo'shimcha himoya.
     */
    fun onName(v: String) = _state.update {
        it.copy(name = TextScript.stripCyrillic(v), error = null)
    }
    fun onPhone(v: String) = _state.update { it.copy(phone = v.filter { c -> c.isDigit() }.take(9), error = null) }
    fun onType(t: BusinessType) = _state.update { it.copy(businessType = t, error = null) }

    fun openMap() = _state.update { it.copy(pickingOnMap = true) }
    fun closeMap() = _state.update {
        it.copy(pickingOnMap = false, searchQuery = "", searchResults = emptyList(), searched = false)
    }

    /**
     * [nearLat]/[nearLng] — xaritaning ko'rinib turgan markazi; natijalar shu atrofdagilarga
     * yaqinlashtiriladi. Qidiruvdan keyin `searched` yoqiladi, shunda UI bo'sh natijani
     * "hali qidirilmagan" holatidan ajrata oladi.
     */
    fun onSearchQuery(query: String, nearLat: Double? = null, nearLng: Double? = null) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.trim().length < SearchPlacesUseCase.MIN_QUERY_LENGTH) {
            _state.update { it.copy(searchResults = emptyList(), searching = false, searched = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _state.update { it.copy(searching = true) }
            val results = searchPlaces(query, nearLat, nearLng)
            _state.update { it.copy(searchResults = results, searching = false, searched = true) }
        }
    }

    fun clearSearch() = _state.update {
        it.copy(searchQuery = "", searchResults = emptyList(), searching = false, searched = false)
    }

    fun setLocationFromMap(lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.update { it.copy(resolvingAddress = true) }
            val branch = createBranch(id = "biz-${Clock.System.now().toEpochMilliseconds()}", lat = lat, lng = lng)
            _state.update {
                it.copy(
                    branch = branch,
                    // Geokoder viloyatni topsa — avtomatik to'ldiramiz. Topa olmasa foydalanuvchi
                    // qo'lda tanlagan qiymat saqlanadi (uni bekorga o'chirmaymiz).
                    regionId = branch.regionId ?: it.regionId,
                    resolvingAddress = false,
                    pickingOnMap = false,
                )
            }
        }
    }

    fun openRegionPicker() = _state.update { it.copy(regionPickerOpen = true) }

    fun closeRegionPicker() = _state.update { it.copy(regionPickerOpen = false) }

    fun onRegion(regionId: String) = _state.update {
        it.copy(regionId = regionId, regionPickerOpen = false, error = null)
    }

    fun save() {
        val s = _state.value
        val branch = s.branch
        val type = s.businessType
        if (!s.canSave || branch == null || type == null) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val business = Business(
                id = s.editId ?: "", // tahrirlashда — mavjud id (yangilanadi), aks holda yangi
                ownerId = "",
                name = s.name.trim(),
                phone = "+998${s.phoneDigits}",
                businessType = type,
                // Foydalanuvchi tanlagan viloyat filialga yoziladi — geokoder topgan qiymat
                // noto'g'ri bo'lsa ham, e'lon to'g'ri viloyat filtriga tushadi.
                branches = listOf(branch.copy(regionId = s.regionId ?: branch.regionId)),
                createdAt = s.editCreatedAt ?: now, // tahrirlashда asl vaqt saqlanadi
                updatedAt = now,
            )
            when (val r = saveBusiness(business)) {
                is Resource.Success -> _state.update { it.copy(saving = false, saved = true) }
                is Resource.Error -> _state.update { it.copy(saving = false, error = r.message) }
                Resource.Loading -> Unit
            }
        }
    }
}
