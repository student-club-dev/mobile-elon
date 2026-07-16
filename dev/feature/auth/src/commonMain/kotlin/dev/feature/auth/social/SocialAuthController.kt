package dev.feature.auth.social

import androidx.compose.runtime.Composable
import dev.core.domain.model.ExternalAuthUser

/**
 * Google Sign-In va Telefon (OTP) autentifikatsiyasining platformaga bog'liq qismi.
 * Android va iOS uchun `actual` implementatsiyalari mavjud.
 *
 * Google va telefon uchun ham yakuniy natija — Firebase orqali autentifikatsiyadan
 * o'tgan [ExternalAuthUser]. Uni ViewModel `SyncExternalUserUseCase` orqali domen
 * `User` ga aylantiradi.
 */
interface SocialAuthController {

    /** Google hisob tanlash oynasini ochadi va Firebase'ga kirtadi. */
    suspend fun signInWithGoogle(): SocialAuthResult

    /** Apple "Sign in with Apple" oqimini ochadi va Firebase'ga kiritadi. */
    suspend fun signInWithApple(): SocialAuthResult

    /**
     * Telegram Login oqimini ochadi (web) — Cloud Function custom token qaytaradi,
     * shu token bilan Firebase'ga kiriladi.
     */
    suspend fun signInWithTelegram(): SocialAuthResult

    /**
     * [phoneNumber] (E.164, masalan "+998901234567") ga SMS kod yuboradi.
     * Android'da avtomatik tasdiqlanishi mumkin ([OtpSendResult.AutoVerified]).
     */
    suspend fun sendOtp(phoneNumber: String): OtpSendResult

    /** Foydalanuvchi kiritgan [code] (SMS kod) ni tekshiradi. */
    suspend fun confirmOtp(code: String): SocialAuthResult
}

sealed interface SocialAuthResult {
    data class Success(val user: ExternalAuthUser) : SocialAuthResult
    data object Cancelled : SocialAuthResult
    data class Error(val message: String) : SocialAuthResult
}

sealed interface OtpSendResult {
    /** SMS yuborildi — endi foydalanuvchidan kod so'raladi. */
    data object Sent : OtpSendResult

    /** Android SMS'ni o'zi o'qib avtomatik kirdi. */
    data class AutoVerified(val user: ExternalAuthUser) : OtpSendResult

    data class Error(val message: String) : OtpSendResult
}

/** Composable ichida platformaga mos [SocialAuthController] ni beradi. */
@Composable
expect fun rememberSocialAuthController(): SocialAuthController
