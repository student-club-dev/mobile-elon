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

/**
 * Ro'yxatdan o'tish qayerда to'xtaganini bildiradi — ilova yopilib qayta ochilsa ham oqim
 * o'sha joydan davom etadi (`SettingsRepository.KEY_SIGNUP_STAGE` da saqlanadi).
 *
 * Backend hisobni `register` da darhol ochib sessiya beradi, shuning uchun "kirgan" degani
 * hali "ro'yxatdan o'tib bo'lgan" degani emas.
 */
enum class SignupStage {
    /** Hisob ochilgan, raqam hali tasdiqlanmagan. */
    OTP,

    /** Raqam tasdiqlangan, ism-familiya hali kiritilmagan. */
    PROFILE,
    ;

    companion object {
        fun parse(value: String?): SignupStage? = entries.firstOrNull { it.name == value }
    }
}

/** Butun auth oqimining forma holati. */
data class AuthFlowState(
    /** Telefon — faqat 9 xonali local qism ("901234567"), `+998` prefiksi UI'da. */
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val termsAccepted: Boolean = false,
    // SMS kod
    val otp: String = "",
    val otpPurpose: OtpPurpose = OtpPurpose.VERIFY_PHONE,
    /** Qayta yuborishgacha qolgan soniya (backend `resendCooldownSeconds` dan). */
    val resendSeconds: Int = 0,
    // Hisob yaratish (OTP dan keyingi oxirgi qadam)
    val firstName: String = "",
    val lastName: String = "",
    /** Aloqa emaili — ixtiyoriy. */
    val email: String = "",
    /** Tanlangan rasm yuklanmoqda — tugma kutadi, chunki URL profil bilan birga saqlanadi. */
    val avatarUploading: Boolean = false,
    /** Yuklangan rasm manzili (`POST /media/upload` qaytargan). */
    val avatarUrl: String? = null,
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

    /** Ro'yxatdan o'tish tayyormi — raqam to'liq, parollar mos va shartlar qabul qilingan. */
    val registerReady: Boolean
        get() = !isLoading && termsAccepted && phoneValid &&
            password.length >= MIN_PASSWORD_LENGTH && password == confirmPassword

    /** Parolni tiklash tayyormi — kod to'liq va yangi parollar mos. */
    val resetReady: Boolean
        get() = !isLoading && otpValid &&
            password.length >= MIN_PASSWORD_LENGTH && password == confirmPassword

    /**
     * Hisob yaratish tayyormi — **ism va familiya majburiy**, email va rasm ixtiyoriy.
     * Rasm yuklanayotganда kutamiz: aks holда profil rasmsiz saqlanib, URL yo'qolardi.
     */
    val accountSetupReady: Boolean
        get() = !isLoading && !avatarUploading &&
            firstName.trim().isNotEmpty() && lastName.trim().isNotEmpty()
}

/** Backend 6 xonali kod yuboradi. */
const val OTP_CODE_LENGTH = 6

/** Backend `RegisterDto.password.minLength` bilan bir xil. */
const val MIN_PASSWORD_LENGTH = 8
