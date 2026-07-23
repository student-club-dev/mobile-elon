// Data qatlami — umumiy infratuzilma `sc.module-data` dan (network + database + Ktor).
// Bu yerda faqat o'z domeni: profil `GET/PUT /v1/profile/me` orqali keladi, kesh kaliti esa
// `SessionProvider` bergan uid (JWT `sub`).
plugins {
    id("sc.module-data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dev.feature.profile.domain)

            // SessionProvider (joriy uid) — implementatsiya auth feature'da.
            implementation(projects.dev.core.domain)
        }
    }
}
