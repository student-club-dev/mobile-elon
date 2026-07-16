// Data qatlami — `sc.module-data` convention plugin'i serialization, database, network,
// SQLDelight, Ktor, datetime'ni beradi. Bu yerda faqat modulning O'ZIGA XOS bog'liqligi.
plugins {
    id("sc.module-data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dev.feature.discounts.domain)

            // Firebase rejim: e'lonlar Firestore `listings` kolleksiyasida (real-time + offline).
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
