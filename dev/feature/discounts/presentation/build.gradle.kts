// Presentation qatlami — `sc.module-ui` Compose, uikit, Lifecycle/ViewModel, Koin'ni
// beradi. Bu yerda ekranga xos bog'liqliklar (o'z domeni/data, network, database, Coil).
plugins {
    id("sc.module-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dev.feature.discounts.domain)
            // Koin moduli implementatsiyalarni bog'laydi (repository + remote manbalar).
            implementation(projects.dev.feature.discounts.data)

            implementation(projects.dev.core.domain)
            implementation(projects.dev.core.network)
            // Koin moduli `ListingRepositoryImpl(database, ...)` ni quradi.
            implementation(projects.dev.core.database)

            implementation(libs.kotlinx.datetime)
            // Backend rejimida rasm CDN havolasi bilan keladi (offline rejimda — data: URI).
            implementation(libs.coil.compose)
        }

        androidMain.dependencies {
            // Joylashuv ruxsatini so'rash (rememberLauncherForActivityResult).
            implementation(libs.androidx.activity.compose)
            // ContextCompat.checkSelfPermission
            implementation(libs.androidx.core.ktx)
        }
    }
}
