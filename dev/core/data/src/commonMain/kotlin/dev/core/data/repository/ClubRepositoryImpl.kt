package dev.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.core.common.AppDispatchers
import dev.core.common.Resource
import dev.core.data.dto.ClubDto
import dev.core.data.mapper.toDomain
import dev.core.database.sql.StudentClubsDatabase
import dev.core.domain.model.Club
import dev.core.domain.repository.ClubRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ClubRepositoryImpl(
    private val client: HttpClient,
    private val database: StudentClubsDatabase,
    private val dispatchers: AppDispatchers,
) : ClubRepository {

    override fun observeClubs(): Flow<List<Club>> =
        database.clubQueries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun refreshClubs(): Resource<List<Club>> = try {
        val dtos: List<ClubDto> = client.get("clubs").body()
        val queries = database.clubQueries
        queries.transaction {
            queries.deleteAll()
            dtos.forEach {
                queries.upsert(it.id, it.name, it.description, it.membersCount.toLong(), it.imageUrl)
            }
        }
        Resource.Success(dtos.map { it.toDomain() })
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Klublarni yuklab bo'lmadi", e)
    }

    override suspend fun setJoined(id: Long, joined: Boolean) = withContext(dispatchers.io) {
        database.clubQueries.setJoined(if (joined) 1L else 0L, id)
    }
}
