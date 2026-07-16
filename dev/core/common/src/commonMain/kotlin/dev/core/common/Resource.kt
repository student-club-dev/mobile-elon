package dev.core.common

import dev.core.common.error.AppException

/** Tarmoq/ma'lumotlar qatlami uchun universal natija turi. */
sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>

    /**
     * Xato. [message] — foydalanuvchiga ko'rsatiladigan matn (moslik uchun qoladi).
     * [error] — **typed** xato ([AppException]); UI shunga qarab retry/login ko'rsatadi.
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val error: AppException? = null,
    ) : Resource<Nothing>

    data object Loading : Resource<Nothing>
}

/** [AppException] dan to'g'ridan-to'g'ri typed [Resource.Error] quradi. */
fun errorOf(e: AppException): Resource.Error =
    Resource.Error(message = e.userMessage, cause = e.cause, error = e)

inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error -> this
    Resource.Loading -> Resource.Loading
}
