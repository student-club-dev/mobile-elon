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
            // BusinessShell ichki navigatsiyasi (NavHost).
            implementation(libs.androidx.navigation.compose)
        }
    }
}
