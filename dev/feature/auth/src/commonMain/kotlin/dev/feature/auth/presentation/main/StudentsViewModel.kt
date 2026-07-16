package dev.feature.auth.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.domain.model.FriendStatus
import dev.core.domain.model.Student
import dev.core.domain.model.StudentSort
import dev.core.domain.repository.StudentRepository
import dev.feature.profile.domain.usecase.ObserveProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Studentlar (1r) ekranining holati. */
data class StudentsUiState(
    val sort: StudentSort = StudentSort.MY_UNIVERSITY,
    val students: List<Student> = emptyList(),
)

class StudentsViewModel(
    observeProfileUseCase: ObserveProfileUseCase,
    private val studentRepository: StudentRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { studentRepository.refresh() }
    }

    private val sort = MutableStateFlow(StudentSort.MY_UNIVERSITY)

    val state: StateFlow<StudentsUiState> = combine(
        studentRepository.observeStudents(),
        observeProfileUseCase(),
        sort,
    ) { students, profile, s ->
        val myUni = profile?.universityId
        val list = when (s) {
            StudentSort.MY_UNIVERSITY -> if (myUni != null) students.filter { it.universityId == myUni } else students
            StudentSort.ALL -> students
        }
        StudentsUiState(sort = s, students = list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentsUiState())

    fun setSort(s: StudentSort) {
        sort.value = s
    }

    /** "+Do'st" ↔ "Kutilmoqda" — do'stlik so'rovi. */
    fun toggleFriend(student: Student) {
        val next = if (student.friendStatus == FriendStatus.NONE) FriendStatus.PENDING else FriendStatus.NONE
        viewModelScope.launch { studentRepository.setFriendStatus(student.id, next) }
    }
}
