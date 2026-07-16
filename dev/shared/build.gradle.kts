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
        it.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(projects.dev.core.di)
            // Swift tomonidagi social auth bridge (IosSocialAuthBridge/Delegate) ko'rinsin
            export(projects.dev.feature.auth)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.dev.core.di)
            implementation(projects.dev.core.designsystem)
            implementation(projects.dev.core.data)
            api(projects.dev.feature.auth)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Rasm yuklash (avatar) — ilova darajasidagi ImageLoader sozlamasi App.kt'da.
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.ktor.client.core)
        }
    }
}

android {
    namespace = "dev.shared"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
