plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

// Convention plugin'lar shu Gradle plugin'larni qo'llashi uchun ular klasspath'da bo'lishi kerak.
// Versiyalar asosiy loyiha katalogidan (`libs`) olinadi — bitta manba.
dependencies {
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.compose:compose-gradle-plugin:${libs.versions.composeMultiplatform.get()}")
    // Serialization Gradle plugin markeri kotlin-gradle-plugin ichida kelmaydi — alohida qo'shamiz,
    // shunda convention plugin `id("org.jetbrains.kotlin.plugin.serialization")` ni topa oladi.
    implementation("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:${libs.versions.kotlin.get()}")
}
