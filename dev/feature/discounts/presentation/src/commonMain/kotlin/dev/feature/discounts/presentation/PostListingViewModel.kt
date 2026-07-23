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
import dev.feature.discounts.domain.model.OptionGroup
import dev.feature.discounts.domain.model.OptionItem
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.RedemptionMethod
import dev.feature.discounts.domain.model.RedemptionPeriod
import dev.feature.discounts.domain.model.SelectionType
import dev.feature.discounts.domain.usecase.GetCategoriesUseCase
import dev.feature.discounts.domain.usecase.GetListingUseCase
import dev.feature.discounts.domain.usecase.PublishListingUseCase
import dev.feature.discounts.domain.usecase.SaveDraftUseCase
import dev.feature.discounts.domain.usecase.UploadListingImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

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

    /** Narx birligi (`priceUnit`) — biznes turiga ruxsat etilganlar ichidan tanlanadi. */
    val priceUnit: PriceUnit = PriceUnit.PER_ITEM,
    val originalPrice: String = "",

    val discountType: DiscountType = DiscountType.SPECIAL_PRICE,
    val discountValue: String = "",
    /** `discount.conditions` — aksiya sharti ("ikkinchi kofe bepul", "faqat ish kunlari"). */
    val conditions: String = "",
    /** `discount.appliesToOptions` — chegirma qo'shimchalar narxiga ham tarqaladimi. */
    val appliesToOptions: Boolean = false,

    val redemptionMethod: RedemptionMethod = RedemptionMethod.STUDENT_ID,
    val promoCode: String = "",
    /** "Onlayn havola" usulida chegirma ishlatiladigan sayt (`redemption.url`). */
    val redemptionUrl: String = "",
    /** `redemption.perUserLimit` — bo'sh matn = cheksiz. */
    val perUserLimit: String = "1",
    val perUserPeriod: RedemptionPeriod = RedemptionPeriod.DAY,
    /** `redemption.totalLimit` — bo'sh matn = cheksiz. */
    val totalLimit: String = "",
    /** Aloqa telefoni — soddalashtirilgan formada so'raladi. */
    val contactPhone: String = "",

    /** `optionGroups` — "Hajmni tanlang" kabi qo'shimchalar (ixtiyoriy). */
    val optionGroups: List<OptionGroup> = emptyList(),

    /** Biznesning filiallari — e'lon qaysilarida amal qilishini shu ro'yxatdan tanlanadi. */
    val branches: List<ListingBranch> = emptyList(),
    /** `branchIds` — belgilangan filiallar. Bo'sh bo'lsa e'lon hech qayerda amal qilmaydi. */
    val selectedBranchIds: Set<String> = emptySet(),

    /**
     * `validFrom` — boshlanish sanasi "kk.oo.yyyy" ko'rinishida. Bo'sh matn = bugundan
     * (eng ko'p uchraydigan holat), kelajakdagi sana esa e'lonni `SCHEDULED` qiladi (spec §6.5).
     */
    val startDate: String = "",

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

    /** Boshlanish sanasi to'liq yozilgan va haqiqiy sanami (bo'sh — bugun, xato emas). */
    val startDateValid: Boolean get() = startDate.isBlank() || parseDate(startDate) != null

    fun errorFor(field: ListingField): String? = errors.firstOrNull { it.field == field }?.message
}

class PostListingViewModel(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val publishListing: PublishListingUseCase,
    private val saveDraft: SaveDraftUseCase,
    private val uploadImage: UploadListingImageUseCase,
    private val getListing: GetListingUseCase,
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
                    priceUnit = biz.businessType?.let(ListingCatalog::defaultPriceUnit) ?: it.priceUnit,
                    businessName = biz.name,
                    branches = biz.branches,
                    // Odatiy holat — e'lon biznesning hamma filialida amal qiladi; kerak
                    // bo'lsa foydalanuvchi ortiqchasini olib tashlaydi.
                    selectedBranchIds = biz.branches.map { branch -> branch.id }.toSet(),
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
    fun onAppliesToOptions(v: Boolean) = _state.update { it.copy(appliesToOptions = v) }

    fun onRedemptionMethod(v: RedemptionMethod) = _state.update { it.copy(redemptionMethod = v) }
    fun onPromoCode(v: String) = _state.update { it.copy(promoCode = v.uppercase()) }
    fun onRedemptionUrl(v: String) = _state.update { it.copy(redemptionUrl = v.trim()) }

    /** Bo'sh matn — cheksiz (`null` yuboriladi). */
    fun onPerUserLimit(v: String) = _state.update { it.copy(perUserLimit = v.digits().take(4)) }
    fun onPerUserPeriod(v: RedemptionPeriod) = _state.update { it.copy(perUserPeriod = v) }
    fun onTotalLimit(v: String) = _state.update { it.copy(totalLimit = v.digits().take(6)) }

    fun onStartDate(v: String) = _state.update { it.copy(startDate = formatDateInput(v)) }
    fun onDuration(days: Int) = _state.update { it.copy(durationDays = days) }

    // -----------------------------------------------------------------------
    // Filiallar — `branchIds`
    // -----------------------------------------------------------------------

    /**
     * E'lon qaysi filiallarda amal qilishini belgilaydi. Yangi filial bu yerда qo'shilmaydi:
     * u backendда alohida resurs (`POST /business/{id}/branches`) va tuman/ish vaqtini talab
     * qiladi — shuning uchun biznes profilida yaratiladi, e'lon esa faqat tanlaydi.
     */
    fun toggleBranch(id: String) = _state.update { state ->
        state.copy(
            selectedBranchIds = if (id in state.selectedBranchIds) {
                state.selectedBranchIds - id
            } else {
                state.selectedBranchIds + id
            },
        )
    }

    // -----------------------------------------------------------------------
    // Qo'shimchalar (`optionGroups`)
    // -----------------------------------------------------------------------

    fun addOptionGroup() = _state.update {
        it.copy(optionGroups = it.optionGroups + OptionGroup(name = "", options = listOf(OptionItem(""))))
    }

    fun removeOptionGroup(index: Int) = _state.update {
        it.copy(optionGroups = it.optionGroups.filterIndexed { i, _ -> i != index })
    }

    fun onGroupName(index: Int, v: String) = updateGroup(index) { it.copy(name = v) }

    fun onGroupSelectionType(index: Int, v: SelectionType) = updateGroup(index) {
        // Bitta tanlovli guruhда min/maks ma'nosiz — ular yashirin qolib ketmasin.
        if (v == SelectionType.SINGLE) it.copy(selectionType = v, minSelect = null, maxSelect = null)
        else it.copy(selectionType = v)
    }

    fun onGroupRequired(index: Int, v: Boolean) = updateGroup(index) { it.copy(isRequired = v) }

    fun onGroupMinSelect(index: Int, v: String) = updateGroup(index) {
        it.copy(minSelect = v.digits().take(2).toIntOrNull())
    }

    fun onGroupMaxSelect(index: Int, v: String) = updateGroup(index) {
        it.copy(maxSelect = v.digits().take(2).toIntOrNull())
    }

    fun addOption(groupIndex: Int) = updateGroup(groupIndex) { it.copy(options = it.options + OptionItem("")) }

    fun removeOption(groupIndex: Int, optionIndex: Int) = updateGroup(groupIndex) { group ->
        group.copy(options = group.options.filterIndexed { i, _ -> i != optionIndex })
    }

    fun onOptionName(groupIndex: Int, optionIndex: Int, v: String) =
        updateOption(groupIndex, optionIndex) { it.copy(name = v) }

    /** Qo'shimcha narxi manfiy ham bo'lishi mumkin ("kichik hajm — 5000 arzon"). */
    fun onOptionPriceDelta(groupIndex: Int, optionIndex: Int, v: String) =
        updateOption(groupIndex, optionIndex) {
            val negative = v.startsWith("-")
            val amount = v.digits().take(9).toLongOrNull() ?: 0
            it.copy(priceDelta = if (negative) -amount else amount)
        }

    fun onOptionAvailable(groupIndex: Int, optionIndex: Int, v: Boolean) =
        updateOption(groupIndex, optionIndex) { it.copy(isAvailable = v) }

    private fun updateGroup(index: Int, transform: (OptionGroup) -> OptionGroup) = _state.update { state ->
        state.copy(optionGroups = state.optionGroups.mapIndexed { i, g -> if (i == index) transform(g) else g })
    }

    private fun updateOption(groupIndex: Int, optionIndex: Int, transform: (OptionItem) -> OptionItem) =
        updateGroup(groupIndex) { group ->
            group.copy(options = group.options.mapIndexed { i, o -> if (i == optionIndex) transform(o) else o })
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

            // E'lon faqat o'ziga biriktirilgan filiallarni biladi. Tahrirlashда biznesning
            // qolgan filiallari ham ro'yxatda turishi kerak — aks holda olib tashlangan
            // filialni qaytarib qo'shib bo'lmasdi.
            val businessId = listing.businessId ?: return@launch
            currentBusinessId = businessId
            val biz = getBusiness(businessId) ?: return@launch
            _state.update { state -> state.copy(branches = biz.branches.ifEmpty { state.branches }) }
            loadCategories()
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
        // Chala yozilgan sana jimgina "bugun" ga aylanib qolmasin — foydalanuvchi uni ko'radi
        // va tuzatadi. Qoralamada bunday tekshiruv yo'q: qoralama to'liq bo'lmasligi mumkin.
        if (!_state.value.startDateValid) {
            _state.update {
                it.copy(
                    errors = listOf(
                        ListingError(ListingField.VALIDITY, "Boshlanish sanasini to'liq yozing (kk.oo.yyyy)"),
                    ),
                )
            }
            return
        }
        val listing = buildListing() ?: return
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, errors = emptyList()) }
            // Filial tanlash faqat biznes filial taklif qilganda majburiy — onlayn
            // biznesda (`isOnlineOnly`) ular umuman bo'lmaydi.
            when (
                val res = publishListing(
                    listing,
                    _state.value.categoryAttributes(),
                    requireBranch = _state.value.branches.isNotEmpty(),
                )
            ) {
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
        // Sana ko'rsatilmagan yoki chala yozilgan bo'lsa — bugundan. Xato sanani validator
        // ushlaydi, lekin qurish paytida hech qachon `null` bo'lib qolmasligi kerak.
        val validFrom = s.startDate.takeIf { it.isNotBlank() }?.let(::parseDate)?.toEpochMillis() ?: now

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
                appliesToOptions = s.appliesToOptions,
            ),
            redemption = ListingRedemption(
                method = s.redemptionMethod,
                promoCode = s.promoCode.trim().ifBlank { null },
                url = s.redemptionUrl.trim().ifBlank { null },
                // Bo'sh maydon — cheksiz (`null`), backend ham shunday tushunadi.
                perUserLimit = s.perUserLimit.toIntOrNull(),
                perUserPeriod = s.perUserPeriod,
                totalLimit = s.totalLimit.toIntOrNull(),
            ),
            optionGroups = s.optionGroups,
            // Faqat belgilangan filiallar — ular `branchIds` bo'lib ketadi.
            branches = s.branches.filter { it.id in s.selectedBranchIds },
            validFrom = validFrom,
            validTo = validFrom + s.durationDays.toLong() * MILLIS_PER_DAY,
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
        appliesToOptions = discount.appliesToOptions,
        redemptionMethod = redemption.method,
        promoCode = redemption.promoCode.orEmpty(),
        redemptionUrl = redemption.url.orEmpty(),
        perUserLimit = redemption.perUserLimit?.toString().orEmpty(),
        perUserPeriod = redemption.perUserPeriod,
        totalLimit = redemption.totalLimit?.toString().orEmpty(),
        optionGroups = optionGroups,
        branches = branches,
        // Saqlangan e'lon faqat o'ziga biriktirilgan filiallarni biladi — hammasi belgilangan
        // bo'lib ochiladi, biznesning qolgan filiallari `prefillFromBusiness` da qo'shiladi.
        selectedBranchIds = branches.map { it.id }.toSet(),
        startDate = formatDate(validFrom),
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
 * tez yozganda ham bitta so'rov chiqadi. Xaritali ekran (biznes joyi) shu qiymatni ishlatadi.
 */
internal const val SEARCH_DEBOUNCE_MS = 400L

/** Raqamli maydonlarga faqat raqam kiritiladi (klaviatura turi kafolat bermaydi). */
private fun String.digits(): String = filter { it.isDigit() }

// ---------------------------------------------------------------------------
// Sana — `validFrom` maydoni uchun (kk.oo.yyyy)
// ---------------------------------------------------------------------------

/** Kiritilayotgan matnni "kk.oo.yyyy" ga keltiradi: nuqtalarni o'zi qo'yadi. */
internal fun formatDateInput(input: String): String {
    val digits = input.filter { it.isDigit() }.take(8)
    return buildString {
        digits.forEachIndexed { index, char ->
            if (index == 2 || index == 4) append('.')
            append(char)
        }
    }
}

/** "kk.oo.yyyy" → sana. To'liq yozilmagan yoki mavjud bo'lmagan sana bo'lsa `null`. */
internal fun parseDate(text: String): LocalDate? {
    val parts = text.split(".")
    if (parts.size != 3 || parts[0].length != 2 || parts[1].length != 2 || parts[2].length != 4) return null
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    // 31-fevral kabi mavjud bo'lmagan sanalarda `LocalDate` xato beradi — uni qaytarmaymiz.
    return runCatching { LocalDate(year, month, day) }.getOrNull()
}

internal fun formatDate(millis: Long): String {
    val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.dayOfMonth.pad2()}.${date.monthNumber.pad2()}.${date.year}"
}

/** Sana kun boshidan boshlanadi — e'lon o'sha kuni ertalab faollashadi. */
internal fun LocalDate.toEpochMillis(): Long =
    atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

private fun Int.pad2(): String = toString().padStart(2, '0')

/** Tanlangan biznes turining kategoriyalari (kiyimда per-e'lon jins, boshqasida profil jinsi). */
/** Kategoriyalar — backenddan yuklangan ro'yxat (zaxira: klient katalogi, UseCase beradi). */
fun PostListingUiState.categories(): List<CategoryInfo> = categoryList

/** Tanlangan KATEGORIYAга xos maydonlar (masalan Game Club > PlayStation). */
/** Tanlangan kategoriyaning maydonlari — o'sha javobning ичидан (yangi so'rov yo'q). */
fun PostListingUiState.categoryAttributes() =
    categoryList.firstOrNull { it.key == categoryKey }?.fields.orEmpty()

