// Data qatlami — umumiy infratuzilma `sc.module-data` dan. Bu yerda o'z domeni va
// Firebase Auth (kesh kaliti — sessiyadagi uid; profilning o'zi backenddan keladi).
plugins {
    id("sc.module-data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dev.feature.profile.domain)

            // Faqat uid kerak (ProfileRepositoryImpl) — Firestore ishlatilmaydi.
            implementation(libs.gitlive.firebase.auth)
        }

        androidMain.dependencies {
            // GitLive'ning Android artefaktlari Firebase SDK versiyalarini BOM'dan oladi.
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
        }
    }
}
