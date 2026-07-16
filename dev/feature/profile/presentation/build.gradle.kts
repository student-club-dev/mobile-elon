// Presentation qatlami — umumiy UI to'plami `sc.module-ui` dan. Bu yerda ekranga xos qism.
plugins {
    id("sc.module-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dev.feature.profile.domain)
            // Koin moduli implementatsiyalarni bog'laydi (repository + remote manbalar).
            implementation(projects.dev.feature.profile.data)

            // "Mening e'lonlarim / Saqlangan / Arizalar" bo'limlari umumiy domen repository'laridan o'qiydi.
            implementation(projects.dev.core.domain)
            implementation(projects.dev.core.network)
            // Koin moduli `ProfileRepositoryImpl(database, ...)` ni quradi.
            implementation(projects.dev.core.database)

            // Avatarni URL'dan ko'rsatish (Coil 3 — multiplatform).
            implementation(libs.coil.compose)
        }

        androidMain.dependencies {
            // Galereyadan rasm tanlash — ActivityResultContracts.PickVisualMedia.
            implementation(libs.androidx.activity.compose)
        }
    }
}
