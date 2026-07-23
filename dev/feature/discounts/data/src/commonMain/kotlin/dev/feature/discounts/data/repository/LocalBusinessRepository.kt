package dev.feature.discounts.data.repository

import dev.core.common.Resource
import dev.core.domain.repository.SessionProvider
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.repository.BusinessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock

/**
 * Local biznes repository (`USE_LOCAL_DATA`) — backend'siz, **xotirada** (in-memory). Ilova ochiq
 * turgan davомда saqlanadi (qayta ishga tushirilганда seed qaytadi). Egalik — joriy dev sessiya uid.
 *
 * Eslатma: hozircha in-memory (SQLDelight jadvali qo'shilса — bardavom bo'ladi).
 */
class LocalBusinessRepository(
    private val session: SessionProvider,
) : BusinessRepository {

    private var counter = 1
    private val store = MutableStateFlow(seed())

    override fun observeMine(): Flow<List<Business>> =
        store.map { list -> list.filter { it.ownerId == ownerId() }.sortedByDescending { it.createdAt } }

    override suspend fun byId(id: String): Business? = store.value.firstOrNull { it.id == id }

    override suspend fun save(business: Business): Resource<Business> {
        val now = Clock.System.now().toEpochMilliseconds()
        val existing = store.value.firstOrNull { it.id == business.id }
        val id = business.id.ifBlank { "biz-local-${counter++}" }
        val saved = business.copy(
            id = id,
            ownerId = ownerId(),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        store.update { list -> list.filterNot { it.id == id } + saved }
        return Resource.Success(saved)
    }

    override suspend fun delete(id: String): Resource<Unit> {
        store.update { list -> list.filterNot { it.id == id } }
        return Resource.Success(Unit)
    }

    private fun ownerId(): String = session.currentUid() ?: "dev-user-1"

    private fun seed(): List<Business> = listOf(
        Business(
            id = "biz-seed-1",
            ownerId = "dev-user-1",
            name = "Namuna Kafe",
            phone = "+998901112233",
            businessType = BusinessType("NATIONAL_FOOD"),
        ),
    )
}
