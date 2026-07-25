import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

/**
 * Maxfiy sozlamalar — `local.properties` dan (u `.gitignore` да, repoga tushmaydi).
 * CI'да fayl bo'lmaydi, shuning uchun muhit o'zgaruvchisiga tushamiz.
 *
 * Namuna kalitlar `local.properties.example` да.
 */
fun secret(key: String): String {
    val file = rootProject.file("local.properties")
    val fromFile = if (file.exists()) {
        Properties().apply { file.inputStream().use(::load) }.getProperty(key)
    } else {
        null
    }
    return fromFile ?: System.getenv(key) ?: ""
}

android {
    namespace = "uz.elonuz"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "uz.elonuz.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        // Google Sign-In — Google Cloud'даgi **Web** (server) OAuth client ID.
        // `GoogleSignIn.android.kt` uni `google_web_client_id` resursi orqali o'qiydi.
        // Bo'sh bo'lsa kirish tugmasi aniq xato beradi (jimgina ishlamay qolmaydi).
        resValue("string", "google_web_client_id", secret("GOOGLE_WEB_CLIENT_ID"))
    }

    buildFeatures { compose = true }

    signingConfigs {
        create("release") {
            // Kalit `.gitignore` да — repoga tushmaydi. Parollar `local.properties` дан
            // (yoki CI'да o'sha nomdagi muhit o'zgaruvchilaridан).
            val store = rootProject.file("elonUzApp/release.keystore")
            if (store.exists() && secret("RELEASE_STORE_PASSWORD").isNotEmpty()) {
                storeFile = store
                storePassword = secret("RELEASE_STORE_PASSWORD")
                keyAlias = secret("RELEASE_KEY_ALIAS")
                keyPassword = secret("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // Kalit yo'q bo'lsa (masalan boshqa dasturchida) release build yiqilmasin —
            // shunchaki imzosiz yig'iladi.
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.dev.shared)
    implementation(projects.dev.core.di)
    implementation(projects.dev.core.uikit)
    // OkHttpInterceptors (Chucker'ni ro'yxatga qo'shish uchun) shu modulда.
    implementation(projects.dev.core.network)

    // Chucker — Debug HTTP inspektori (bildirishnoma + alohida ekran).
    // release'да no-op: hech narsa qilmaydi, kod o'zgarmaydi.
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    // FragmentActivity — biometrik BiometricPrompt shuni talab qiladi
    implementation(libs.androidx.fragment)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(compose.preview)
}
