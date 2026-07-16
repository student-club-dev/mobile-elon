import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import dev.buildlogic.NameUtils

// Baza KMP + Android library sozlamasi — barcha modullar uchun UMUMIY qism (target'lar,
// android bloki, namespace, coroutines, test). Loyiha bog'liqliklari YO'Q, shuning uchun
// core modullar ham bemalol ishlatishi mumkin (sikl hosil bo'lmaydi).

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
fun VersionCatalog.lib(alias: String) = findLibrary(alias).get()

extensions.configure<KotlinMultiplatformExtension> {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = NameUtils.frameworkName(project.path) }
    }

    sourceSets.getByName("commonMain").dependencies {
        implementation(libs.lib("kotlinx-coroutines-core"))
    }
    sourceSets.getByName("commonTest").dependencies {
        implementation("org.jetbrains.kotlin:kotlin-test")
    }
}

extensions.configure<LibraryExtension> {
    namespace = NameUtils.namespace(project.path)
    compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()
    defaultConfig {
        minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
