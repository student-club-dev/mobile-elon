// Generatsiya qilingan API klienti moduli — `iym-native-business` loyihasidagi `:dev:api-client`
// joylashuvining moslashtirilgan varianti.
//
// Bu modulda QO'LDA kod yozilmaydi: generatsiya qilingan Kotlin klientini `:dev:api-client-generator`
// moduli `openApiGenerate` taski o'zining `build/generated-client/` papkasiga chiqaradi, bu modul esa
// o'sha kodni `commonMain` srcDir sifatida ulaydi (paket `dev.core.network.generated`). Generatsiya
// qilingan kod git'da saqlanmaydi, build vaqtida qayta yaratiladi.

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

// Kompilyatsiyadan oldin generatsiya ishga tushishi shart.
evaluationDependsOn(":dev:api-client-generator")
val generateApi = project(":dev:api-client-generator").tasks.named("openApiGenerate")

// Generator chiqishi — SHU srcDir'ni `builtBy(generateApi)` bilan ulash Gradle'ga aniq task
// bog'liqligini beradi: commonMain manbalarini o'qiydigan HAR bir task (kompilyatsiya, metadata
// transform, lint...) avtomatik ravishda generatsiyadan keyin ishlaydi. Shu bois pastda nom bo'yicha
// moslashtiradigan mo'rt `configureEach` blok endi kerak emas.
val generatedClientSrc = project(":dev:api-client-generator")
    .layout.buildDirectory.dir("generated-client/src/commonMain/kotlin")

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "apiClient" }
    }

    sourceSets {
        commonMain.configure {
            kotlin.srcDir(files(generatedClientSrc).builtBy(generateApi))
        }
        commonMain.dependencies {
            // Generatsiya qilingan klient faqat shu kutubxonalarga tayanadi (HTTP dvigatel'i
            // — okhttp/darwin — `:dev:core:network` da beriladi).
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "dev.core.network.generated"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
