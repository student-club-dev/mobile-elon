import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Feature PRESENTATION qatlami — kmp-library + Compose Multiplatform + umumiy UI to'plami
// (designsystem, Compose, Lifecycle/ViewModel, Koin). Ekran/data bog'liqliklari modulda qoladi.
//
// Compose kutubxonalari katalog orqali ulanadi (`compose.*` DSL emas), chunki Compose plugin'i
// convention plugin orqali qo'llanganda `compose.*` DSL modul skriptida mavjud bo'lmaydi.

plugins {
    id("sc.kmp-library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
fun VersionCatalog.lib(alias: String) = findLibrary(alias).get()

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets.getByName("commonMain").dependencies {
        implementation(project(":dev:core:designsystem"))

        implementation(libs.lib("compose-runtime"))
        implementation(libs.lib("compose-foundation"))
        implementation(libs.lib("compose-material3"))
        implementation(libs.lib("compose-ui"))

        implementation(libs.lib("androidx-lifecycle-viewmodel"))
        implementation(libs.lib("androidx-lifecycle-runtimeCompose"))

        implementation(libs.lib("koin-core"))
        implementation(libs.lib("koin-compose"))
        implementation(libs.lib("koin-compose-viewmodel"))
    }
}
