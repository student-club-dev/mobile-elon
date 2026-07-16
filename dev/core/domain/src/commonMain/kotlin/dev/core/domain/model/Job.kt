package dev.core.domain.model

/** Ishlar ro'yxati filtri. */
enum class JobFilter { ALL, IT, REMOTE, PART_TIME }

/** Ish e'loni. */
data class Job(
    val id: String,
    val title: String,           // "SMM menejer (part-time)"
    val company: String,         // "Uzum Market"
    val companyMonogram: String, // "U"
    val location: String,        // "masofaviy", "Toshkent", "Chilonzor"
    val category: String,        // "IT", "Xizmat"
    val tags: List<String>,      // ["IT", "SMM"]
    val salary: String,          // "3–5 mln so'm"
    val remote: Boolean = false,
    val partTime: Boolean = false,
    val postedAgo: String,       // "2 soat oldin"
    val field: String,           // foydalanuvchi bo'limiga moslash uchun
    val bookmarked: Boolean = false,
)

/** Ish arizasi holati (profil → "Ish arizalarim"). */
enum class ApplicationStatus { SENT, VIEWED, INTERVIEW, REJECTED }

/** Foydalanuvchining ishga arizasi. */
data class JobApplication(
    val id: String,
    val jobId: String,
    val jobTitle: String,
    val company: String,
    val status: ApplicationStatus,
    val appliedAgo: String,
)
