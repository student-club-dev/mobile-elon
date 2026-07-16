package dev.feature.auth.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.core.domain.model.Job
import dev.core.domain.model.JobFilter
import dev.core.domain.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Ishlar (1t) ekranining holati. */
data class JobsUiState(
    val filter: JobFilter = JobFilter.ALL,
    val jobs: List<Job> = emptyList(),
)

class JobsViewModel(
    private val jobRepository: JobRepository,
) : ViewModel() {

    init {
        // Offline-first: backend'dan sinxronlashga urinamiz (no-op yoki Error → cache saqlanadi).
        viewModelScope.launch { jobRepository.refresh() }
    }

    private val filter = MutableStateFlow(JobFilter.ALL)

    val state: StateFlow<JobsUiState> = combine(jobRepository.observeJobs(), filter) { jobs, f ->
        JobsUiState(filter = f, jobs = jobs.filter { it.matches(f) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JobsUiState())

    fun setFilter(f: JobFilter) {
        filter.value = f
    }

    fun toggleBookmark(job: Job) {
        viewModelScope.launch { jobRepository.setBookmarked(job.id, !job.bookmarked) }
    }

    fun apply(job: Job) {
        viewModelScope.launch { jobRepository.apply(job) }
    }
}

private fun Job.matches(filter: JobFilter): Boolean = when (filter) {
    JobFilter.ALL -> true
    JobFilter.IT -> category.equals("IT", ignoreCase = true) || tags.any { it.equals("IT", ignoreCase = true) }
    JobFilter.REMOTE -> remote
    JobFilter.PART_TIME -> partTime
}
