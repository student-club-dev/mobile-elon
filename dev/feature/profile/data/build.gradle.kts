// Data qatlami — umumiy infratuzilma `sc.module-data` dan. Bu yerda o'z domeni va
// Firebase (backendsiz rejim: profil Firestore `users/{uid}` da).
plugins {
    id("sc.module-data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dev.feature.profile.domain)

            // Backendsiz rejim: profil Firestore hujjatida.
            implementation(libs.gitlive.firebase.auth)
            implementation(libs.gitlive.firebase.firestore)
        }

        androidMain.dependencies {
            // GitLive'ning Android artefaktlari Firebase SDK versiyalarini BOM'dan oladi.
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
        }
    }
}
