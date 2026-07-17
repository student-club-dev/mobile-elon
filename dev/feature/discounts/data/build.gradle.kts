// Data qatlami — `sc.module-data` convention plugin'i serialization, database, network,
// SQLDelight, Ktor, datetime'ni beradi. Bu yerda faqat modulning O'ZIGA XOS bog'liqligi.
plugins {
    id("sc.module-data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dev.feature.discounts.domain)
        }
    }
}
