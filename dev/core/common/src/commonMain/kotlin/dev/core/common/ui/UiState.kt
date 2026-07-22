package dev.core.common.ui

import dev.core.common.error.AppException

/**
 * Ekran (list/detail) holati — UI shu to'rt holatдан birini ko'rsatadi:
 * spinner, kontent, bo'sh holat yoki xato (retry bilan).
 *
 * ViewModel `StateFlow<UiState<T>>` chiqaradi, Compose esa `when` bilan mos komponentни
 * chizadi ([dev.core.uikit] dagi umumiy StateView orqali).
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>

    data class Content<T>(val data: T) : UiState<T>

    /** Muvaffaqiyatli, lekin ma'lumot yo'q (bo'sh ro'yxat). */
    data object Empty : UiState<Nothing>

    /**
     * Xato holati. [error] — typed sabab (UI retry/login qarorini shunga qarab qiladi),
     * [message] — ko'rsatiladigan matn, [retryable] — "Qayta urinish" tugmasi chiqsinmi.
     */
    data class Error(
        val error: AppException,
        val message: String = error.userMessage,
        val retryable: Boolean = error !is AppException.Unauthorized,
    ) : UiState<Nothing>
}

/** Ro'yxatni holatga aylantiradi: bo'sh → [UiState.Empty], aks holda [UiState.Content]. */
fun <T> List<T>.toUiState(): UiState<List<T>> =
    if (isEmpty()) UiState.Empty else UiState.Content(this)
