// Generatsiya qilingan API klienti moduli — `iym-native-business` loyihasidagi `:dev:api-client`
// joylashuvining moslashtirilgan varianti.
//
// Bu modulda QO'LDA kod yozilmaydi: uning `src/` papkasini `:dev:api-client-generator` moduli
// `openApiGenerate` taski to'ldiradi (paket `dev.core.network.generated`). Generatsiya qilingan
// kod git'da saqlanmaydi (.gitignore), build vaqtida qayta yaratiladi.

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

// Kompilyatsiyadan oldin generatsiya ishga tushishi shart.
evaluationDependsOn(":dev:api-client-generator")
val generateApi = project(":dev:api-client-generator").tasks.named("openApiGenerate")

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "apiClient" }
    }

    sourceSets {
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

// Generatsiya chiqishini o'qiydigan HAR bir task undan oldin ishlashi kerak — nafaqat Kotlin
// kompilyatsiyasi, balki Android resurs/AarMetadata/prebuild tasklari ham (iym'dagi
// `tasks.configureEach { ... dependsOn(genV2) }` uslubi).
tasks.configureEach {
    if (name.contains("compile", ignoreCase = true) ||
        name.contains("ProcessResources", ignoreCase = true) ||
        name.contains("generateResources", ignoreCase = true) ||
        name.contains("Resources", ignoreCase = true) ||
        name.contains("AarMetadata", ignoreCase = true) ||
        name.contains("Sources", ignoreCase = true) ||
        name.contains("prebuild", ignoreCase = true) ||
        name.contains("lint", ignoreCase = true)
    ) {
        dependsOn(generateApi)
    }
}
