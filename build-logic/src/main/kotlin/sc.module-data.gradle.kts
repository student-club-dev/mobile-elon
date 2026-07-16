import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Feature DATA qatlami — kmp-library + serialization + umumiy data infratuzilmasi
// (database, network, SQLDelight, Ktor). Modulning O'ZIGA XOS bog'liqliklari (masalan
// o'z domain moduli yoki Firebase) modul build.gradle.kts sida qoladi.

plugins {
    id("sc.kmp-library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
fun VersionCatalog.lib(alias: String) = findLibrary(alias).get()

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets.getByName("commonMain").dependencies {
        implementation(project(":dev:core:database"))
        implementation(project(":dev:core:network"))

        implementation(libs.lib("sqldelight-runtime"))
        implementation(libs.lib("sqldelight-coroutines"))
        implementation(libs.lib("ktor-client-core"))
        implementation(libs.lib("kotlinx-serialization-json"))
        implementation(libs.lib("kotlinx-datetime"))
    }
}
