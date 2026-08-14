package dev.feature.auth.presentation.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.common.Resource
import dev.core.domain.model.User
import dev.core.domain.repository.SettingsRepository
import dev.core.domain.usecase.ForgotPasswordUseCase
import dev.core.domain.usecase.LoginUseCase
import dev.core.domain.usecase.LoginWithGoogleUseCase
import dev.core.domain.usecase.LogoutUseCase
import dev.core.domain.usecase.ObserveCurrentUserUseCase
import dev.core.domain.usecase.RegisterUseCase
import dev.core.domain.usecase.RequestRegistrationOtpUseCase
import dev.core.domain.usecase.RequestPhoneOtpUseCase
import dev.core.domain.usecase.ResetPasswordUseCase
import dev.core.domain.usecase.VerifyPhoneOtpUseCase
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.repository.ProfileExistence
import dev.feature.profile.domain.usecase.HasProfileUseCase
import dev.feature.profile.domain.usecase.ObserveProfileUseCase
import dev.feature.profile.domain.usecase.SaveProfileUseCase
import dev.feature.profile.domain.usecase.UploadAvatarUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI navigatsiyasini boshqaradigan bir martalik hodisalar. */
sealed interface AuthEvent {
    /** Ro'yxatdan o'tildi va raqamga SMS kod ketdi — tasdiqlash ekraniga. */
    data object PhoneVerificationSent : AuthEvent

    /** Raqam tasdiqlandi — endi hisob ma'lumotlari (ism, familiya, rasm, email). */
    data object AccountSetupRequired : AuthEvent

    /** Parolni tiklash kodi yuborildi — yangi parol ekraniga. */
    data object ResetCodeSent : AuthEvent

    /** Parol yangilandi — kirish ekraniga qaytamiz. */
    data object PasswordReset : AuthEvent

    /** Ro'yxat bekor qilindi (orqaga) — sessiya tozalandi, kirish ekraniga. */
    data object SignupCancelled : AuthEvent

    /** To'liq kirildi (telefon/parol yoki Google) — bosh sahifaga o'tish. */
    data class Authenticated(val user: User) : AuthEvent
}

/** Ilova ochilishida qaysi ekran birinchi ko'rinishi. */
enum class AuthStart { LOGIN, VERIFY_PHONE, ACCOUNT_SETUP, HOME }

/**
 * Auth oqimining forma holatini va biznes-amallarini boshqaradi.
 *
 * Backend oqimi (`/v1/auth/business/…`):
 * - **kirish** — telefon + parol yoki Google (`oauth/google`);
 * - **ro'yxat** — telefon + parol → SMS kod → ism/familiya (+ rasm, email);
 * - **parolni tiklash** — raqamga SMS kod → kod + yangi parol.
 *
 * MUHIM — ro'yxatdan o'tish **uch qadamli, uzilmaydigan** oqim. `POST /register` hisobni
 * darhol ochib sessiya beradi, lekin bu foydalanuvchi uchun "kirdim" degani emas: raqam
 * tasdiqlanmagan (OTP chaqiruvlari sessiyani talab qiladi, shu bois kod ro'yxatdan
 * OLDIN so'ralishi mumkin emas) va profilда ismi yo'q. Shuning uchun oqim tugagunча
 * bosh ekranga o'tkazmaymiz va bosqichni [SignupStage] bayrog'ida saqlaymiz — ilova
 * yopilib qayta ochilsa ham o'sha joydan davom etadi.
 *
 * Email bilan kirish/ro'yxatdan o'tish ilovadan olib tashlangan — muqobil usul Google.
 *
 * Token yangilash bu yerda emas — u tarmoq qatlamida avtomatik.
 */
class AuthFlowViewModel(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val registerUseCase: RegisterUseCase,
    private val requestRegistrationOtpUseCase: RequestRegistrationOtpUseCase,
    private val requestPhoneOtpUseCase: RequestPhoneOtpUseCase,
    private val verifyPhoneOtpUseCase: VerifyPhoneOtpUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val hasProfileUseCase: HasProfileUseCase,
    private val saveProfileUseCase: SaveProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthFlowState())
    val state: StateFlow<AuthFlowState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    /**
     * Ilova ochilishida qaysi ekrandan boshlash (`null` — hali o'qilmoqda, splash).
     *
     * Sessiya bor bo'lishi yetarli emas: tugallanmagan ro'yxat bosqichi bo'lsa oqim
     * o'sha joydan davom etadi. Aks holда `register` dan keyin ilova yopilsa, foydalanuvchi
     * raqami tasdiqlanmagan va ismsiz holда bosh ekranga tushib qolardi.
     */
    val start: StateFlow<AuthStart?> = combine(
        observeCurrentUserUseCase(),
        settingsRepository.observeValue(SettingsRepository.KEY_SIGNUP_STAGE),
    ) { user, stage ->
        when {
            user == null -> AuthStart.LOGIN
            SignupStage.parse(stage) == SignupStage.OTP -> AuthStart.VERIFY_PHONE
            SignupStage.parse(stage) == SignupStage.PROFILE -> AuthStart.ACCOUNT_SETUP
            else -> AuthStart.HOME
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var timerJob: Job? = null

    init {
        // Oqim yarmida ilova qayta ochilgan bo'lsa formani tiklaymiz: OTP ekrani qaysi
        // raqamga kod ketganini, hisob ekrani esa qisman to'ldirilgan profilni ko'rsatsin.
        viewModelScope.launch {
            val stage = SignupStage.parse(
                settingsRepository.observeValue(SettingsRepository.KEY_SIGNUP_STAGE).first(),
            ) ?: return@launch
            val phone = observeCurrentUserUseCase().first()?.phoneNumber
            val profile = observeProfileUseCase().first()
            _state.update {
                it.copy(
                    phone = phone?.filter(Char::isDigit)?.takeLast(9) ?: it.phone,
                    otpPurpose = OtpPurpose.VERIFY_PHONE,
                    firstName = profile?.firstName.orEmpty(),
                    lastName = profile?.lastName.orEmpty(),
                    email = profile?.email.orEmpty(),
                    avatarUrl = profile?.avatarUrl,
                )
            }
            // Kod eskirgan bo'lishi mumkin — qayta yuborish tugmasi darhol faol bo'lsin.
            if (stage == SignupStage.OTP) _state.update { it.copy(resendSeconds = 0) }
        }
    }

    // --- Forma yangilanishlari ---
    fun onPhoneChange(v: String) = _state.update {
        it.copy(phone = v.filter { c -> c.isDigit() }.take(9), error = null)
    }

    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _state.update { it.copy(confirmPassword = v, error = null) }
    fun togglePasswordVisible() = _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun toggleTerms() = _state.update { it.copy(termsAccepted = !it.termsAccepted, error = null) }

    fun onOtpChange(v: String) = _state.update {
        it.copy(otp = v.filter { c -> c.isDigit() }.take(OTP_CODE_LENGTH), error = null)
    }

    fun clearError() = _state.update { it.copy(error = null, info = null) }

    // ------------------------------------------------------------------
    // Kirish
    // ------------------------------------------------------------------

    /** Biznes kirish ekrani — telefon + parol. */
    fun loginWithPhone() {
        val s = _state.value
        if (s.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = loginUseCase("+998${s.phoneDigits}", s.password)) {
                is Resource.Success -> finishLogin(result.data)
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    /** Google ID token bilan kirish (login ekranidagi "Google bilan kirish" tugmasi). */
    fun signInWithGoogle(idToken: String) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = loginWithGoogleUseCase(idToken)) {
                is Resource.Success -> finishLogin(result.data)
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    /** Tashqi (platform) oqim xatosini ekranга ko'rsatadi — masalan Google bekor qilinди/mavjud emas. */
    fun showAuthError(message: String) = _state.update { it.copy(isLoading = false, error = message) }

    // ------------------------------------------------------------------
    // Ro'yxatdan o'tish
    // ------------------------------------------------------------------

    /**
     * Ro'yxatdan o'tishning 1-qadami — **hisob hali yaratilmaydi**, faqat raqamga kod ketadi
     * (`POST /auth/business/register/otp`).
     *
     * Tartib ataylab shunday: raqam bazada unique, shuning uchun backend tasdiqsiz ro'yxatni
     * umuman qabul qilmaydi (`otpCode` siz `422`). Ilgari avval hisob ochilar, kod esa keyin
     * so'ralardi — begona raqamni yozgan odam o'sha raqamni butunlay band qilib qo'yishi
     * mumkin edi.
     *
     * Kod ketmasa tasdiqlash ekraniga **o'tilmaydi**: kiritadigan kod yo'q.
     */
    fun register() {
        val s = _state.value
        if (s.isLoading) return
        if (!s.termsAccepted) {
            _state.update { it.copy(error = "Shartlarga rozilik bering.") }
            return
        }
        if (s.password != s.confirmPassword) {
            _state.update { it.copy(error = "Parollar mos kelmadi.") }
            return
        }
        if (s.password.length < MIN_PASSWORD_LENGTH) {
            _state.update { it.copy(error = "Parol kamida $MIN_PASSWORD_LENGTH belgidan iborat bo'lsin") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val sent = requestRegistrationOtpUseCase("+998${s.phoneDigits}")) {
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = sent.message) }
                Resource.Loading -> Unit
                is Resource.Success -> {
                    // Bosqich SAQLANMAYDI (`setSignupStage` yo'q): hisob ham, sessiya ham hali
                    // yo'q va parol faqat xotirada. Ilova shu yerда yopilsa, oqimni kirish
                    // ekranidan boshlash — yarim holatni tiklashga urinishdan to'g'riroq.
                    _state.update {
                        it.copy(isLoading = false, otp = "", otpPurpose = OtpPurpose.REGISTER)
                    }
                    startResendTimer(sent.data.resendCooldownSeconds)
                    _events.send(AuthEvent.PhoneVerificationSent)
                }
            }
        }
    }

    /**
     * Ro'yxatning 2-qadami — kod bilan hisob yaratiladi (`register` + `otpCode`).
     *
     * Muvaffaqiyatda raqam allaqachon tasdiqlangan (`phoneVerified: true`), shuning uchun
     * ilgarigi qo'shimcha `otp/request` → `otp/verify` qadami umuman bo'lmaydi.
     */
    private fun completeRegistration() {
        val s = _state.value
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = registerUseCase("+998${s.phoneDigits}", s.password, s.otp)) {
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
                is Resource.Success -> {
                    // Hisob ochildi — endi profil qadami (ilova yopilsa ham shu yerdan davom).
                    setSignupStage(SignupStage.PROFILE)
                    _state.update { it.copy(isLoading = false, otp = "") }
                    _events.send(AuthEvent.AccountSetupRequired)
                }
            }
        }
    }

    /**
     * Raqamni tasdiqlash uchun SMS kod so'raydi. Kod ketmasa xato holatда qoladi — chaqiruvchi
     * ekranni o'zi hal qiladi (ro'yxatда baribir tasdiqlash ekraniga o'tiladi).
     */
    private suspend fun requestPhoneOtp() {
        when (val sent = requestPhoneOtpUseCase("+998${_state.value.phoneDigits}")) {
            is Resource.Success -> {
                _state.update { it.copy(isLoading = false, error = null) }
                startResendTimer(sent.data.resendCooldownSeconds)
            }
            is Resource.Error -> _state.update {
                it.copy(isLoading = false, resendSeconds = 0, error = sent.message)
            }
            Resource.Loading -> Unit
        }
    }

    /**
     * Kod tugmasi — maqsadga qarab ikki xil ish qiladi:
     * ro'yxatда hisob YARATADI (kod `register` ichida ketadi), mavjud hisobда esa raqamni
     * tasdiqlaydi (`otp/verify`).
     */
    fun verifyPhone() {
        val s = _state.value
        if (s.isLoading) return
        if (s.otpPurpose == OtpPurpose.REGISTER) {
            completeRegistration()
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = verifyPhoneOtpUseCase("+998${s.phoneDigits}", s.otp)) {
                is Resource.Success -> {
                    setSignupStage(SignupStage.PROFILE)
                    _state.update { it.copy(isLoading = false, otp = "") }
                    _events.send(AuthEvent.AccountSetupRequired)
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    /**
     * Ro'yxatni yarim yo'lda bekor qiladi (tasdiqlash yoki hisob ekranidagi "orqaga").
     *
     * Sessiya tozalanadi va kirish ekraniga qaytamiz. Hisob serverда qoladi — foydalanuvchi
     * keyinroq o'sha raqam va parol bilan kiraveradi; qayta ro'yxatdan o'tishga urinsa
     * backend "raqam band" der edi, shuning uchun bu yerда `logout` to'g'ri yechim.
     */
    fun cancelSignup() {
        val pendingRegistration = _state.value.otpPurpose == OtpPurpose.REGISTER
        viewModelScope.launch {
            // Kod bosqichida hisob hali yaratilmagan — chiqadigan sessiya ham yo'q.
            if (!pendingRegistration) logoutUseCase()
            setSignupStage(null)
            _state.update {
                AuthFlowState(phone = it.phone) // raqam qolsin — kirish ekranida qayta terilmasin
            }
            _events.send(AuthEvent.SignupCancelled)
        }
    }

    // ------------------------------------------------------------------
    // Hisob ma'lumotlari (ro'yxatning oxirgi qadami)
    // ------------------------------------------------------------------

    fun onFirstNameChange(v: String) = _state.update { it.copy(firstName = v, error = null) }
    fun onLastNameChange(v: String) = _state.update { it.copy(lastName = v, error = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v.trim(), error = null) }

    /**
     * Profil rasmini yuklaydi. Rasm darhol serverга ketadi va URL holatда saqlanadi —
     * "Davom etish" bosilganda profil bitta so'rov bilan to'liq saqlanadi.
     */
    fun uploadAvatar(bytes: ByteArray, fileName: String) {
        _state.update { it.copy(avatarUploading = true, error = null) }
        viewModelScope.launch {
            when (val result = uploadAvatarUseCase(bytes, fileName)) {
                is Resource.Success -> _state.update {
                    it.copy(avatarUploading = false, avatarUrl = result.data)
                }
                is Resource.Error -> _state.update {
                    it.copy(avatarUploading = false, error = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    /** 3-qadam — ism/familiya (majburiy) + email/rasm (ixtiyoriy) saqlanadi va oqim tugaydi. */
    fun completeAccountSetup() {
        val s = _state.value
        if (s.isLoading) return
        val first = s.firstName.trim()
        val last = s.lastName.trim()
        if (first.isEmpty() || last.isEmpty()) {
            _state.update { it.copy(error = "Ism va familiyani kiriting.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val existing = observeProfileUseCase().first() ?: UserProfile()
            val profile = existing.copy(
                firstName = first,
                lastName = last,
                phoneNumber = existing.phoneNumber ?: "+998${s.phoneDigits}",
                email = s.email.trim().ifBlank { null },
                avatarUrl = s.avatarUrl ?: existing.avatarUrl,
            )
            when (val saved = saveProfileUseCase(profile)) {
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = saved.message) }
                Resource.Loading -> Unit
                is Resource.Success -> {
                    setSignupStage(null)
                    val user = observeCurrentUserUseCase().first()
                    if (user != null) {
                        finishAuthenticated(user)
                    } else {
                        _state.update {
                            it.copy(isLoading = false, error = "Sessiya topilmadi. Qaytadan kiring.")
                        }
                    }
                }
            }
        }
    }

    private suspend fun setSignupStage(stage: SignupStage?) =
        settingsRepository.setValue(SettingsRepository.KEY_SIGNUP_STAGE, stage?.name)

    // ------------------------------------------------------------------
    // Parolni tiklash
    // ------------------------------------------------------------------

    /** 1-qadam — raqamga SMS kod. */
    fun requestPasswordReset() {
        val s = _state.value
        if (s.isLoading) return
        _state.update { it.copy(isLoading = true, error = null, info = null) }
        viewModelScope.launch {
            when (val result = forgotPasswordUseCase("+998${s.phoneDigits}")) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            otp = "",
                            password = "",
                            confirmPassword = "",
                            otpPurpose = OtpPurpose.RESET_PASSWORD,
                        )
                    }
                    startResendTimer(DEFAULT_RESEND_SECONDS)
                    _events.send(AuthEvent.ResetCodeSent)
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    /** 2-qadam — kod + yangi parol. */
    fun resetPassword() {
        val s = _state.value
        if (s.isLoading) return
        if (s.password != s.confirmPassword) {
            _state.update { it.copy(error = "Parollar mos kelmadi.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = resetPasswordUseCase("+998${s.phoneDigits}", s.otp, s.password)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(isLoading = false, otp = "", password = "", confirmPassword = "")
                    }
                    _events.send(AuthEvent.PasswordReset)
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    // ------------------------------------------------------------------
    // Kodni qayta yuborish
    // ------------------------------------------------------------------

    fun resendCode() {
        val s = _state.value
        if (s.isLoading || s.resendSeconds > 0) return
        when (s.otpPurpose) {
            OtpPurpose.RESET_PASSWORD -> requestPasswordReset()
            // Ro'yxatда hisob yo'q — kod `register/otp` dan qayta so'raladi.
            OtpPurpose.REGISTER -> register()
            OtpPurpose.VERIFY_PHONE -> {
                _state.update { it.copy(isLoading = true, error = null) }
                viewModelScope.launch { requestPhoneOtp() }
            }
        }
    }

    /** Backend bergan kutish oralig'i bo'yicha taymer. */
    private fun startResendTimer(seconds: Int) {
        timerJob?.cancel()
        _state.update { it.copy(resendSeconds = seconds.coerceAtLeast(0)) }
        timerJob = viewModelScope.launch {
            while (_state.value.resendSeconds > 0) {
                delay(1000)
                _state.update { it.copy(resendSeconds = (it.resendSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    // ------------------------------------------------------------------

    /**
     * Kirish tugadi — lekin hisobда ism bo'lmasa avval uni so'raymiz.
     *
     * Nega kerak: ilovaning hamma joyida ism ko'rinadi (biznes kartasi, e'lon, suhbat), va
     * ro'yxat oqimi yarim yo'lda tashlab ketilgan bo'lishi mumkin — hisob serverда ochilgan,
     * profil esa bo'sh. Bunda foydalanuvchi ismsiz ichkariga kirsa, uni hech qayerда to'ldirishga
     * majburlamas edik.
     *
     * Noaniq holatда ([ProfileExistence.ERROR] — tarmoq/token muammosi) **ushlab qolmaymiz**:
     * profil bor-yo'qligini bilmasak, bekorga forma ko'rsatgandan ko'ra ichkariga kiritgan
     * yaxshi.
     */
    private suspend fun finishLogin(user: User) {
        val needsSetup = when (hasProfileUseCase()) {
            ProfileExistence.MISSING -> true
            ProfileExistence.EXISTS -> observeProfileUseCase().first()?.displayName.isNullOrBlank()
            ProfileExistence.ERROR -> false
        }
        if (!needsSetup) {
            finishAuthenticated(user)
            return
        }
        setSignupStage(SignupStage.PROFILE)
        // MUHIM — tartib: avval hodisa, keyin formani tozalash.
        //
        // Aks holда kirish ekrani bir necha kadr davomida BO'SH parol maydoni bilan qayta
        // chiziladi (navigatsiya asinxron — hodisa yig'uvchi keyingi kadrда ishlaydi) va
        // foydalanuvchi "parolim o'chib ketdi, keyin nimadir bo'ldi" degan sakrashni ko'radi.
        // `isLoading` ham shu paytgacha `true` qoladi: tugma kutish holatida turaveradi.
        _events.send(AuthEvent.AccountSetupRequired)
        _state.update {
            it.copy(isLoading = false, password = "", confirmPassword = "", otp = "")
        }
    }

    private suspend fun finishAuthenticated(user: User) {
        // Rolni local saqlaymiz — ildiz router keyingi ochilishda shu bo'yicha yo'naltiradi.
        settingsRepository.setValue(SettingsRepository.KEY_SELECTED_ROLE, Role.BUSINESS.name)
        // Tozalash navigatsiyadan KEYIN — sababi yuqorida ([finishLogin]).
        _events.send(AuthEvent.Authenticated(user))
        _state.update { it.copy(isLoading = false, password = "", confirmPassword = "") }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}

/** Backend cheklovi noma'lum bo'lganda ishlatiladigan standart kutish oralig'i. */
private const val DEFAULT_RESEND_SECONDS = 60
