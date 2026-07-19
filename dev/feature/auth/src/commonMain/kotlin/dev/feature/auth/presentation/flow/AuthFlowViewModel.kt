package dev.feature.auth.presentation.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.common.Resource
import dev.core.domain.model.ExternalAuthUser
import dev.core.domain.model.User
import dev.core.domain.repository.SettingsRepository
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.repository.ProfileExistence
import dev.core.domain.usecase.ConfirmEmailSignupUseCase
import dev.feature.profile.domain.usecase.HasProfileUseCase
import dev.core.domain.usecase.LoginUseCase
import dev.core.domain.usecase.ObserveCurrentUserUseCase
import dev.core.domain.usecase.RegisterUseCase
import dev.core.domain.usecase.RequestEmailSignupUseCase
import dev.feature.profile.domain.usecase.SaveProfileUseCase
import dev.core.domain.usecase.SendPasswordResetUseCase
import dev.core.domain.usecase.SyncExternalUserUseCase
import dev.feature.auth.social.OtpSendResult
import dev.feature.auth.social.SocialAuthController
import dev.feature.auth.social.SocialAuthResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI navigatsiyasini boshqaradigan bir martalik hodisalar. */
sealed interface AuthEvent {
    /** SMS kod yuborildi — OTP ekraniga o'tish. */
    data object OtpSent : AuthEvent

    /** OTP tasdiqlandi — ro'yxatni yakunlash (SignUp) ekraniga o'tish. */
    data object OtpVerified : AuthEvent

    /** Email hisob yaratildi, tasdiqlash havolasi yuborildi — verify ekraniga. */
    data object EmailVerificationSent : AuthEvent

    /** Ro'yxat yakunlandi — muvaffaqiyat ekraniga o'tish. */
    data object Registered : AuthEvent

    /** Profil saqlandi — bosh sahifaga o'tish. */
    data object ProfileSaved : AuthEvent

    /** To'liq kirildi (email/Google/avto-OTP) — bosh sahifaga o'tish. */
    data class Authenticated(val user: User) : AuthEvent
}

/**
 * Auth oqimining forma holatini va biznes-amallarni boshqaradi.
 * Email login va tashqi (Google/Telefon) auth domen use-case'lariga ulangan;
 * platformaga bog'liq [SocialAuthController] composable'dan uzatiladi.
 */
class AuthFlowViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    private val requestEmailSignupUseCase: RequestEmailSignupUseCase,
    private val confirmEmailSignupUseCase: ConfirmEmailSignupUseCase,
    private val saveProfileUseCase: SaveProfileUseCase,
    private val syncExternalUserUseCase: SyncExternalUserUseCase,
    private val hasProfileUseCase: HasProfileUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    /** Jinsni local settings'ga yozadi (biznes turlari shundan filtrlanadi). */
    private val settingsRepository: SettingsRepository,
    /** true → email kodi (Cloud Function) ishlatiladi; false → native (deploy'siz). */
    private val useEmailCode: Boolean = false,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthFlowState())
    val state: StateFlow<AuthFlowState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    /**
     * Ilova ochilishida avtomatik kirish holati (local keshdan):
     * `null` — hali tekshirilmoqda (splash), `true` — kirgan (HOME), `false` — kirmagan (onboarding).
     */
    val loggedIn: StateFlow<Boolean?> = observeCurrentUserUseCase()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** OTP orqali tasdiqlangan foydalanuvchi — ro'yxatni yakunlashda ishlatiladi. */
    private var verifiedExternal: ExternalAuthUser? = null

    private var timerJob: Job? = null

    // --- Forma yangilanishlari ---
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v.filter { c -> c.isDigit() }.take(9), error = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null, info = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _state.update { it.copy(confirmPassword = v, error = null) }
    fun togglePasswordVisible() = _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun toggleRememberMe() = _state.update { it.copy(rememberMe = !it.rememberMe) }
    fun onOtpChange(v: String) = _state.update { it.copy(otp = v.filter { c -> c.isDigit() }.take(6), error = null) }
    fun onEmailCodeChange(v: String) = _state.update { it.copy(emailCode = v.filter { c -> c.isDigit() }.take(6), error = null) }

    fun onFirstNameChange(v: String) = _state.update { it.copy(firstName = v) }
    fun onLastNameChange(v: String) = _state.update { it.copy(lastName = v) }
    fun onGenderChange(g: String) = _state.update { it.copy(gender = g, error = null) }
    fun onRoleChange(r: Role) = _state.update { it.copy(role = r) }
    fun onUniversityEmailChange(v: String) = _state.update { it.copy(universityEmail = v) }
    fun onBusinessNameChange(v: String) = _state.update { it.copy(businessName = v, error = null) }
    fun onBusinessTypeChange(v: String) = _state.update { it.copy(businessType = v) }
    fun toggleTerms() = _state.update { it.copy(termsAccepted = !it.termsAccepted) }

    fun onBirthYearChange(y: Int) = _state.update { it.copy(birthYear = y) }
    fun onCourseYearChange(c: CourseYear) = _state.update { it.copy(courseYear = c) }
    fun onUniversityQueryChange(v: String) = _state.update { it.copy(universityQuery = v) }
    fun onUniversitySelected(id: String) = _state.update { it.copy(universityId = id) }

    // ------------------------------------------------------------------
    // Email + parol bilan kirish
    // ------------------------------------------------------------------
    fun login() {
        val s = _state.value
        if (s.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = loginUseCase(s.email.trim(), s.password)) {
                is Resource.Success -> finishAuthenticated(result.data)
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    // ------------------------------------------------------------------
    // Telefon (OTP)
    // ------------------------------------------------------------------
    fun sendOtp(controller: SocialAuthController) {
        val s = _state.value
        if (s.isLoading) return
        if (!s.phoneValid) {
            _state.update { it.copy(error = "To‘liq 9 xonali raqam kiriting.") }
            return
        }
        val e164 = "+998${s.phoneDigits}"
        // Yangi OTP so'rovi — eski tasdiqlangan sessiya belgisini tozalaymiz, aks holda
        // confirmOtp OTP ni butunlay o'tkazib yuborishi mumkin (eski verifiedExternal qolib).
        verifiedExternal = null
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = controller.sendOtp(e164)) {
                OtpSendResult.Sent -> {
                    _state.update { it.copy(isLoading = false) }
                    startResendTimer()
                    _events.send(AuthEvent.OtpSent)
                }
                is OtpSendResult.AutoVerified -> onPhoneVerified(result.user)
                is OtpSendResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun confirmOtp(controller: SocialAuthController) {
        val s = _state.value
        if (s.isLoading) return
        // Sessiya allaqachon tasdiqlangan (oldingi urinishda profil tekshiruvi ERROR bergan) —
        // OTP kodi iste'mol qilingan, uni qayta tasdiqlamaymiz; faqat profil tekshiruvini
        // qayta yuritamiz.
        verifiedExternal?.let { external ->
            _state.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch { onPhoneVerified(external) }
            return
        }
        if (!s.otpValid) {
            _state.update { it.copy(error = "6 xonali kodni kiriting.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = controller.confirmOtp(s.otp)) {
                is SocialAuthResult.Success -> onPhoneVerified(result.user)
                SocialAuthResult.Cancelled -> _state.update { it.copy(isLoading = false) }
                is SocialAuthResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    /** OTP yuborilgach 60 soniyalik qayta yuborish taymerini ishga tushiradi. */
    fun startResendTimer() {
        timerJob?.cancel()
        _state.update { it.copy(resendSeconds = 60) }
        timerJob = viewModelScope.launch {
            while (_state.value.resendSeconds > 0) {
                delay(1000)
                _state.update { it.copy(resendSeconds = (it.resendSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun resend(controller: SocialAuthController) {
        if (_state.value.resendSeconds > 0) return
        sendOtp(controller)
    }

    // ------------------------------------------------------------------
    // Email + parol bilan ro'yxatdan o'tish (yangi hisob)
    // ------------------------------------------------------------------
    fun registerWithEmail() {
        val s = _state.value
        if (s.isLoading) return
        val email = s.email.trim()
        if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
            _state.update { it.copy(error = "To‘g‘ri email manzil kiriting.") }
            return
        }
        if (s.password.length < 6) {
            _state.update { it.copy(error = "Parol kamida 6 belgidan iborat bo‘lsin.") }
            return
        }
        if (s.password != s.confirmPassword) {
            _state.update { it.copy(error = "Parollar mos kelmadi.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            if (useEmailCode) {
                // Cloud Function bilan: akkaunt faqat kod tasdiqlangач yaratiladi
                when (val result = requestEmailSignupUseCase(email)) {
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false, emailCode = "") }
                        startResendTimer()
                        _events.send(AuthEvent.EmailVerificationSent)
                    }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                    Resource.Loading -> Unit
                }
            } else {
                // Native (deploy'siz): akkaunt darrov yaratiladi
                when (val result = registerUseCase(email, s.password)) {
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        _events.send(AuthEvent.Registered)
                    }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                    Resource.Loading -> Unit
                }
            }
        }
    }

    /** Kod ekrani — kod to'g'ri bo'lsa akkaunt yaratiladi va tizimga kiriladi. */
    fun verifyEmailCode() {
        val s = _state.value
        if (s.isLoading) return
        if (!s.emailCodeValid) {
            _state.update { it.copy(error = "6 xonali kodni kiriting.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null, info = null) }
        viewModelScope.launch {
            when (val result = confirmEmailSignupUseCase(s.email.trim(), s.emailCode, s.password)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(AuthEvent.Registered) // → rol tanlash (Success)
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    /** Email kodini qayta yuboradi. */
    fun resendEmailCode() {
        val s = _state.value
        if (s.isLoading || s.resendSeconds > 0) return
        viewModelScope.launch {
            when (val result = requestEmailSignupUseCase(s.email.trim())) {
                is Resource.Success -> {
                    _state.update { it.copy(info = "Kod qayta yuborildi.", error = null) }
                    startResendTimer()
                }
                is Resource.Error -> _state.update { it.copy(error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    // ------------------------------------------------------------------
    // Ro'yxatni yakunlash
    // ------------------------------------------------------------------
    /**
     * Telefon (OTP) orqali kelgan bo'lsa — joriy sessiya profilini saqlaydi.
     * To'g'ridan-to'g'ri kelgan bo'lsa — universitet emaili + parol bilan yangi
     * Firebase hisob yaratadi, so'ng profilni saqlaydi.
     */
    fun register() {
        val s = _state.value
        if (s.isLoading) return
        if (!s.termsAccepted) {
            _state.update { it.copy(error = "Shartlarga rozilik bering.") }
            return
        }
        if (verifiedExternal == null) {
            // Bu ekranga telefon (OTP) orqali kelinadi — sessiya bo'lishi shart
            _state.update { it.copy(error = "Avval telefon raqamini tasdiqlang.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            persistGender()
            when (val saved = saveProfileUseCase(profileFromState(_state.value))) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(AuthEvent.Registered)
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = saved.message) }
                Resource.Loading -> Unit
            }
        }
    }

    /** Profilni to'ldirish ekrani (1n) — universitet/yil/kursni saqlab, bosh sahifaga o'tadi. */
    fun completeProfile() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            persistGender()
            when (val saved = saveProfileUseCase(profileFromState(_state.value))) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(AuthEvent.ProfileSaved)
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = saved.message) }
                Resource.Loading -> Unit
            }
        }
    }

    /** Jinsni local settings'ga yozadi — biznes turlari/kategoriyalar shundan moslanadi. */
    private suspend fun persistGender() {
        _state.value.gender?.let { settingsRepository.setValue(SettingsRepository.KEY_GENDER, it) }
    }

    private fun profileFromState(s: AuthFlowState) = UserProfile(
        firstName = s.firstName.ifBlank { null },
        lastName = s.lastName.ifBlank { null },
        phoneNumber = if (s.phoneDigits.isNotBlank()) "+998${s.phoneDigits}" else null,
        gender = s.gender,
        role = s.role.name,
        universityId = if (s.role == Role.BUSINESS) null else s.universityId,
        universityEmail = s.universityEmail.ifBlank { null },
        birthYear = s.birthYear,
        courseYear = if (s.role == Role.BUSINESS) null else s.courseYear.name,
        businessName = s.businessName.ifBlank { null },
        businessType = s.businessType.ifBlank { null },
    )

    // ------------------------------------------------------------------
    // Google Sign-In
    // ------------------------------------------------------------------
    fun signInWithGoogle(controller: SocialAuthController) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = controller.signInWithGoogle()) {
                is SocialAuthResult.Success -> syncAndFinish(result.user)
                SocialAuthResult.Cancelled -> _state.update { it.copy(isLoading = false) }
                is SocialAuthResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    // ------------------------------------------------------------------
    // Apple Sign-In
    // ------------------------------------------------------------------
    fun signInWithApple(controller: SocialAuthController) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = controller.signInWithApple()) {
                is SocialAuthResult.Success -> syncAndFinish(result.user)
                SocialAuthResult.Cancelled -> _state.update { it.copy(isLoading = false) }
                is SocialAuthResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    // ------------------------------------------------------------------
    // Telegram (Cloud Function → custom token)
    // ------------------------------------------------------------------
    fun signInWithTelegram(controller: SocialAuthController) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = controller.signInWithTelegram()) {
                is SocialAuthResult.Success -> syncAndFinish(result.user)
                SocialAuthResult.Cancelled -> _state.update { it.copy(isLoading = false) }
                is SocialAuthResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    /**
     * Biometrik tasdiqdan (Face ID / barmoq izi) so'ng chaqiriladi — local keshda sessiya bo'lsa
     * to'g'ridan-to'g'ri HOME'ga, aks holda xato xabari (avval oddiy kirish kerak).
     */
    fun onBiometricAuthenticated() {
        viewModelScope.launch {
            val user = observeCurrentUserUseCase().first()
            if (user != null) finishAuthenticated(user)
            else _state.update { it.copy(error = "Saqlangan hisob topilmadi. Avval email/parol bilan kiring.") }
        }
    }

    /** Biometrik oqim xatosi (mavjud emas / tanilmadi) uchun xabar. */
    fun biometricError(message: String) = _state.update { it.copy(error = message) }

    /** Parolni tiklash havolasini yuboradi (Firebase). */
    fun requestPasswordReset() {
        val email = _state.value.email.trim()
        if (_state.value.isLoading) return
        if (email.isBlank()) {
            _state.update { it.copy(error = "Email manzilini kiriting.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null, info = null) }
        viewModelScope.launch {
            when (val result = sendPasswordResetUseCase(email)) {
                is Resource.Success ->
                    _state.update { it.copy(isLoading = false, info = "Tiklash havolasi $email manziliga yuborildi.") }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null, info = null) }

    // ------------------------------------------------------------------
    /**
     * Telefon OTP tasdiqlangач (qo'lda yoki avto): Firebase sessiyasi tayyor.
     * Profil bor bo'lsa — mavjud foydalanuvchi → HOME.
     * Profil yo'q bo'lsa — yangi foydalanuvchi → SignUp (rol/ism to'ldirish).
     */
    private suspend fun onPhoneVerified(external: ExternalAuthUser) {
        verifiedExternal = external
        when (hasProfileUseCase()) {
            // Profil bor → mavjud foydalanuvchi → HOME.
            ProfileExistence.EXISTS -> syncAndFinish(external)
            // Backend aniq "yo'q" dedi (404) → yangi foydalanuvchi → SignUp.
            ProfileExistence.MISSING -> {
                _state.update { it.copy(isLoading = false) }
                _events.send(AuthEvent.OtpVerified)
            }
            // Tekshirib bo'lmadi (backend o'chiq/timeout) → OTP'da qolib qayta urinsin.
            // Mavjud profilli foydalanuvchini noto'g'ri SignUp'ga tushirmaymiz.
            ProfileExistence.ERROR -> _state.update {
                it.copy(isLoading = false, error = "Profilni tekshirib bo'lmadi. Internetni tekshirib, qayta urinib ko'ring.")
            }
        }
    }

    private suspend fun syncAndFinish(external: ExternalAuthUser) {
        when (val synced = syncExternalUserUseCase(external)) {
            is Resource.Success -> finishAuthenticated(synced.data)
            is Resource.Error -> _state.update { it.copy(isLoading = false, error = synced.message) }
            Resource.Loading -> Unit
        }
    }

    private suspend fun finishAuthenticated(user: User) {
        _state.update { it.copy(isLoading = false) }
        _events.send(AuthEvent.Authenticated(user))
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
