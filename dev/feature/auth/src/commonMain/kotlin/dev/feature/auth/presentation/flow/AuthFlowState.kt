package dev.feature.auth.presentation.flow

/** Ro'yxatdan o'tuvchi roli. */
enum class Role { STUDENT, BUSINESS, EMPLOYER, UNIVERSITY }

/** Kurs bosqichi. */
enum class CourseYear(val label: String) {
    ONE("1"), TWO("2"), THREE("3"), FOUR("4"), MASTER("Mag")
}

/** Universitet ma'lumoti (placeholder — keyin API/JSON bilan almashtiriladi). */
data class University(
    val id: String,
    val name: String,
    val city: String,
    val monogram: String,
    val accent: Long,
)

/** Statik namunaviy universitetlar ro'yxati. */
val sampleUniversities: List<University> = listOf(
    University("tatu", "TATU — al-Xorazmiy nomidagi", "Toshkent shahri", "TA", 0xFF6C47FF),
    University("nuu", "O‘zbekiston Milliy Universiteti", "Toshkent shahri", "NU", 0xFF2563EB),
    University("tdiu", "Toshkent Davlat Iqtisodiyot U.", "Toshkent shahri", "TD", 0xFF059669),
    University("tpi", "Toshkent Politexnika Instituti", "Toshkent shahri", "TP", 0xFFD97706),
    University("tta", "Toshkent Tibbiyot Akademiyasi", "Toshkent shahri", "TT", 0xFFBE185D),
    University("samdu", "Samarqand Davlat Universiteti", "Samarqand", "SD", 0xFF6C47FF),
    University("buxdu", "Buxoro Davlat Universiteti", "Buxoro", "BD", 0xFF2563EB),
    University("qarshi", "Qarshi Davlat Universiteti", "Qarshi", "QD", 0xFF059669),
    University("namdu", "Namangan Davlat Universiteti", "Namangan", "ND", 0xFFD97706),
    University("ferpi", "Farg‘ona Politexnika Instituti", "Farg‘ona", "FP", 0xFFBE185D),
    University("inha", "Inha University in Tashkent", "Toshkent shahri", "IU", 0xFF6C47FF),
    University("wiut", "Westminster University", "Toshkent shahri", "WU", 0xFF2563EB),
    University("turin", "Turin Politexnika Universiteti", "Toshkent shahri", "TU", 0xFF059669),
    University("tatuff", "TATU Farg‘ona filiali", "Farg‘ona", "TF", 0xFFD97706),
    University("adu", "Andijon Davlat Universiteti", "Andijon", "AD", 0xFFBE185D),
)

/** Butun auth oqimining forma holati. */
data class AuthFlowState(
    // Aloqa
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val rememberMe: Boolean = true,
    // OTP (telefon) va email kod
    val otp: String = "",
    val emailCode: String = "",
    val resendSeconds: Int = 60,
    // Ro'yxat
    val firstName: String = "",
    val lastName: String = "",
    /** Jins — "MALE" | "FEMALE" | null (tanlanmagan). Biznes turlarini moslaydi. */
    val gender: String? = null,
    val role: Role = Role.STUDENT,
    val universityEmail: String = "",
    val termsAccepted: Boolean = false,
    // Profil (talaba)
    val universityId: String? = null,
    val birthYear: Int = 2004,
    val courseYear: CourseYear = CourseYear.TWO,
    val universityQuery: String = "",
    // Profil (biznes egasi) — rol BUSINESS bo'lganda
    val businessName: String = "",
    val businessType: String = "",
    // Umumiy
    val isLoading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
) {
    val phoneDigits: String get() = phone.filter { it.isDigit() }.take(9)
    val phoneValid: Boolean get() = phoneDigits.length == 9
    val otpValid: Boolean get() = otp.length == 6
    val emailCodeValid: Boolean get() = emailCode.length == 6
    val selectedUniversity: University? get() = sampleUniversities.firstOrNull { it.id == universityId }

    val filteredUniversities: List<University>
        get() = if (universityQuery.isBlank()) sampleUniversities
        else sampleUniversities.filter {
            it.name.contains(universityQuery, ignoreCase = true) ||
                it.city.contains(universityQuery, ignoreCase = true)
        }
}
