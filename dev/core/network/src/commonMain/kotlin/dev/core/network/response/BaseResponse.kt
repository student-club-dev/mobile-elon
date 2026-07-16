package dev.core.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Backend'ning **standart javob konverti** — har bir API so'rovi shu bitta shaklда qaytadi
 * (IYM-business / Ipak Yo'li naqshi). Klient hech qachon "yalang'och" JSON kutmaydi:
 * status/xato bir joyда — [ResponseChecker] — tekshiriladi.
 *
 * Backend'lar turlicha nomlaydi, shuning uchun konvert moslashuvchan:
 * - muvaffaqiyat: [success] `true` **yoki** [status] 2xx,
 * - foydali yuk: [result] **yoki** [data] (qaysi biri kelsa — [payload]),
 * - xato: [error] yoki [message].
 */
@Serializable
data class BaseResponse<T>(
    val success: Boolean = false,
    val status: Int? = null,
    val code: String? = null,
    val message: String? = null,
    val result: T? = null,
    @SerialName("data") val data: T? = null,
    val error: ApiError? = null,
) {
    /** `result` yoki `data` — backend qaysi maydonда yuborsa. */
    val payload: T? get() = result ?: data

    /**
     * Muvaffaqiyatli javobmi. `status` bor bo'lsa — 2xx bo'yicha; yo'q bo'lsa — [success]
     * bayrog'i bo'yicha (shunda `{"success": false, "message": ...}` xato deb hisoblanadi).
     */
    val isSuccessful: Boolean
        get() = error == null && (if (status != null) status in 200..299 else success)
}

/** Standart xato tanasi — kod, matn va (validatsiya) maydon-xatolari. */
@Serializable
data class ApiError(
    val code: String? = null,
    val message: String? = null,
    /** Maydonga bog'langan validatsiya xatolari: {"phone": "Noto'g'ri format"}. */
    val fields: Map<String, String> = emptyMap(),
)
