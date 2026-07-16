import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Feature DOMAIN qatlami uchun — baza + umumiy `:dev:core:common` (Resource, natija turlari).
// Domen qatlami hech qanday framework'ga bog'lanmaydi.

plugins {
    id("sc.kmp-base-library")
}

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets.getByName("commonMain").dependencies {
        api(project(":dev:core:common"))
    }
}
