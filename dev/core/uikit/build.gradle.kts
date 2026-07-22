plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "uikit" }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.dev.core.common)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)
            api(compose.components.resources)
            implementation(compose.materialIconsExtended)
        }

        androidMain.dependencies {
            // Galereyadan rasm tanlash (`media/ImagePicker`) — ActivityResultContracts.PickVisualMedia.
            // Xarita uchun joylashuv ruxsatini so'rash (`map/UserLocation`) ham shu API'da.
            implementation(libs.androidx.activity.compose)
            // ContextCompat.checkSelfPermission — `map/UserLocation`.
            implementation(libs.androidx.core.ktx)
        }
    }
}

// Matnlar shu modulda yashaydi va HAMMA feature moduli ulardan foydalanadi, shuning uchun
// generatsiya qilinadigan `Res` klassi ochiq (public) bo'lishi shart — aks holda u faqat
// uikit ichida ko'rinadi.
compose.resources {
    publicResClass = true
    packageOfResClass = "dev.core.uikit.resources"
    generateResClass = always
}

android {
    namespace = "dev.core.uikit"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
