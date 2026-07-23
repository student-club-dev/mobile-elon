package dev.feature.auth.presentation.flow

/** Ro'yxatdan o'tuvchi roli. ElonUz — biznes ilovasi, lekin ildiz router shu turni ishlatadi. */
enum class Role { STUDENT, BUSINESS, EMPLOYER, UNIVERSITY }

/**
 * SMS kod nima uchun so'ralgani — bitta kod ekrani ikkala oqimga xizmat qiladi.
 *
 * Backend'da SMS kod **kirish usuli emas**: u faqat raqamni tasdiqlash
 * (`/auth/business/otp/…`) va parolni tiklash (`/auth/business/password/…`) uchun.
 */
enum class OtpPurpose { VERIFY_PHONE, RESET_PASSWORD }

/** Butun auth oqimining forma holati. */
data class AuthFlowState(
    /** Telefon — faqat 9 xonali local qism ("901234567"), `+998` prefiksi UI'da. */
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    /** Ro'yxatdan o'tishda tanlangan usul — telefon yoki email. */
    val registerWithEmail: Boolean = false,
    val termsAccepted: Boolean = false,
    // SMS kod
    val otp: String = "",
    val otpPurpose: OtpPurpose = OtpPurpose.VERIFY_PHONE,
    /** Qayta yuborishgacha qolgan soniya (backend `resendCooldownSeconds` dan). */
    val resendSeconds: Int = 0,
    // Umumiy
    val isLoading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
) {
    val phoneDigits: String get() = phone.filter { it.isDigit() }.take(9)
    val phoneValid: Boolean get() = phoneDigits.length == 9
    val otpValid: Boolean get() = otp.length == OTP_CODE_LENGTH

    /** Kirish tugmasi faolmi — telefon to'liq va parol bo'sh emas. */
    val phoneLoginReady: Boolean get() = phoneValid && password.isNotBlank() && !isLoading

    /** Email bilan kirish tayyormi. */
    val emailLoginReady: Boolean get() = email.contains('@') && password.isNotBlank() && !isLoading

    /** Ro'yxatdan o'tish tayyormi — usulga mos maydon to'ldirilgan va parollar mos. */
    val registerReady: Boolean
        get() = !isLoading && termsAccepted &&
            password.length >= MIN_PASSWORD_LENGTH && password == confirmPassword &&
            if (registerWithEmail) email.contains('@') else phoneValid

    /** Parolni tiklash tayyormi — kod to'liq va yangi parollar mos. */
    val resetReady: Boolean
        get() = !isLoading && otpValid &&
            password.length >= MIN_PASSWORD_LENGTH && password == confirmPassword
}

/** Backend 6 xonali kod yuboradi. */
const val OTP_CODE_LENGTH = 6

/** Backend `RegisterDto.password.minLength` bilan bir xil. */
const val MIN_PASSWORD_LENGTH = 8
