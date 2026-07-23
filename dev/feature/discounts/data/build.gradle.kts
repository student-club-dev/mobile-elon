// Data qatlami — `sc.module-data` convention plugin'i serialization, database, network,
// SQLDelight, Ktor, datetime'ni beradi. Bu yerda faqat modulning O'ZIGA XOS bog'liqligi.
plugins {
    id("sc.module-data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dev.feature.discounts.domain)

            // SessionProvider (joriy uid) — local biznes egaligini belgilaydi.
            implementation(projects.dev.core.domain)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
        // Repository testlari real SQLite'da ishlaydi (host JVM) — offline-first oqim
        // "saqladim → ro'yxatda ko'rindi" faqat haqiqiy bazada isbotlanadi.
        androidUnitTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
