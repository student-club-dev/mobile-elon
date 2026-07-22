plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

// google-services plagini root'da `apply false` bilan classpath'da turadi. Uni faqat
// google-services.json shu papkada bo'lgandagina ulaymiz — shunda Firebase sozlanmagunча
// ham loyiha build bo'laveradi. `uz.elonuz.app` package Firebase'da ro'yxatdan o'tib,
// yangi google-services.json shu yerga tashlangach — Google/Telefon login ishlaydi.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "uz.elonuz"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        // ElonUz'ning o'z mustaqil package'i. Alohida Firebase loyihasida shu package
        // (uz.elonuz.app) + debug SHA-1 ro'yxatdan o'tib, google-services.json shu papkaga
        // tashlangach — Google/Telefon/Email login ishlaydi.
        applicationId = "uz.elonuz.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures { compose = true }

    buildTypes {
        release {
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

    // Firebase (Google + Phone auth) — google-services.json orqali sozlanadi
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

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
