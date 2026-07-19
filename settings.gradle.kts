rootProject.name = "ElonUz"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    // Convention plugin'lar (yangi modul = bitta qator) shu included build'da yashaydi.
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Yagona ilova — ElonUz (biznes egalari). Launcher to'g'ridan-to'g'ri biznes login +
// BusinessShell'ni ochadi. Umumiy KMP kod (dev/*) kutubxona sifatida ishlatiladi.
include(":elonUzApp")

// API-generatsiya — `iym-native-business` joylashuvining moslashtirilgan varianti.
// `:dev:api-client-generator` spec'ni ushlaydi va generatsiya tasklarini beradi
// (cleanSwagger / openApiGenerate / generateAllApi); `:dev:api-client` — generatsiya
// qilingan klient (paket `dev.core.network.generated`).
include(":dev:api-client-generator")
include(":dev:api-client")

// Core modules
include(":dev:core:common")
include(":dev:core:designsystem")
include(":dev:core:network")
include(":dev:core:database")
include(":dev:core:domain")
include(":dev:core:data")
include(":dev:core:di")

// Feature modules
include(":dev:feature:auth")

// Biznesmen tomoni — alohida modul (sof UI). Auth shuni ishlatadi.
include(":dev:feature:business")

// Profil feature'i — qatlamlarga ajratilgan (domain / data / presentation).
// Keyingi feature'lar (Jobs, Chat, Discounts...) aynan shu shakldan nusxa oladi.
include(":dev:feature:profile:domain")
include(":dev:feature:profile:data")
include(":dev:feature:profile:presentation")

// Chegirmalar feature'i — biznes egasi e'lon qo'yadi, talaba ko'radi.
// Spetsifikatsiya: DISCOUNTS_BUSINESS_API.md + dev/api-client-generator/elon-uz.json.
include(":dev:feature:discounts:domain")
include(":dev:feature:discounts:data")
include(":dev:feature:discounts:presentation")

// iOS framework + Compose App() aggregator
include(":dev:shared")
