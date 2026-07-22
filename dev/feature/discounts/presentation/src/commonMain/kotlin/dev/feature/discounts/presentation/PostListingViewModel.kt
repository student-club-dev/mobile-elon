package dev.feature.discounts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.common.Resource
import dev.core.common.text.TextScript
import dev.core.domain.repository.SettingsRepository
import dev.core.domain.usecase.ObserveCurrentUserUseCase
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.CategoryInfo
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.ListingDiscount
import dev.feature.discounts.domain.model.ListingError
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.domain.model.ListingBranch
import dev.feature.discounts.domain.model.ListingRedemption
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.ListingValidator
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.RedemptionMethod
import dev.feature.discounts.domain.repository.PlaceSuggestion
import dev.feature.discounts.domain.usecase.CreateBranchFromPointUseCase
import dev.feature.discounts.domain.usecase.GetCategoriesUseCase
import dev.feature.discounts.domain.usecase.GetListingUseCase
import dev.feature.discounts.domain.usecase.PublishListingUseCase
import dev.feature.discounts.domain.usecase.SaveDraftUseCase
import dev.feature.discounts.domain.usecase.SearchPlacesUseCase
import dev.feature.discounts.domain.usecase.UploadListingImageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/** E'lon qo'yish oqimi: avval biznes turi, keyin forma. */
enum class PostListingStep { TYPE, FORM }

/**
 * E'lon qo'yish formasining holati. Barcha raqamli maydonlar **matn** ko'rinishida —
 * foydalanuvchi yozayotganda qisman kiritishga yo'l qo'yish uchun ("5" → "55" → "55000").
 * Domen modeliga o'girish [PostListingViewModel.buildListing] da bo'ladi.
 */
data class PostListingUiState(
    val step: PostListingStep = PostListingStep.TYPE,
    val businessType: BusinessType? = null,
    /** Foydalanuvchi jinsi — biznes turlari va kategoriyalar shunga qarab moslanadi. */
    val gender: Gender? = null,
    /** Kiyim-kechak e'loni jinsi — sotuvchi har e'lonда tanlaydi (erkak/ayol kiyimi). */
    val listingGender: Gender? = null,

    val businessName: String = "",
    /**
     * Backenddan kelgan kategoriyalar + har birining maydonlari (`fields` ичида `options`).
     * Backend javob bermasa UseCase klient katalogini beradi — ro'yxat bo'sh qolmaydi.
     */
    val categoryList: List<CategoryInfo> = emptyList(),
    /**
     * Biznesni yuklab bo'lmadi (internet/xato/o'chirilgan) — tur meros olinmaydi. Shunда forma
     * o'rniga xato + "Qayta urinish"/"Orqaga" ko'rsatiladi (cheksiz spinner tuzog'i bo'lmasin).
     */
    val loadError: String? = null,
    val categoryKey: String = "",
    val customCategoryName: String = "",
    /** Kategoriyaga xos maydonlar qiymatlari (ListingCatalog.categoryAttributes kalitlari). */
    val attributeValues: Map<String, String> = emptyMap(),
    /**
     * `true` — chegirma e'loni (2 narx: oldingi + hozirgi), `false` — oddiy e'lon (1 narx).
     * Odatiy holat — **oddiy e'lon**: ko'pchilik e'lon chegirmasiz joylanadi, chegirma esa
     * ataylab tanlanadi.
     */
    val isDiscount: Boolean = false,
    /** Rejim tashqaridan (tab) belgilangan — E'lon turi tanlovi yashiriladi. */
    val modeLocked: Boolean = false,

    val title: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val uploadingImage: Boolean = false,

    val priceUnit: PriceUnit = PriceUnit.PER_ITEM,
    val originalPrice: String = "",

    // Chegirma endi faqat "hozirgi narx" ko'rinishida — foiz/boshqa turlar yo'q.
    val discountType: DiscountType = DiscountType.SPECIAL_PRICE,
    val discountValue: String = "",
    val conditions: String = "",

    val redemptionMethod: RedemptionMethod = RedemptionMethod.STUDENT_ID,
    val promoCode: String = "",
    /** Aloqa telefoni — soddalashtirilgan formada so'raladi. */
    val contactPhone: String = "",

    /** Filiallar — har biri xaritadan tanlangan (koordinatasi bor). */
    val branches: List<ListingBranch> = emptyList(),
    /** Xarita ochiqmi (yangi filial belgilash uchun). */
    val pickingOnMap: Boolean = false,
    /** Xaritadan nuqta tanlandi, manzil aniqlanmoqda. */
    val resolvingAddress: Boolean = false,

    /** Xaritadagi qidiruv. */
    val searchQuery: String = "",
    val searchResults: List<PlaceSuggestion> = emptyList(),
    val searching: Boolean = false,
    /** Kamida bir marta qidirildi — bo'sh natijani "topilmadi" deb ko'rsatish uchun. */
    val searched: Boolean = false,

    val durationDays: Int = 30,

    val errors: List<ListingError> = emptyList(),
    val submitting: Boolean = false,
    val published: Boolean = false,
    /** Bir martalik xabar (masalan rasm yuklashdagi xato). */
    val message: String? = null,
    val editing: Boolean = false,
) {
    /** Live hisoblanadigan yakuniy narx — foydalanuvchi yozayotganda ko'rinadi. */
    val finalPrice: Long
        get() = ListingDiscount(discountType, discountValue.toLongOrNull() ?: 0)
            .finalPrice(originalPrice.toLongOrNull() ?: 0)

    fun errorFor(field: ListingField): String? = errors.firstOrNull { it.field == field }?.message
}

class PostListingViewModel(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val publishListing: PublishListingUseCase,
    private val saveDraft: SaveDraftUseCase,
    private val uploadImage: UploadListingImageUseCase,
    private val getListing: GetListingUseCase,
    private val createBranch: CreateBranchFromPointUseCase,
    private val searchPlaces: SearchPlacesUseCase,
    private val settings: SettingsRepository,
    private val getBusiness: dev.feature.discounts.domain.usecase.GetBusinessUseCase,
    private val getCategories: GetCategoriesUseCase,
) : ViewModel() {

    /** E'lon tegishli biznes id'si (biznesdan meros olinganda yoki tahrirlashda). */
    private var currentBusinessId: String? = null

    private val user = observeCurrentUserUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _state = MutableStateFlow(PostListingUiState())

    init {
        // Jinsni local settings'dan kuzatamiz — biznes turlari/kategoriyalar shunga moslanadi.
        viewModelScope.launch {
            settings.observeValue(SettingsRepository.KEY_GENDER).collect { code ->
                val g = when (code) {
                    "MALE" -> Gender.MALE
                    "FEMALE" -> Gender.FEMALE
                    else -> null
                }
                _state.update { it.copy(gender = g) }
                loadCategories()
            }
        }
    }

    /**
     * Kategoriyalarni backenddan oladi (`GET /business/types/{type}/categories?gender=`).
     * Javob ичида har kategoriyaning maydonlari ham keladi, shuning uchun kategoriya
     * tanlanganда yangi so'rov ketmaydi. Backend ishlamasa — UseCase klient katalogini beradi.
     */
    private fun loadCategories() {
        val s = _state.value
        val type = s.businessType ?: return
        // Kiyimда jins e'lonда tanlanadi, boshqa turlarда profildan keladi.
        val g = if (type == BusinessType.CLOTHING) s.listingGender else s.gender
        viewModelScope.launch {
            val list = getCategories(type, g)
            _state.update { it.copy(categoryList = list) }
        }
    }
    val state: StateFlow<PostListingUiState> = _state.asStateFlow()

    /** Tahrirlanayotgan e'lonning id/yaratilgan vaqti — publish o'shani upsert qiladi. */
    private var editingId: String? = null
    private var editingCreatedAt: Long? = null
    private var editingBusinessId: String? = null

    // -----------------------------------------------------------------------
    // Tur va kategoriya
    // -----------------------------------------------------------------------

    /** Kiyim-kechak e'loni uchun jins (erkak/ayol kiyimi). Kategoriyalar shunga moslanadi. */
    fun onListingGender(g: Gender) {
        _state.update { it.copy(listingGender = g, categoryKey = "") }
        loadCategories() // erkak/ayol kategoriyalari boshqacha
    }

    /**
     * E'lon tanlangan biznesga tegishli — tur, nom va lokatsiya biznesdan **meros** olinadi.
     * Biznes turi bor bo'lgani uchun tur tanlash grid'i o'tkazib yuboriladi (forma darrov ochiladi).
     */
    fun prefillFromBusiness(businessId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loadError = null) }
            // Bo'sh id — navigatsiya argumenti yo'qolган; so'rov qilmasdan xato beramiz.
            val biz = if (businessId.isBlank()) null else getBusiness(businessId)
            if (biz == null) {
                _state.update {
                    it.copy(loadError = "Biznes ma'lumotini yuklab bo'lmadi. Internetni tekshirib, qayta urinib ko'ring.")
                }
                return@launch
            }
            currentBusinessId = biz.id
            _state.update {
                it.copy(
                    loadError = null,
                    businessType = biz.businessType,
                    step = PostListingStep.FORM,
                    priceUnit = biz.businessType?.defaultPriceUnit ?: it.priceUnit,
                    businessName = biz.name,
                    branches = biz.branches,
                    categoryKey = "",
                )
            }
            loadCategories()
        }
    }

    // Kategoriya o'zgarsa — kategoriyaга xos maydonlar boshqacha, shuning uchun tozalanadi.
    fun onCategory(key: String) = _state.update { it.copy(categoryKey = key, attributeValues = emptyMap()) }

    /** Kategoriyaga xos maydon qiymatini yozadi (bo'sh bo'lsa o'chiradi). */
    fun onAttribute(key: String, value: String) = _state.update {
        it.copy(attributeValues = if (value.isBlank()) it.attributeValues - key else it.attributeValues + (key to value))
    }

    /** E'lon rejimi: chegirma yoki oddiy. */
    fun onListingMode(discount: Boolean) = _state.update { it.copy(isDiscount = discount) }

    /** Yaratishда rejimni tab belgilaydi — qulflab qo'yamiz (tanlov ko'rinmaydi). */
    // Chegirma — e'lon formasi ICHIDAGI toggle (qulflanmaydi): "Chegirma e'loni" tanlansa
    // narx 2 ta (Oldingi + Hozirgi), "Oddiy e'lon" bo'lsa narx bitta.
    fun setInitialMode(discount: Boolean) = _state.update {
        it.copy(isDiscount = discount, modeLocked = false, discountType = DiscountType.SPECIAL_PRICE)
    }

    /** Aloqa telefoni. */
    fun onContactPhone(v: String) = _state.update { it.copy(contactPhone = v) }
    fun onCustomCategory(v: String) = _state.update { it.copy(customCategoryName = v) }

    // -----------------------------------------------------------------------
    // Asosiy maydonlar
    // -----------------------------------------------------------------------

    /** Biznes nomi lotin alifbosida bo'lishi shart — kirill harflari kiritilmaydi. */
    fun onBusinessName(v: String) = _state.update { it.copy(businessName = TextScript.stripCyrillic(v)) }
    fun onTitle(v: String) = _state.update { it.copy(title = v) }
    fun onDescription(v: String) = _state.update { it.copy(description = v) }
    fun onPriceUnit(v: PriceUnit) = _state.update { it.copy(priceUnit = v) }
    fun onPrice(v: String) = _state.update { it.copy(originalPrice = v.digits()) }

    fun onDiscountType(v: DiscountType) = _state.update {
        // 1+1 da qiymat maydoni yo'q — eski foizni tozalaymiz, aks holda u yashirin qolib ketadi.
        it.copy(discountType = v, discountValue = if (v == DiscountType.FREE_ITEM) "" else it.discountValue)
    }

    fun onDiscountValue(v: String) = _state.update { it.copy(discountValue = v.digits()) }
    fun onConditions(v: String) = _state.update { it.copy(conditions = v) }

    fun onRedemptionMethod(v: RedemptionMethod) = _state.update { it.copy(redemptionMethod = v) }
    fun onPromoCode(v: String) = _state.update { it.copy(promoCode = v.uppercase()) }
    fun onDuration(days: Int) = _state.update { it.copy(durationDays = days) }

    // -----------------------------------------------------------------------
    // Filiallar — xaritadan
    // -----------------------------------------------------------------------

    /** "+" bosilganda xarita ochiladi. */
    fun openMap() = _state.update { it.copy(pickingOnMap = true) }

    fun closeMap() = _state.update {
        it.copy(pickingOnMap = false, searchQuery = "", searchResults = emptyList(), searched = false)
    }

    // -----------------------------------------------------------------------
    // Xaritadagi qidiruv
    // -----------------------------------------------------------------------

    private var searchJob: Job? = null

    /**
     * Har bosilgan harfda so'rov yubormaymiz: oldingi qidiruv bekor qilinadi va foydalanuvchi
     * yozishni to'xtatgandan keyingina Nominatim'ga boramiz (uning qoidasi ham shuni talab qiladi).
     */
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

    /** Natija tanlandi — qidiruv yopiladi, ekran xaritani o'sha joyga olib boradi. */
    fun clearSearch() = _state.update {
        it.copy(searchQuery = "", searchResults = emptyList(), searching = false, searched = false)
    }

    /**
     * Xaritada nuqta tanlandi. Manzil teskari geokodlash bilan avtomatik to'ladi —
     * foydalanuvchi uni qo'lda yozmaydi. Internet bo'lmasa manzil o'rniga koordinata
     * yoziladi va filial baribir qo'shiladi (nuqta yo'qolmasligi kerak).
     */
    fun addBranchFromMap(lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.update { it.copy(resolvingAddress = true) }
            val branch = createBranch(
                id = "br-${Clock.System.now().toEpochMilliseconds()}",
                lat = lat,
                lng = lng,
            )
            _state.update {
                it.copy(
                    branches = it.branches + branch,
                    resolvingAddress = false,
                    pickingOnMap = false,
                )
            }
        }
    }

    fun removeBranch(index: Int) = _state.update {
        it.copy(branches = it.branches.filterIndexed { i, _ -> i != index })
    }

    /** Avtomatik topilgan manzilni aniqlashtirish (masalan "2-qavat" qo'shish). */
    fun onBranchAddress(index: Int, address: String) = _state.update { state ->
        state.copy(
            branches = state.branches.mapIndexed { i, branch ->
                if (i == index) branch.copy(address = address) else branch
            },
        )
    }

    /** Filial nomi ("Chilonzor filiali") — ixtiyoriy. */
    fun onBranchName(index: Int, name: String) = _state.update { state ->
        state.copy(
            branches = state.branches.mapIndexed { i, branch ->
                if (i == index) branch.copy(name = name.ifBlank { null }) else branch
            },
        )
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    // -----------------------------------------------------------------------
    // Rasmlar
    // -----------------------------------------------------------------------

    fun addImage(bytes: ByteArray, fileName: String) {
        if (_state.value.images.size >= ListingValidator.MAX_IMAGES) {
            _state.update { it.copy(message = "Maksimal ${ListingValidator.MAX_IMAGES} ta rasm") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(uploadingImage = true) }
            when (val res = uploadImage(bytes, fileName)) {
                is Resource.Success -> _state.update {
                    it.copy(images = it.images + res.data, uploadingImage = false)
                }
                is Resource.Error -> _state.update {
                    it.copy(uploadingImage = false, message = res.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun removeImage(index: Int) = _state.update {
        it.copy(images = it.images.filterIndexed { i, _ -> i != index })
    }

    // -----------------------------------------------------------------------
    // Saqlash / publish
    // -----------------------------------------------------------------------

    /** Mavjud e'lonni formaga yuklaydi (tahrirlash). */
    fun loadForEdit(listingId: String) {
        if (editingId == listingId) return
        viewModelScope.launch {
            val listing = getListing(listingId) ?: return@launch
            editingId = listing.id
            editingCreatedAt = listing.createdAt
            editingBusinessId = listing.businessId
            _state.value = listing.toUiState()
        }
    }

    fun saveDraft() {
        val listing = buildListing() ?: return
        viewModelScope.launch {
            when (val res = saveDraft.invoke(listing)) {
                is Resource.Success -> {
                    editingId = res.data.id
                    editingCreatedAt = res.data.createdAt
                    _state.update { it.copy(message = "Qoralama saqlandi") }
                }
                is Resource.Error -> _state.update { it.copy(message = res.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun publish() {
        val listing = buildListing() ?: return
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, errors = emptyList()) }
            when (val res = publishListing(listing)) {
                is PublishListingUseCase.Result.Success ->
                    _state.update { it.copy(submitting = false, published = true) }

                // Validatsiya xatolari maydonlarga bog'lanadi — forma ularni joyida ko'rsatadi.
                is PublishListingUseCase.Result.Invalid ->
                    _state.update { it.copy(submitting = false, errors = res.errors) }

                is PublishListingUseCase.Result.Failed ->
                    _state.update { it.copy(submitting = false, message = res.message) }
            }
        }
    }

    /** Forma holatidan domen modelini quradi. Tur tanlanmagan bo'lsa — `null`. */
    private fun buildListing(): Listing? {
        val s = _state.value
        val type = s.businessType ?: return null
        val ownerId = (user.value?.id ?: 0L).toString()
        val now = Clock.System.now().toEpochMilliseconds()

        return Listing(
            id = editingId ?: "lst-$ownerId-$now",
            ownerId = ownerId,
            businessId = currentBusinessId ?: editingBusinessId,
            businessType = type,
            businessName = s.businessName.trim(),
            categoryKey = s.categoryKey,
            customCategoryName = s.customCategoryName.trim().ifBlank { null },
            // Oddiy e'lon belgisi + aloqa telefoni ham attributes ichida saqlanadi (migratsiyasiz).
            attributes = s.attributeValues +
                (if (!s.isDiscount) mapOf(ListingCatalog.REGULAR_KEY to "1") else emptyMap()) +
                (if (s.contactPhone.isNotBlank()) mapOf(ListingCatalog.PHONE_KEY to s.contactPhone.trim()) else emptyMap()) +
                (if (s.businessType == BusinessType.CLOTHING && s.listingGender != null)
                    mapOf(ListingCatalog.GENDER_KEY to s.listingGender.name) else emptyMap()),
            title = s.title.trim(),
            description = s.description.trim().ifBlank { null },
            images = s.images,
            priceUnit = s.priceUnit,
            originalPrice = s.originalPrice.toLongOrNull() ?: 0,
            discount = ListingDiscount(
                type = s.discountType,
                value = s.discountValue.toLongOrNull() ?: 0,
                conditions = s.conditions.trim().ifBlank { null },
            ),
            redemption = ListingRedemption(
                method = s.redemptionMethod,
                promoCode = s.promoCode.trim().ifBlank { null },
            ),
            branches = s.branches,
            validFrom = now,
            validTo = now + s.durationDays.toLong() * MILLIS_PER_DAY,
            status = ListingStatus.DRAFT,
            createdAt = editingCreatedAt ?: now,
            updatedAt = now,
        )
    }

    private fun Listing.toUiState() = PostListingUiState(
        step = PostListingStep.FORM,
        businessType = businessType,
        businessName = businessName,
        categoryKey = categoryKey,
        customCategoryName = customCategoryName.orEmpty(),
        attributeValues = attributes - ListingCatalog.REGULAR_KEY - ListingCatalog.PHONE_KEY - ListingCatalog.GENDER_KEY,
        isDiscount = attributes[ListingCatalog.REGULAR_KEY] != "1",
        modeLocked = true,
        contactPhone = attributes[ListingCatalog.PHONE_KEY].orEmpty(),
        // Kiyim e'lonini tahrirlashда tanlangan jinsni tiklaymiz.
        listingGender = when (attributes[ListingCatalog.GENDER_KEY]) {
            "MALE" -> Gender.MALE
            "FEMALE" -> Gender.FEMALE
            else -> null
        },
        title = title,
        description = description.orEmpty(),
        images = images,
        priceUnit = priceUnit,
        originalPrice = originalPrice.toString(),
        discountType = discount.type,
        discountValue = discount.value.toString(),
        conditions = discount.conditions.orEmpty(),
        redemptionMethod = redemption.method,
        promoCode = redemption.promoCode.orEmpty(),
        branches = branches,
        durationDays = ((validTo - validFrom) / MILLIS_PER_DAY).toInt().coerceAtLeast(1),
        editing = true,
    )

    private companion object {
        const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
    }
}

/**
 * Foydalanuvchi yozishni to'xtatgach kutiladigan vaqt — har harfga so'rov ketmasin.
 * Nominatim foydalanish qoidasi soniyasiga 1 ta so'rovni so'raydi; 400 ms kutish bilan
 * tez yozganda ham bitta so'rov chiqadi. Xaritali ikkala ekran ham shu qiymatni ishlatadi.
 */
internal const val SEARCH_DEBOUNCE_MS = 400L

/** Raqamli maydonlarga faqat raqam kiritiladi (klaviatura turi kafolat bermaydi). */
private fun String.digits(): String = filter { it.isDigit() }

/** Tanlangan biznes turining kategoriyalari (kiyimда per-e'lon jins, boshqasida profil jinsi). */
/** Kategoriyalar — backenddan yuklangan ro'yxat (zaxira: klient katalogi, UseCase beradi). */
fun PostListingUiState.categories(): List<CategoryInfo> = categoryList

/** Tanlangan KATEGORIYAга xos maydonlar (masalan Game Club > PlayStation). */
/** Tanlangan kategoriyaning maydonlari — o'sha javobning ичидан (yangi so'rov yo'q). */
fun PostListingUiState.categoryAttributes() =
    categoryList.firstOrNull { it.key == categoryKey }?.fields.orEmpty()

