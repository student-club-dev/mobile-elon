// Auth — birlashgan feature moduli (UI + oqim). `sc.module-ui` Compose/Koin/Lifecycle/
// uikit/common'ni beradi; serialization plugin qo'shimcha yoqiladi. Qolgan bog'liqliklar
// (navigatsiya, sessiya keshi, backend klienti) shu yerda.
plugins {
    id("sc.module-ui")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dev.core.domain)
            implementation(projects.dev.core.database)
            // Backend auth (`/v1/auth/business/*`) — generatsiya qilingan klient `:dev:core:network`
            // orqali keladi (u `:dev:api-client` ni `api(...)` bilan eksport qiladi).
            implementation(projects.dev.core.network)

            // Ro'yxatdan o'tish oqimi profilni saqlaydi; MainShell Profil/e'lon ekranlarini ochadi.
            api(projects.dev.feature.profile.domain)
            implementation(projects.dev.feature.profile.presentation)
            implementation(projects.dev.feature.discounts.presentation)
            // Biznesmen ekranlari alohida modulda (sof UI) — auth uni ishlatadi.
            implementation(projects.dev.feature.business)

            // Local sessiya keshi (offline + avtomatik kirish)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            // JWT payload'ini o'qish (JwtClaims) uchun
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            // Biometrik login (F1) — Face ID / barmoq izi
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.fragment)
        }
    }
}
