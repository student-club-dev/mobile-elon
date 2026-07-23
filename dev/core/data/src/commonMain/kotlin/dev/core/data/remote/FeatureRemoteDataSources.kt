package dev.core.data.remote

import dev.core.common.Resource
import dev.core.data.dto.AdDto
import dev.core.data.dto.ConversationDto
import dev.core.data.dto.JobDto
import dev.core.data.dto.StudentDto
import dev.core.data.dto.UniversityDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Qolgan domenlar uchun masofaviy manbalar (B4 shabloni — Discounts'dan tarqatilgan).
 * Har biri bitta endpoint'dan ro'yxat oladi; Ktor klientiga sessiya tokeni avtomatik qo'shiladi (B3).
 */

// --- Ishlar ---
interface JobRemoteDataSource { suspend fun fetchJobs(): Resource<List<JobDto>> }

class KtorJobRemoteDataSource(private val client: HttpClient) : JobRemoteDataSource {
    override suspend fun fetchJobs(): Resource<List<JobDto>> = try {
        Resource.Success(client.get("jobs").body())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Ishlarni yuklab bo'lmadi", e)
    }
}

// --- Studentlar ---
interface StudentRemoteDataSource { suspend fun fetchStudents(): Resource<List<StudentDto>> }

class KtorStudentRemoteDataSource(private val client: HttpClient) : StudentRemoteDataSource {
    override suspend fun fetchStudents(): Resource<List<StudentDto>> = try {
        Resource.Success(client.get("students").body())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Studentlarni yuklab bo'lmadi", e)
    }
}

// --- E'lonlar ---
interface AdRemoteDataSource { suspend fun fetchAds(): Resource<List<AdDto>> }

class KtorAdRemoteDataSource(private val client: HttpClient) : AdRemoteDataSource {
    override suspend fun fetchAds(): Resource<List<AdDto>> = try {
        Resource.Success(client.get("ads").body())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "E'lonlarni yuklab bo'lmadi", e)
    }
}

// --- Universitetlar ---
interface UniversityRemoteDataSource { suspend fun fetchUniversities(): Resource<List<UniversityDto>> }

class KtorUniversityRemoteDataSource(private val client: HttpClient) : UniversityRemoteDataSource {
    override suspend fun fetchUniversities(): Resource<List<UniversityDto>> = try {
        Resource.Success(client.get("universities").body())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Universitetlarni yuklab bo'lmadi", e)
    }
}

// --- Chat (suhbatlar ro'yxati) ---
interface ChatRemoteDataSource { suspend fun fetchConversations(): Resource<List<ConversationDto>> }

class KtorChatRemoteDataSource(private val client: HttpClient) : ChatRemoteDataSource {
    override suspend fun fetchConversations(): Resource<List<ConversationDto>> = try {
        Resource.Success(client.get("conversations").body())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Suhbatlarni yuklab bo'lmadi", e)
    }
}
