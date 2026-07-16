package dev.core.common.usecase

import dev.core.common.Resource
import dev.core.common.errorOf
import dev.core.common.error.AppException
import dev.core.common.error.toAppException
import dev.core.common.network.NetworkConnectivity
import kotlinx.coroutines.CancellationException

/**
 * Bir martalik (suspend) amal uchun bazaviy use-case.
 *
 * Har bir use-case'da takrorlanadigan **try/catch + internet tekshiruvi + typed xatoga
 * aylantirish** shu yerda markazlashtirilgan. Voris klass faqat [execute] ni yozadi —
 * qolgan hammasini baza bajaradi:
 *
 * ```
 * class SubmitListing(repo: ListingRepository, net: NetworkConnectivity)
 *     : BaseUseCase<Listing, Listing>(net, requiresNetwork = true) {
 *     override suspend fun execute(params: Listing) = repo.submit(params).getOrThrow()
 * }
 * ```
 *
 * [requiresNetwork] `true` bo'lsa — chaqiruvdan oldin internet tekshiriladi; yo'q bo'lsa
 * so'rov umuman qilinmaydi, darrov [AppException.NoInternet] qaytadi.
 */
abstract class BaseUseCase<in P, out R>(
    private val connectivity: NetworkConnectivity? = null,
    private val requiresNetwork: Boolean = false,
) {
    suspend operator fun invoke(params: P): Resource<R> {
        if (requiresNetwork && connectivity?.isOnline() == false) {
            return errorOf(AppException.NoInternet())
        }
        return try {
            Resource.Success(execute(params))
        } catch (e: CancellationException) {
            throw e // korutinani bekor qilish — xato emas, uzatiladi
        } catch (e: Throwable) {
            val online = connectivity?.isOnline() ?: true
            errorOf(e.toAppException(online))
        }
    }

    protected abstract suspend fun execute(params: P): R
}

/** Parametrsiz variant. */
abstract class BaseNoParamUseCase<out R>(
    connectivity: NetworkConnectivity? = null,
    requiresNetwork: Boolean = false,
) : BaseUseCase<Unit, R>(connectivity, requiresNetwork) {
    suspend operator fun invoke(): Resource<R> = invoke(Unit)
    final override suspend fun execute(params: Unit): R = execute()
    protected abstract suspend fun execute(): R
}
