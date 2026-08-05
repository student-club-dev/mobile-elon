package dev.core.data.remote

import dev.core.common.Resource
import dev.core.data.dto.AdDto
import dev.core.data.dto.ConversationDto
import dev.core.data.dto.JobDto
import dev.core.data.dto.StudentDto
import dev.core.data.dto.UniversityDto
import dev.core.network.response.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Qolgan domenlar uchun masofaviy manbalar (B4 shabloni — Discounts'dan tarqatilgan).
 * Har biri bitta endpoint'dan ro'yxat oladi; Ktor klientiga sessiya tokeni avtomatik qo'shiladi (B3).
 *
 * Xato-ishlash `safeCall` da: u javob tanasidagi backend matnini o'qib typed `AppException`
 * qaytaradi. O'z `try/catch` imiz bo'lsa foydalanuvchi Ktor'ning inglizcha istisno matnini
 * ("Client request ... invalid: 403 Forbidden") ko'rardi.
 */

// --- Ishlar ---
interface JobRemoteDataSource { suspend fun fetchJobs(): Resource<List<JobDto>> }

class KtorJobRemoteDataSource(private val client: HttpClient) : JobRemoteDataSource {
    override suspend fun fetchJobs(): Resource<List<JobDto>> =
        safeCall { client.get("jobs").body() }
}

// --- Studentlar ---
interface StudentRemoteDataSource { suspend fun fetchStudents(): Resource<List<StudentDto>> }

class KtorStudentRemoteDataSource(private val client: HttpClient) : StudentRemoteDataSource {
    override suspend fun fetchStudents(): Resource<List<StudentDto>> =
        safeCall { client.get("students").body() }
}

// --- E'lonlar ---
interface AdRemoteDataSource { suspend fun fetchAds(): Resource<List<AdDto>> }

class KtorAdRemoteDataSource(private val client: HttpClient) : AdRemoteDataSource {
    override suspend fun fetchAds(): Resource<List<AdDto>> =
        safeCall { client.get("ads").body() }
}

// --- Universitetlar ---
interface UniversityRemoteDataSource { suspend fun fetchUniversities(): Resource<List<UniversityDto>> }

class KtorUniversityRemoteDataSource(private val client: HttpClient) : UniversityRemoteDataSource {
    override suspend fun fetchUniversities(): Resource<List<UniversityDto>> =
        safeCall { client.get("universities").body() }
}

// --- Chat (suhbatlar ro'yxati) ---
interface ChatRemoteDataSource { suspend fun fetchConversations(): Resource<List<ConversationDto>> }

class KtorChatRemoteDataSource(private val client: HttpClient) : ChatRemoteDataSource {
    override suspend fun fetchConversations(): Resource<List<ConversationDto>> =
        safeCall { client.get("conversations").body() }
}
