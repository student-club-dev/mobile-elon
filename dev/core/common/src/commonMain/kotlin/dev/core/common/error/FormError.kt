package dev.core.common.error

import dev.core.common.Resource

/**
 * Formaga qaytariladigan xato — **umumiy xabar + maydon xatolari** bir joyda.
 *
 * ViewModel'lar `(String?) -> Unit` o'rniga shuni qaytaradi: backend 422 bilan
 * `{"error": {"fields": {"phoneNumber": "Noto'g'ri format"}}}` yuborganda forma har bir xatoni
 * o'z maydoni ostida ko'rsata oladi, [message] esa maydonga bog'lanmagan xatolar uchun qoladi.
 *
 * Naqsh: `EditProfileScreen` + `ProfileViewModel.saveProfile` — boshqa formalar shundan nusxa oladi.
 */
data class FormError(
    /** Umumiy (maydonga bog'lanmagan) xabar — forma tagida ko'rsatiladi. */
    val message: String,
    /** Maydon nomi → xato matni. Kalit — so'rov tanasidagi maydon nomi. */
    val fields: Map<String, String> = emptyMap(),
) {
    /** Faqat maydon xatolari bo'lsa umumiy xabarni takrorlamaslik uchun. */
    val hasFieldErrors: Boolean get() = fields.isNotEmpty()
}

/** [Resource.Error] dan formaga tayyor xato — typed [AppException] dagi maydonlar bilan. */
fun Resource.Error.toFormError(): FormError =
    FormError(message = message, fields = error?.fieldErrors.orEmpty())
