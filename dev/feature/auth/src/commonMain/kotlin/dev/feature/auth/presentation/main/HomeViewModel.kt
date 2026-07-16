package dev.feature.auth.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.domain.model.Club
import dev.core.domain.model.DiscountCategory
import dev.core.domain.model.DiscountOffer
import dev.core.domain.model.FriendStatus
import dev.core.domain.model.Job
import dev.core.domain.model.Student
import dev.core.domain.repository.ClubRepository
import dev.core.domain.repository.DiscountRepository
import dev.core.domain.repository.JobRepository
import dev.core.domain.repository.NotificationRepository
import dev.core.domain.repository.StudentRepository
import dev.core.domain.repository.UniversityRepository
import dev.core.domain.usecase.ObserveCurrentUserUseCase
import dev.feature.profile.domain.usecase.ObserveProfileUseCase
import dev.feature.profile.domain.usecase.RefreshProfileUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Home (1p) ekranining holati — barchasi local DB'dan reaktiv. */
data class HomeUiState(
    val userName: String = "Talaba",
    val universityMonogram: String? = null,
    val courseLabel: String? = null,
    val categories: List<DiscountCategory> = emptyList(),
    val featured: DiscountOffer? = null,
    val jobs: List<Job> = emptyList(),
    val students: List<Student> = emptyList(),
    val clubs: List<Club> = emptyList(),
    val hasUnreadNotifications: Boolean = false,
)

class HomeViewModel(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    observeProfileUseCase: ObserveProfileUseCase,
    private val refreshProfileUseCase: RefreshProfileUseCase,
    universityRepository: UniversityRepository,
    private val discountRepository: DiscountRepository,
    private val jobRepository: JobRepository,
    private val studentRepository: StudentRepository,
    clubRepository: ClubRepository,
    notificationRepository: NotificationRepository,
) : ViewModel() {

    init {
        // Offline-first: universitetlarni backend'dan sinxronlashga urinamiz.
        viewModelScope.launch { universityRepository.refresh() }
        // Kirishdan keyin ilova shu ekrandan boshlanadi — profilni masofaviy manbadan
        // keshga tortamiz, shunda sarlavhadagi universitet/kurs darrov ko'rinadi.
        viewModelScope.launch { refreshProfileUseCase() }
    }

    private val header = combine(
        observeCurrentUserUseCase(),
        observeProfileUseCase(),
        universityRepository.observeUniversities(),
    ) { user, profile, universities ->
        val uni = universities.firstOrNull { it.id == profile?.universityId }
        Header(
            // Ism profildan olinadi; profil hali to'ldirilmagan bo'lsa — sessiya nomidan.
            name = profile?.displayName
                ?: user?.fullName?.takeIf { it.isNotBlank() }
                ?: "Talaba",
            monogram = uni?.monogram,
            course = profile?.courseYear?.let(::courseLabel),
        )
    }

    private val content = combine(
        discountRepository.observeCategories(),
        discountRepository.observeFeatured(),
        jobRepository.observeJobs(),
        studentRepository.observeStudents(),
        clubRepository.observeClubs(),
    ) { categories, featured, jobs, students, clubs ->
        Content(categories, featured.firstOrNull(), jobs, students, clubs)
    }

    val state: StateFlow<HomeUiState> = combine(
        header, content, notificationRepository.observeUnreadCount(),
    ) { h, c, unread ->
        HomeUiState(
            userName = h.name,
            universityMonogram = h.monogram,
            courseLabel = h.course,
            categories = c.categories,
            featured = c.featured,
            jobs = c.jobs,
            students = c.students,
            clubs = c.clubs,
            hasUnreadNotifications = unread > 0,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    /** Student kartasidagi "+Do'st" ↔ "Kutilmoqda" o'zgartirish. */
    fun toggleFriend(student: Student) {
        val next = if (student.friendStatus == FriendStatus.NONE) FriendStatus.PENDING else FriendStatus.NONE
        viewModelScope.launch { studentRepository.setFriendStatus(student.id, next) }
    }

    /** Ish kartasidagi bookmark. */
    fun toggleBookmark(job: Job) {
        viewModelScope.launch { jobRepository.setBookmarked(job.id, !job.bookmarked) }
    }

    private data class Header(val name: String, val monogram: String?, val course: String?)
    private data class Content(
        val categories: List<DiscountCategory>,
        val featured: DiscountOffer?,
        val jobs: List<Job>,
        val students: List<Student>,
        val clubs: List<Club>,
    )
}

// Profil "1".."4" yozadi (EditProfileScreen); eski yozuvlarda "ONE".."FOUR" uchraydi.
private fun courseLabel(courseYear: String): String = when (courseYear) {
    "1", "ONE" -> "1-kurs"
    "2", "TWO" -> "2-kurs"
    "3", "THREE" -> "3-kurs"
    "4", "FOUR" -> "4-kurs"
    "MASTER" -> "Magistr"
    else -> courseYear
}
