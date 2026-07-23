package dev.core.domain.usecase

import dev.core.common.Resource
import dev.core.domain.model.AuthIdentifier
import dev.core.domain.model.DeviceSession
import dev.core.domain.model.OtpChallenge
import dev.core.domain.model.User
import dev.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Autentifikatsiya biznes-amallari — ViewModel faqat shu use-case'larni chaqiradi.
 *
 * Validatsiya shu yerda, bitta joyда: ekran ham, kelajakdagi boshqa chaqiruvchi ham bir xil
 * qoidaga bo'ysunadi. Backend qoidalari (`RegisterDto.password.minLength = 8`) bilan mos.
 */
private const val MIN_PASSWORD_LENGTH = 8

/** Telefon yoki email + parol bilan kirish. */
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(login: String, password: String): Resource<User> {
        val identifier = AuthIdentifier.of(login)
            ?: return Resource.Error("Telefon raqami yoki email manzilini to'g'ri kiriting")
        if (password.isBlank()) return Resource.Error("Parolni kiriting")
        return repository.login(identifier, password)
    }
}

/** Telefon yoki email + parol bilan yangi hisob yaratish. */
class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(login: String, password: String): Resource<User> {
        val identifier = AuthIdentifier.of(login)
            ?: return Resource.Error("Telefon raqami yoki email manzilini to'g'ri kiriting")
        if (password.length < MIN_PASSWORD_LENGTH) {
            return Resource.Error("Parol kamida $MIN_PASSWORD_LENGTH belgidan iborat bo'lsin")
        }
        return repository.register(identifier, password)
    }
}

/** Tizimdan chiqish — refresh token bekor qilinadi, local kesh tozalanadi. */
class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}

/**
 * Local keshdagi joriy foydalanuvchini reaktiv kuzatadi.
 * Ilova ochilishida avtomatik kirish (session restore) uchun ishlatiladi.
 */
class ObserveCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<User?> = repository.observeCurrentUser()
}

/** Raqamni tasdiqlash uchun SMS kod so'raydi (hisob yaratilgandan keyin). */
class RequestPhoneOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(phone: String): Resource<OtpChallenge> {
        val identifier = AuthIdentifier.of(phone) as? AuthIdentifier.Phone
            ?: return Resource.Error("To'liq 9 xonali raqam kiriting")
        return repository.requestPhoneOtp(identifier.value)
    }
}

/** SMS kodni tekshiradi — raqam tasdiqlanadi. */
class VerifyPhoneOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(phone: String, code: String): Resource<Unit> {
        val identifier = AuthIdentifier.of(phone) as? AuthIdentifier.Phone
            ?: return Resource.Error("To'liq 9 xonali raqam kiriting")
        if (code.length != OTP_LENGTH) return Resource.Error("$OTP_LENGTH xonali kodni kiriting")
        return repository.verifyPhoneOtp(identifier.value, code)
    }
}

/** Parolni tiklash: 1-qadam — raqamga SMS kod. */
class ForgotPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(phone: String): Resource<Unit> {
        val identifier = AuthIdentifier.of(phone) as? AuthIdentifier.Phone
            ?: return Resource.Error("Parolni tiklash uchun telefon raqamini kiriting")
        return repository.forgotPassword(identifier.value)
    }
}

/** Parolni tiklash: 2-qadam — kod + yangi parol. */
class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(phone: String, code: String, newPassword: String): Resource<Unit> {
        val identifier = AuthIdentifier.of(phone) as? AuthIdentifier.Phone
            ?: return Resource.Error("Telefon raqamini to'g'ri kiriting")
        if (code.length != OTP_LENGTH) return Resource.Error("$OTP_LENGTH xonali kodni kiriting")
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return Resource.Error("Parol kamida $MIN_PASSWORD_LENGTH belgidan iborat bo'lsin")
        }
        return repository.resetPassword(identifier.value, code, newPassword)
    }
}

/** Kirgan holatda parolni o'rnatish/almashtirish (Sozlamalar). */
class SetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(currentPassword: String?, newPassword: String): Resource<Unit> {
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return Resource.Error("Parol kamida $MIN_PASSWORD_LENGTH belgidan iborat bo'lsin")
        }
        return repository.setPassword(currentPassword?.ifBlank { null }, newPassword)
    }
}

/** Hisobga kirilgan qurilmalar ro'yxati. */
class GetDeviceSessionsUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<List<DeviceSession>> = repository.sessions()
}

/** Bitta qurilmani hisobdan chiqarish. */
class RevokeDeviceSessionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> = repository.revokeSession(id)
}

/** Barcha qurilmalardan chiqish. */
class LogoutAllDevicesUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<Unit> = repository.logoutAllDevices()
}

/** SMS kodning uzunligi — backend 6 xonali kod yuboradi. */
const val OTP_LENGTH = 6
