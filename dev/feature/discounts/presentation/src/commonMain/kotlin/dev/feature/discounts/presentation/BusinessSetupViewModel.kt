package dev.feature.discounts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.core.domain.repository.SettingsRepository
import dev.core.domain.usecase.ObserveCurrentUserUseCase
import dev.core.common.text.TextScript
import dev.feature.discounts.domain.model.BranchWorkingHours
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.BusinessTypeInfo
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.District
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.GeoCatalog
import dev.feature.discounts.domain.model.WeekDay
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.MetroStation
import dev.feature.discounts.domain.model.Region
import dev.feature.discounts.domain.repository.RegionRepository
import dev.feature.discounts.domain.repository.PlaceSuggestion
import dev.feature.discounts.domain.usecase.CreateBranchFromPointUseCase
import dev.feature.discounts.domain.usecase.GetBusinessTypesUseCase
import dev.feature.discounts.domain.usecase.ObserveMyBusinessesUseCase
import dev.feature.discounts.domain.usecase.SaveBusinessUseCase
import dev.feature.discounts.domain.usecase.SearchPlacesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
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
    /** Bir martalik xato xabari (masalan o'chirib bo'lmadi). */
    val message: String? = null,
    /**
     * Hozir moderatsiyaga yuborilayotgan biznes id'si — o'sha kartaning tugmasi kutish
     * holatiga o'tadi. Bitta id, ro'yxat emas: yuborish tez tugaydi va bir vaqtda bir nechta
     * biznesni yuborish real oqim emas.
     */
    val submittingId: String? = null,
    /**
     * Muvaffaqiyat xabari — "yuborildi". [message] dan alohida, chunki u xato ohangida
     * (qizil banner) ko'rsatiladi.
     */
    val successMessage: String? = null,
)

class MyBusinessesViewModel(
    private val observeMyBusinesses: ObserveMyBusinessesUseCase,
    private val deleteBusiness: dev.feature.discounts.domain.usecase.DeleteBusinessUseCase,
    private val submitBusiness: dev.feature.discounts.domain.usecase.SubmitBusinessUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {

    /**
     * Ro'yxatni qayta o'qish triggeri.
     *
     * `observeMyBusinesses()` — REST ustidagi **bir martalik** oqim (`GET /v1/business/my` bir
     * marta o'qiladi va emit qilinadi), ya'ni o'zi qayta yangilanmaydi. Shu sabab o'chirgandan
     * keyin ro'yxat eski holida qolib ketardi — trigger uni qaytadan yig'adi.
     */
    private val reload = MutableStateFlow(0)

    /** O'chirish xatosi — ro'yxat bilan bir oqimda yashamaydi, shuning uchun alohida. */
    private val deleteError = MutableStateFlow<String?>(null)

    /** Moderatsiyaga yuborish holati — ham ro'yxatdan tashqarida yashaydi. */
    private val submitState = MutableStateFlow(SubmitState())

    /** [submittingId] — kutayotgan karta, [success] — "yuborildi" xabari. */
    private data class SubmitState(val submittingId: String? = null, val success: String? = null)

    init {
        // Sessiya almashsa (masalan "Biznes +" oynasidan boshqa hisobga kirilsa) ro'yxat
        // eski egasining bizneslari bilan qolib ketardi — foydalanuvchi id'si o'zgarishi
        // ro'yxatni qaytadan o'qitadi.
        viewModelScope.launch {
            observeCurrentUser()
                .map { it?.id }
                .distinctUntilChanged()
                .drop(1) // birinchi qiymat — ro'yxat allaqachon shu bilan yig'ilgan
                .collect { reload.value += 1 }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MyBusinessesUiState> =
        combine(
            reload.flatMapLatest { observeMyBusinesses() },
            deleteError,
            submitState,
        ) { businesses, error, submit ->
            MyBusinessesUiState(
                loading = false,
                businesses = businesses,
                message = error,
                submittingId = submit.submittingId,
                successMessage = submit.success,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyBusinessesUiState())

    fun delete(id: String) {
        viewModelScope.launch {
            when (val res = deleteBusiness(id)) {
                // Ro'yxatni serverdan qayta o'qiymiz — lokal o'chirish "o'chdi" deb ko'rsatib,
                // aslida serverda qolib ketishi mumkin edi.
                is Resource.Success -> {
                    deleteError.value = null
                    reload.value += 1
                }
                is Resource.Error -> deleteError.value = res.message
                Resource.Loading -> Unit
            }
        }
    }

    /**
     * Biznesni moderatsiyaga yuboradi.
     *
     * Muvaffaqiyatda ro'yxat **serverdan qayta o'qiladi**, javobdagi biznes kartaga qo'lda
     * yozilmaydi: yangi status `MODERATION_ENABLED` bayrog'iga bog'liq (`PENDING_REVIEW` yoki
     * darrov `APPROVED`) va uni ilova tomonida taxmin qilish mumkin emas.
     */
    fun submit(business: Business) {
        if (submitState.value.submittingId != null) return
        submitState.value = SubmitState(submittingId = business.id)
        viewModelScope.launch {
            when (val res = submitBusiness(business)) {
                is Resource.Success -> {
                    submitState.value = SubmitState(success = res.data.status?.label)
                    deleteError.value = null
                    reload.value += 1
                }
                is Resource.Error -> {
                    submitState.value = SubmitState()
                    deleteError.value = res.message
                }
                Resource.Loading -> submitState.value = SubmitState()
            }
        }
    }

    fun consumeMessage() {
        deleteError.value = null
        submitState.update { it.copy(success = null) }
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
    /**
     * Filial nomi ("Chilonzor filiali") — `BranchRequestDto.name` majburiy. Bo'sh qoldirilsa
     * saqlashда biznes nomi yoziladi, chunki bitta filialli bizneslar uchun alohida nom
     * so'rash ortiqcha.
     */
    val branchName: String = "",
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
    /**
     * Tuman — `LocationDto.districtId` **majburiy** va tanlangan viloyatga tegishli bo'lishi
     * shart (aks holda `422 DISTRICT_REGION_MISMATCH`). Teskari geokodlash topsa avtomatik
     * to'ladi, topa olmasa foydalanuvchi o'zi tanlaydi.
     */
    val districtId: String? = null,
    /**
     * Eng yaqin metro bekati — **ixtiyoriy** mo'ljal, faqat Toshkent shahri filiallari uchun
     * ([showMetroField]). Xaritadan joy tanlanganda `nearestMetro` dan avtomatik to'ladi.
     */
    val metroStation: String = "",
    /** Viloyat tanlash sheet'i ochiqmi (`AppBottomSheet`). */
    val regionPickerOpen: Boolean = false,
    /** Tuman tanlash sheet'i ochiqmi. */
    val districtPickerOpen: Boolean = false,
    /** Metro bekati tanlash sheet'i ochiqmi. */
    val metroPickerOpen: Boolean = false,
    /**
     * Metro bekatlari (`GET /geo/metro-stations`). Bo'sh bo'lishi mumkin — u holda maydon
     * oddiy matn kiritish bo'lib qoladi (ro'yxat majburiy emas, backend erkin matn kutadi).
     */
    val metroStations: List<MetroStation> = emptyList(),
    /**
     * Filial ish vaqti — backend yetti kunni ham kutadi (`BranchRequestDto.workingHours`),
     * shuning uchun forma odatiy jadval bilan ochiladi.
     */
    val workingHours: List<BranchWorkingHours> = BranchWorkingHours.defaultWeek(),
    /** Biznes turi tanlash sheet'i ochiqmi (`AppBottomSheet`). */
    val typePickerOpen: Boolean = false,
    val pickingOnMap: Boolean = false,
    val resolvingAddress: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<PlaceSuggestion> = emptyList(),
    val searching: Boolean = false,
    /** Kamida bir marta qidirildi — bo'sh natijani "topilmadi" deb ko'rsatish uchun. */
    val searched: Boolean = false,
    /** Qidiruv **xatosi** — "topilmadi" dan farqli: geokoderga yetib borilmadi. */
    val searchError: String? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
    /**
     * Chegara kodi (`RATE_LIMITED` — bir foydalanuvchida 5 tadan ortiq biznes) — [error]
     * bilan birga keladi. Bu forma xatosi EMAS: tuzatiladigan maydon yo'q, shuning uchun
     * ekran uning ostida nima qilish kerakligini aytadi.
     */
    val limitCode: String? = null,
    /**
     * Backend "avval profilingizdagi telefon raqamini kiriting/tasdiqlang" dedi
     * (`403 PHONE_NOT_VERIFIED`). Bunда oddiy xato matni o'rniga raqam so'raydigan oyna
     * ochiladi: raqam → `PUT /profile/me` → SMS kod. Tasdiqlangach saqlash **avtomatik
     * qaytariladi**, chunki foydalanuvchi to'ldirgan formani qayta terishi kerak emas.
     */
    val needsPhone: Boolean = false,
    /**
     * Backenddan kelgan biznes turlari (`GET /business/types?gender=`). Backend javob
     * bermasa UseCase klient katalogini beradi — ro'yxat hech qachon bo'sh qolmaydi.
     */
    val availableTypes: List<BusinessTypeInfo> = emptyList(),
    /**
     * Viloyatlar (`GET /v1/regions`). Backend javob bermasa repository klient katalogini
     * beradi — ro'yxat hech qachon bo'sh qolmaydi.
     */
    val regions: List<Region> = GeoCatalog.regions(),
) {
    val editing: Boolean get() = editId != null
    val phoneDigits: String get() = phone.filter { it.isDigit() }.take(9)
    /** Tanlangan viloyat nomi — ko'rsatish uchun. */
    val regionName: String? get() = regions.firstOrNull { it.id == regionId }?.name

    /** Tanlangan viloyatning tumanlari — tuman sheet'i shu ro'yxatdan to'ladi. */
    val districts: List<District> get() = regions.firstOrNull { it.id == regionId }?.districts.orEmpty()

    val districtName: String? get() = districts.firstOrNull { it.id == districtId }?.name

    /**
     * Metro maydoni ko'rsatiladimi — faqat Toshkent shahri tanlanganda. Metro boshqa
     * viloyatlarda yo'q, shuning uchun u yerda maydon shunchaki shovqin bo'lardi.
     */
    val showMetroField: Boolean get() = regionId == TASHKENT_CITY_REGION_ID

    val canSave: Boolean
        get() = name.isNotBlank() && phoneDigits.length == 9 && businessType != null &&
            branch != null && regionId != null && districtId != null &&
            workingHours.all { it.isValid } && !saving
}

/**
 * Biznes qo'shishда oldindan tanlanadigan tur. Kiyim-kechak — eng ko'p uchraydigan tur,
 * shuning uchun forma darrov ishlashga tayyor holatда ochiladi.
 */
private val DEFAULT_BUSINESS_TYPE = BusinessType.CLOTHING

/**
 * Toshkent shahrining `regionId` si — metro maydoni faqat shu viloyatda ko'rsatiladi.
 * Id backendда ham, klient [GeoCatalog] ida ham bir xil formatda ("TOSHKENT_SHAHRI").
 */
private const val TASHKENT_CITY_REGION_ID = "TOSHKENT_SHAHRI"

class AddBusinessViewModel(
    private val saveBusiness: SaveBusinessUseCase,
    private val searchPlaces: SearchPlacesUseCase,
    private val createBranch: CreateBranchFromPointUseCase,
    private val settings: SettingsRepository,
    private val getBusiness: dev.feature.discounts.domain.usecase.GetBusinessUseCase,
    private val getBusinessTypes: GetBusinessTypesUseCase,
    private val regionRepository: RegionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddBusinessUiState())
    val state: StateFlow<AddBusinessUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    /** Tahrirlash: mavjud biznesni yuklab, forma maydonlarini to'ldiradi. */
    fun loadForEdit(businessId: String) {
        if (_state.value.editId == businessId) return
        viewModelScope.launch {
            val biz = getBusiness(businessId)
            if (biz == null) {
                // Aks holда jim bo'sh (va "yangi biznes" sarlavhali) forma ko'rinardi — sababsiz.
                _state.update {
                    it.copy(error = "Biznes ma'lumotini yuklab bo'lmadi. Internetni tekshirib, qayta urinib ko'ring.")
                }
                return@launch
            }
            val branch = biz.branches.firstOrNull()
            _state.update {
                it.copy(
                    editId = biz.id,
                    editCreatedAt = biz.createdAt,
                    name = biz.name,
                    phone = biz.phone.removePrefix("+998"),
                    branchName = branch?.name.orEmpty(),
                    businessType = biz.businessType,
                    branch = branch,
                    regionId = branch?.regionId,
                    districtId = branch?.districtId,
                    metroStation = branch?.metroStation.orEmpty(),
                    // Eski, ish vaqtisiz saqlangan filial ham to'liq haftalik jadval bilan ochiladi.
                    workingHours = BranchWorkingHours.fullWeek(branch?.workingHours.orEmpty()),
                )
            }
        }
    }

    init {
        // Viloyat/tuman ro'yxati serverdan (filial id'lari server ro'yxatiga mos bo'lishi shart).
        viewModelScope.launch {
            val regions = regionRepository.regions()
            if (regions.isNotEmpty()) _state.update { it.copy(regions = regions) }
        }
        // Metro bekatlari — mo'ljal maydonining takliflari. Bo'sh qaytsa maydon oddiy
        // matn kiritish bo'lib qolaveradi, shuning uchun xato ko'rsatilmaydi.
        viewModelScope.launch {
            val stations = regionRepository.metroStations()
            if (stations.isNotEmpty()) _state.update { it.copy(metroStations = stations) }
        }
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

    /** Filial nomi ("Chilonzor filiali") — biznes nomidan farqli, alifbo cheklovi yo'q. */
    fun onBranchName(v: String) = _state.update { it.copy(branchName = v, error = null) }
    // Tur tanlanishi bilan sheet yopiladi — tasdiqlash tugmasi yo'q, tanlovning o'zi javob.
    fun onType(t: BusinessType) = _state.update {
        it.copy(businessType = t, typePickerOpen = false, error = null)
    }

    fun openTypePicker() = _state.update { it.copy(typePickerOpen = true) }

    fun closeTypePicker() = _state.update { it.copy(typePickerOpen = false) }

    fun openMap() = _state.update { it.copy(pickingOnMap = true) }
    fun closeMap() = _state.update {
        it.copy(
            pickingOnMap = false,
            searchQuery = "",
            searchResults = emptyList(),
            searched = false,
            searchError = null,
        )
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
            _state.update { it.copy(searching = true, searchError = null) }
            when (val res = searchPlaces(query, nearLat, nearLng)) {
                is Resource.Success -> _state.update {
                    it.copy(searchResults = res.data, searching = false, searched = true, searchError = null)
                }
                // "Topilmadi" EMAS — geokoderga yetib borilmadi. Ikkisi bir xil ko'rinmasin.
                is Resource.Error -> _state.update {
                    it.copy(searchResults = emptyList(), searching = false, searched = true, searchError = res.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun clearSearch() = _state.update {
        it.copy(searchQuery = "", searchResults = emptyList(), searching = false, searched = false)
    }

    fun setLocationFromMap(lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.update { it.copy(resolvingAddress = true) }
            val branch = createBranch(id = "biz-${Clock.System.now().toEpochMilliseconds()}", lat = lat, lng = lng)
            _state.update { state ->
                // Geokoder viloyatni topsa — avtomatik to'ldiramiz. Topa olmasa foydalanuvchi
                // qo'lda tanlagan qiymat saqlanadi (uni bekorga o'chirmaymiz).
                val regionId = branch.regionId ?: state.regionId
                // Tuman viloyatga tegishli bo'lishi shart (`422 DISTRICT_REGION_MISMATCH`):
                // geokoder bergan tuman boshqa viloyatniki bo'lsa yoki viloyat almashgan bo'lsa —
                // tanlov tozalanadi va foydalanuvchi tumanni o'zi ko'rsatadi.
                val districtId = branch.districtId ?: state.districtId
                state.copy(
                    branch = branch,
                    regionId = regionId,
                    districtId = districtId.takeIf { id ->
                        state.regions.firstOrNull { it.id == regionId }?.districts.orEmpty()
                            .any { it.id == id }
                    },
                    // Geokoder bekat topsa yozamiz; topmasa foydalanuvchi qo'lda tanlagan
                    // qiymatni o'chirmaymiz (u yangi nuqta uchun ham to'g'ri bo'lishi mumkin).
                    metroStation = branch.metroStation ?: state.metroStation,
                    resolvingAddress = false,
                    pickingOnMap = false,
                )
            }
        }
    }

    fun openRegionPicker() = _state.update { it.copy(regionPickerOpen = true) }

    fun closeRegionPicker() = _state.update { it.copy(regionPickerOpen = false) }

    // Viloyat almashsa tuman ham almashadi — eski tanlov yangi viloyatga tegishli emas.
    fun onRegion(regionId: String) = _state.update {
        it.copy(
            regionId = regionId,
            districtId = it.districtId?.takeIf { _ -> regionId == it.regionId },
            regionPickerOpen = false,
            error = null,
        )
    }

    fun openDistrictPicker() = _state.update { it.copy(districtPickerOpen = true) }

    fun closeDistrictPicker() = _state.update { it.copy(districtPickerOpen = false) }

    fun onDistrict(districtId: String) = _state.update {
        it.copy(districtId = districtId, districtPickerOpen = false, error = null)
    }

    // -----------------------------------------------------------------------
    // Metro bekati — ixtiyoriy mo'ljal (faqat Toshkent)
    // -----------------------------------------------------------------------

    fun openMetroPicker() = _state.update { it.copy(metroPickerOpen = true) }

    fun closeMetroPicker() = _state.update { it.copy(metroPickerOpen = false) }

    /**
     * Sheet'dan bekat tanlandi. Ayni bekat qayta bosilsa tanlov **bekor qilinadi** — maydon
     * ixtiyoriy, shuning uchun undan qaytish yo'li bo'lishi kerak.
     */
    fun onMetroStationPicked(name: String) = _state.update {
        it.copy(
            metroStation = if (it.metroStation == name) "" else name,
            metroPickerOpen = false,
            error = null,
        )
    }

    /**
     * Bekat qo'lda yozildi — ro'yxat yuklanmaganda maydon oddiy matn kiritish bo'ladi.
     * Bu yerда almashtirish yo'q ([onMetroStationPicked] dan farqi shu): har bosilgan harf
     * yangi qiymat, uni "tanlovni bekor qilish" deb tushunish yozishni buzardi.
     */
    fun onMetroStation(value: String) = _state.update { it.copy(metroStation = value, error = null) }

    // -----------------------------------------------------------------------
    // Ish vaqti — yetti kunning har biri alohida tahrirlanadi
    // -----------------------------------------------------------------------

    fun onDayClosed(day: WeekDay, closed: Boolean) = updateDay(day) { it.copy(isClosed = closed) }

    fun onDayOpen(day: WeekDay, value: String) = updateDay(day) { it.copy(open = formatTime(value)) }

    fun onDayClose(day: WeekDay, value: String) = updateDay(day) { it.copy(close = formatTime(value)) }

    private fun updateDay(day: WeekDay, transform: (BranchWorkingHours) -> BranchWorkingHours) =
        _state.update { state ->
            state.copy(
                workingHours = state.workingHours.map { if (it.day == day) transform(it) else it },
                error = null,
            )
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
                // Foydalanuvchi tanlagan viloyat/tuman filialga yoziladi — geokoder topgan qiymat
                // noto'g'ri bo'lsa ham, e'lon to'g'ri filtrga tushadi.
                branches = listOf(
                    branch.copy(
                        // Filial nomi backendда majburiy: bo'sh qoldirilsa biznes nomi yoziladi.
                        name = s.branchName.trim().ifBlank { s.name.trim() },
                        regionId = s.regionId ?: branch.regionId,
                        districtId = s.districtId ?: branch.districtId,
                        // Metro faqat Toshkentda mazmunli: viloyat almashtirilgan bo'lsa
                        // eski bekat qiymati filialga yozilib qolmasin.
                        metroStation = s.metroStation.trim().takeIf { it.isNotEmpty() && s.showMetroField },
                        workingHours = s.workingHours,
                    ),
                ),
                createdAt = s.editCreatedAt ?: now, // tahrirlashда asl vaqt saqlanadi
                updatedAt = now,
            )
            when (val r = saveBusiness(business)) {
                is Resource.Success -> _state.update { it.copy(saving = false, saved = true) }
                is Resource.Error ->
                    if (r.isPhoneGate()) {
                        _state.update { it.copy(saving = false, needsPhone = true) }
                    } else {
                        _state.update {
                            it.copy(
                                saving = false,
                                error = r.message,
                                limitCode = (r.error as? AppException.LimitReached)?.code,
                            )
                        }
                    }
                Resource.Loading -> Unit
            }
        }
    }

    /** Raqam so'ralgan oyna yopildi (foydalanuvchi voz kechdi) — forma joyida qoladi. */
    fun dismissPhoneGate() = _state.update { it.copy(needsPhone = false) }

    /** Raqam kiritilib SMS kod bilan tasdiqlandi — saqlashni o'zimiz qayta urinamiz. */
    fun onPhoneVerified() {
        _state.update { it.copy(needsPhone = false) }
        save()
    }
}

/**
 * Xato "avval telefon raqamini tasdiqlang" degani emasmi?
 *
 * Backend buni `403 PHONE_NOT_VERIFIED` bilan qaytaradi, lekin matn tili/shakli o'zgarishi
 * mumkin — shuning uchun typed xato ham, kalit so'zlar ham tekshiriladi. Formadagi biznes
 * telefoni xatosi bilan chalkashmasligi uchun faqat "tasdiqlash/kiritish" ma'nosidagi
 * iboralar hisobga olinadi.
 */
private fun Resource.Error.isPhoneGate(): Boolean {
    val fieldTexts = (error as? AppException.Validation)?.fields?.values?.joinToString(" ").orEmpty()
    val text = "$message $fieldTexts".lowercase()
    if ("phone_not_verified" in text) return true
    val aboutPhone = "raqam" in text || "phone" in text || "telefon" in text
    val aboutMissing = "tasdiq" in text || "verif" in text || "kirit" in text || "required" in text
    return aboutPhone && aboutMissing
}

/**
 * Klaviaturadan kelgan matnni "HH:MM" ga keltiradi: foydalanuvchi faqat raqam teradi
 * ("0930"), ikki nuqta o'zi qo'yiladi. Soat 23, daqiqa 59 bilan cheklanadi — noto'g'ri
 * vaqtni yozib bo'lmaydi, shu sabab formaда alohida xato ko'rsatish shart emas.
 */
internal fun formatTime(input: String): String {
    val digits = input.filter { it.isDigit() }.take(4)
    if (digits.isEmpty()) return ""
    val hour = digits.take(2)
    val minute = digits.drop(2)
    val cappedHour = if (hour.length == 2) hour.toInt().coerceAtMost(23).pad() else hour
    if (minute.isEmpty()) return cappedHour
    val cappedMinute = if (minute.length == 2) minute.toInt().coerceAtMost(59).pad() else minute
    return "$cappedHour:$cappedMinute"
}

private fun Int.pad(): String = toString().padStart(2, '0')
