// API-generatsiya moduli — `iym-native-business` loyihasidagi `:dev:api-client-generator`
// joylashuvining moslashtirilgan (bitta spec'li) varianti.
//
// Bu modul kod chiqarmaydi: u faqat spec'ni (`student-clubs.json`) ushlaydi va generatsiya
// tasklarini beradi. Generatsiya qilingan Kotlin klienti qo'shni `:dev:api-client` moduliga
// yoziladi. Spec — API'ning yagona manbasi.
//
// Tasklar:
//   cleanSwagger    — spec'ni build papkasiga tayyorlaydi (kelajakda normalizatsiya shu yerda)
//   openApiGenerate — Kotlin/Multiplatform klientini `:dev:api-client` ga generatsiya qiladi
//   generateAllApi  — barcha generatsiyani bitta buyruq bilan ishga tushiradi (iym'dagidek nom)

plugins {
    alias(libs.plugins.openapi.generator)
}

// Kirish spec'i shu modulda yashaydi.
val specFile = layout.projectDirectory.file("student-clubs.json")

// Tayyorlangan spec build papkasiga tushadi (iym'dagi `v2-processed.json` ekvivalenti).
val processedSpec = layout.buildDirectory.file("student-clubs-processed.json")

// Generatsiya qilingan kod qo'shni `:dev:api-client` moduliga chiqadi.
val clientDir = layout.projectDirectory.dir("../api-client")

// iym'dagi `cleanSwaggerV2` bosqichining moslashtirilgan varianti — hozircha spec'ni
// build papkasiga ko'chiradi. Keyinchalik spec tozalash/normalizatsiya shu yerga qo'shiladi.
val cleanSwagger = tasks.register<Copy>("cleanSwagger") {
    from(specFile)
    into(processedSpec.get().asFile.parentFile)
    rename { "student-clubs-processed.json" }
}

openApiGenerate {
    generatorName.set("kotlin")
    library.set("multiplatform")
    inputSpec.set(processedSpec.map { it.asFile.path })
    outputDir.set(clientDir.asFile.path)
    packageName.set("dev.core.network.generated")
    apiPackage.set("dev.core.network.generated.api")
    modelPackage.set("dev.core.network.generated.model")
    configOptions.set(
        mapOf(
            "dateLibrary" to "kotlinx-datetime",
            "collectionType" to "list",
            "omitGradleWrapper" to "true",
            "generateApiTests" to "false",
            "generateModelTests" to "false",
            "generateApiDocumentation" to "false",
            "generateModelDocumentation" to "false",
        ),
    )
}

// Generatsiya spec tayyorlangandan keyin ishlaydi.
tasks.named("openApiGenerate") {
    dependsOn(cleanSwagger)
}

// iym'dagi `generateAllApi` taskining ekvivalenti — bitta joydan hamma API'ni generatsiya qiladi.
tasks.register("generateAllApi") {
    group = "openapi"
    description = "Barcha OpenAPI klientlarini generatsiya qiladi (student-clubs.json)"
    dependsOn("openApiGenerate")
}
