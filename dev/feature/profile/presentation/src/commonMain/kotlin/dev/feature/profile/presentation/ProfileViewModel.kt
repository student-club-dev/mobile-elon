package dev.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.common.Resource
import dev.core.common.error.FormError
import dev.core.common.error.toFormError
import dev.core.domain.model.Ad
import dev.core.domain.model.DiscountOffer
import dev.core.domain.model.JobApplication
import dev.core.domain.repository.AdRepository
import dev.core.domain.repository.DiscountRepository
import dev.core.domain.repository.JobRepository
import dev.core.domain.usecase.LogoutUseCase
import dev.core.domain.usecase.ObserveCurrentUserUseCase
import dev.core.domain.usecase.RequestPhoneOtpUseCase
import dev.core.domain.usecase.VerifyPhoneOtpUseCase
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.usecase.ObserveProfileUseCase
import dev.feature.profile.domain.usecase.RefreshProfileUseCase
import dev.feature.profile.domain.usecase.SaveProfileUseCase
import dev.feature.profile.domain.usecase.UploadAvatarUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Profil (1z) ekranining holati. */
data class ProfileUiState(
    val name: String = "Talaba",
    val contact: String = "",
    val myAds: List<Ad> = emptyList(),
    val savedDiscounts: List<DiscountOffer> = emptyList(),
    val applications: List<JobApplication> = emptyList(),
    /** Tahrirlash ekrani uchun xom profil ma'lumoti (local keshdan). */
    val profile: UserProfile? = null,
)

class ProfileViewModel(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    observeProfileUseCase: ObserveProfileUseCase,
    private val adRepository: AdRepository,
    discountRepository: DiscountRepository,
    jobRepository: JobRepository,
    private val logoutUseCase: LogoutUseCase,
    private val saveProfileUseCase: SaveProfileUseCase,
    private val refreshProfileUseCase: RefreshProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val requestPhoneOtpUseCase: RequestPhoneOtpUseCase,
    private val verifyPhoneOtpUseCase: VerifyPhoneOtpUseCase,
) : ViewModel() {

    init {
        // Offline-first: ekran ochilishi bilan fon rejimida masofaviy manbadan
        // yangilaymiz. Xato bo'lsa keshdagi ma'lumot ko'rinaveradi.
        viewModelScope.launch { refreshProfileUseCase() }
        viewModelScope.launch { adRepository.refresh() }
    }

    // Universitet/kurs bu yerда yo'q: ular talaba profilining maydonlari (spec'da "students
    // only"), biznes egasida hech qachon to'lmaydi.
    private val header = combine(
        observeCurrentUserUseCase(),
        observeProfileUseCase(),
    ) { user, profile ->
        Header(
            // Ism profildan olinadi; profil hali to'ldirilmagan bo'lsa — sessiya nomidan.
            name = profile?.displayName
                ?: user?.fullName?.takeIf { it.isNotBlank() }
                ?: "Talaba",
            contact = user?.phoneNumber ?: user?.email.orEmpty(),
            ownerId = (user?.id ?: 0L).toString(),
            profile = profile,
        )
    }

    private val lists = combine(
        adRepository.observeAds(),
        discountRepository.observeSaved(),
        jobRepository.observeApplications(),
    ) { ads, saved, applications -> Lists(ads, saved, applications) }

    val state: StateFlow<ProfileUiState> = combine(header, lists) { h, l ->
        ProfileUiState(
            name = h.name,
            contact = h.contact,
            myAds = l.ads.filter { it.ownerId == h.ownerId },
            savedDiscounts = l.saved,
            applications = l.applications,
            profile = h.profile,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    /** E'lonni o'chirish ("Mening e'lonlarim"). */
    fun deleteAd(adId: String) {
        viewModelScope.launch { adRepository.delete(adId) }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onDone()
        }
    }

    /**
     * Profilni saqlaydi (masofaviy manba + local kesh). [onResult] `null` — muvaffaqiyat,
     * aks holda [FormError]: umumiy xabar **va** backend qaytargan maydon xatolari
     * (`{"phoneNumber": "Noto'g'ri format"}`). Kesh yangilangach `state` avtomatik yangilanadi.
     */
    fun saveProfile(updated: UserProfile, onResult: (FormError?) -> Unit) {
        viewModelScope.launch {
            when (val res = saveProfileUseCase(updated)) {
                is Resource.Success -> onResult(null)
                is Resource.Error -> onResult(res.toFormError())
                else -> onResult(null)
            }
        }
    }

    /**
     * Raqamga SMS kod yuboradi (`POST /v1/auth/business/otp/request`).
     *
     * Nega profil ekranida kerak: backend telefonni almashtirganда uning tasdig'ini **bekor
     * qiladi** (spec: *"changing the phone number resets its verification"*), tasdiqlanmagan
     * raqam bilan esa biznes yaratib/tahrirlab bo'lmaydi (`403 PHONE_NOT_VERIFIED`).
     *
     * [onResult] — kod ketgan bo'lsa `cooldownSeconds` (qayta yuborish taymeri uchun),
     * aks holda `error` matni.
     */
    fun requestPhoneOtp(phone: String, onResult: (cooldownSeconds: Int?, error: String?) -> Unit) {
        viewModelScope.launch {
            when (val res = requestPhoneOtpUseCase(phone)) {
                is Resource.Success -> onResult(res.data.resendCooldownSeconds, null)
                is Resource.Error -> onResult(null, res.message)
                else -> onResult(null, null)
            }
        }
    }

    /** SMS kodni tekshiradi. [onResult] `null` — raqam tasdiqlandi, aks holda xato matni. */
    fun verifyPhoneOtp(phone: String, code: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            when (val res = verifyPhoneOtpUseCase(phone, code)) {
                is Resource.Success -> {
                    // Tasdiqlangach profilni qayta tortamiz — server holati keshga tushsin.
                    refreshProfileUseCase()
                    onResult(null)
                }
                is Resource.Error -> onResult(res.message)
                else -> onResult(null)
            }
        }
    }

    /**
     * Galereyadan tanlangan rasmni yuklaydi va profilga bog'laydi. [onResult] `null` —
     * muvaffaqiyat (kesh yangilanib, `state.profile.avatarUrl` avtomatik keladi),
     * aks holda xato matni.
     */
    fun uploadAvatar(bytes: ByteArray, fileName: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            when (val res = uploadAvatarUseCase(bytes, fileName)) {
                is Resource.Success -> onResult(null)
                is Resource.Error -> onResult(res.message)
                else -> onResult(null)
            }
        }
    }

    private data class Header(
        val name: String,
        val contact: String,
        val ownerId: String,
        val profile: UserProfile?,
    )

    private data class Lists(
        val ads: List<Ad>,
        val saved: List<DiscountOffer>,
        val applications: List<JobApplication>,
    )
}

