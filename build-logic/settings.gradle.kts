// `build-logic` — alohida (included) build. Bu yerda convention plugin'lar yashaydi:
// yangi modul qo'shganda uning build.gradle.kts fayliga faqat BITTA qator yoziladi
// (masalan `id("sc.module-ui")`), butun target/android/deps sozlamalari shu yerdan keladi.
// (iym-native-business dagi build-logic joylashuvining moslashtirilgan varianti.)

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    // Asosiy loyiha bilan bir xil versiya katalogidan foydalanamiz.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
