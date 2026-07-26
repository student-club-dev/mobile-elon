// Biznesmen tomoni — alohida feature moduli (sof UI/presentation).
// Auth'ga bog'lanmaydi (aylanma bo'lmasligi uchun): biznes ekranlari holat/callbacklarni
// oddiy parametr sifatida oladi, auth esa shu modulni ishlatadi.
plugins {
    id("sc.module-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Biznesning e'lon ekranlari (MyListings/PostListing) va biznes profil (ProfileViewModel).
            implementation(projects.dev.feature.discounts.presentation)
            implementation(projects.dev.feature.profile.presentation)
            implementation(projects.dev.feature.profile.domain)
            // Hisobga kirish/parol tiklash use case'lari (auth FEATURE'iga emas, core'ga bog'lanamiz —
            // `auth -> business` yo'nalishi buzilmasin).
            implementation(projects.dev.core.domain)
            implementation(projects.dev.core.common)
            // BusinessShell ichki navigatsiyasi (NavHost).
            implementation(libs.androidx.navigation.compose)
        }
    }
}
