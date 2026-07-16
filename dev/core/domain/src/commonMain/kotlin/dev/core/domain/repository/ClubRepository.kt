package dev.core.domain.repository

import dev.core.common.Resource
import dev.core.domain.model.Club
import kotlinx.coroutines.flow.Flow

interface ClubRepository {
    fun observeClubs(): Flow<List<Club>>
    suspend fun refreshClubs(): Resource<List<Club>>

    /** Klubga qo'shilish / chiqish (local). */
    suspend fun setJoined(id: Long, joined: Boolean)
}
