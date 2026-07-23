package dev.feature.auth.presentation.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.common.Resource
import dev.core.domain.model.User
import dev.core.domain.repository.SettingsRepository
import dev.core.domain.usecase.ForgotPasswordUseCase
import dev.core.domain.usecase.LoginUseCase
import dev.core.domain.usecase.ObserveCurrentUserUseCase
import dev.core.domain.usecase.RegisterUseCase
import dev.core.domain.usecase.RequestPhoneOtpUseCase
import dev.core.domain.usecase.ResetPasswordUseCase
import dev.core.domain.usecase.VerifyPhoneOtpUseCase
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
    /** Hisob yaratildi va raqamga SMS kod ketdi — tasdiqlash ekraniga. */
    data object PhoneVerificationSent : AuthEvent

    /** Parolni tiklash kodi yuborildi — yangi parol ekraniga. */
    data object ResetCodeSent : AuthEvent

    /** Parol yangilandi — kirish ekraniga qaytamiz. */
    data object PasswordReset : AuthEvent

    /** To'liq kirildi (kirish/ro'yxat/tasdiqlash) — bosh sahifaga o'tish. */
    data class Authenticated(val user: User) : AuthEvent
}

/**
 * Auth oqimining forma holatini va biznes-amallarini boshqaradi.
 *
 * Backend oqimi (`/v1/auth/business/…`):
 * - **kirish** — telefon yoki email + parol;
 * - **ro'yxat** — telefon yoki email + parol; telefon bilan bo'lsa keyin SMS kod bilan
 *   raqam tasdiqlanadi (hisob esa allaqachon ochilgan);
 * - **parolni tiklash** — raqamga SMS kod → kod + yangi parol.
 *
 * Token yangilash bu yerda emas — u tarmoq qatlamida avtomatik.
 */
class AuthFlowViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val requestPhoneOtpUseCase: RequestPhoneOtpUseCase,
    private val verifyPhoneOtpUseCase: VerifyPhoneOtpUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthFlowState())
    val state: StateFlow<AuthFlowState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    /**
     * Ilova ochilishida avtomatik kirish holati (local sessiya keshidan):
     * `null` — hali tekshirilmoqda (splash), `true` — kirgan (HOME), `false` — kirmagan.
     */
    val loggedIn: StateFlow<Boolean?> = observeCurrentUserUseCase()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var timerJob: Job? = null

    // --- Forma yangilanishlari ---
    fun onPhoneChange(v: String) = _state.update {
        it.copy(phone = v.filter { c -> c.isDigit() }.take(9), error = null)
    }

    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null, info = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _state.update { it.copy(confirmPassword = v, error = null) }
    fun togglePasswordVisible() = _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun toggleTerms() = _state.update { it.copy(termsAccepted = !it.termsAccepted, error = null) }
    fun onRegisterMethodChange(email: Boolean) = _state.update { it.copy(registerWithEmail = email, error = null) }

    fun onOtpChange(v: String) = _state.update {
        it.copy(otp = v.filter { c -> c.isDigit() }.take(OTP_CODE_LENGTH), error = null)
    }

    fun clearError() = _state.update { it.copy(error = null, info = null) }

    // ------------------------------------------------------------------
    // Kirish
    // ------------------------------------------------------------------

    /** Biznes kirish ekrani — telefon + parol. */
    fun loginWithPhone() = login("+998${_state.value.phoneDigits}")

    /** Email kirish ekrani — email + parol. */
    fun loginWithEmail() = login(_state.value.email)

    private fun login(identifier: String) {
        val s = _state.value
        if (s.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = loginUseCase(identifier, s.password)) {
                is Resource.Success -> finishAuthenticated(result.data)
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    // ------------------------------------------------------------------
    // Ro'yxatdan o'tish
    // ------------------------------------------------------------------

    /**
     * Hisob yaratadi. Telefon bilan bo'lsa — hisob darhol ochiladi va raqamni tasdiqlash uchun
     * SMS kod so'raladi (foydalanuvchi uni o'tkazib yuborsa ham ilovaga kirgan bo'ladi).
     * Email bilan bo'lsa — to'g'ridan-to'g'ri bosh sahifaga.
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
        val identifier = if (s.registerWithEmail) s.email else "+998${s.phoneDigits}"
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = registerUseCase(identifier, s.password)) {
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
                is Resource.Success ->
                    if (s.registerWithEmail) {
                        finishAuthenticated(result.data)
                    } else {
                        // Hisob ochildi — endi raqamni tasdiqlaymiz (sessiya allaqachon bor,
                        // shuning uchun OTP so'rovi avtorizatsiya bilan ketadi).
                        _state.update { it.copy(otp = "", otpPurpose = OtpPurpose.VERIFY_PHONE) }
                        requestPhoneOtp(onFailure = { finishAuthenticated(result.data) })
                    }
            }
        }
    }

    /**
     * Raqamni tasdiqlash uchun SMS kod so'raydi. Kod ketmasa (masalan SMS xizmati javob bermasa)
     * [onFailure] chaqiriladi — hisob baribir ochilgani uchun foydalanuvchini ushlab qolmaymiz.
     */
    private suspend fun requestPhoneOtp(onFailure: suspend () -> Unit) {
        when (val sent = requestPhoneOtpUseCase("+998${_state.value.phoneDigits}")) {
            is Resource.Success -> {
                _state.update { it.copy(isLoading = false) }
                startResendTimer(sent.data.resendCooldownSeconds)
                _events.send(AuthEvent.PhoneVerificationSent)
            }
            is Resource.Error -> onFailure()
            Resource.Loading -> Unit
        }
    }

    /** Tasdiqlash ekrani — kodni tekshiradi va bosh sahifaga o'tadi. */
    fun verifyPhone() {
        val s = _state.value
        if (s.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = verifyPhoneOtpUseCase("+998${s.phoneDigits}", s.otp)) {
                is Resource.Success -> {
                    val user = observeCurrentUserUseCase().first()
                    if (user != null) {
                        finishAuthenticated(user)
                    } else {
                        _state.update { it.copy(isLoading = false, error = "Sessiya topilmadi. Qaytadan kiring.") }
                    }
                }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    /** Tasdiqlashni o'tkazib yuborish — hisob allaqachon ochilgan. */
    fun skipPhoneVerification() {
        viewModelScope.launch {
            observeCurrentUserUseCase().first()?.let { finishAuthenticated(it) }
        }
    }

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
            OtpPurpose.VERIFY_PHONE -> {
                _state.update { it.copy(isLoading = true, error = null) }
                viewModelScope.launch {
                    requestPhoneOtp(
                        onFailure = {
                            _state.update {
                                it.copy(isLoading = false, error = "Kodni yuborib bo'lmadi. Qayta urining.")
                            }
                        },
                    )
                }
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
    // Biometrika
    // ------------------------------------------------------------------

    /**
     * Biometrik tasdiqdan (Face ID / barmoq izi) so'ng chaqiriladi — local keshda sessiya bo'lsa
     * to'g'ridan-to'g'ri HOME'ga, aks holda xato xabari (avval oddiy kirish kerak).
     */
    fun onBiometricAuthenticated() {
        viewModelScope.launch {
            val user = observeCurrentUserUseCase().first()
            if (user != null) {
                finishAuthenticated(user)
            } else {
                _state.update {
                    it.copy(error = "Saqlangan hisob topilmadi. Avval telefon/parol bilan kiring.")
                }
            }
        }
    }

    /** Biometrik oqim xatosi (mavjud emas / tanilmadi) uchun xabar. */
    fun biometricError(message: String) = _state.update { it.copy(error = message) }

    // ------------------------------------------------------------------

    private suspend fun finishAuthenticated(user: User) {
        // Rolni local saqlaymiz — ildiz router keyingi ochilishda shu bo'yicha yo'naltiradi.
        settingsRepository.setValue(SettingsRepository.KEY_SELECTED_ROLE, Role.BUSINESS.name)
        _state.update { it.copy(isLoading = false, password = "", confirmPassword = "") }
        _events.send(AuthEvent.Authenticated(user))
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}

/** Backend cheklovi noma'lum bo'lganda ishlatiladigan standart kutish oralig'i. */
private const val DEFAULT_RESEND_SECONDS = 60
