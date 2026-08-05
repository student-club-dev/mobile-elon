import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// API-generatsiya moduli — `iym-native-business` loyihasidagi `:dev:api-client-generator`
// joylashuvining moslashtirilgan (bitta spec'li) varianti.
//
// Bu modul kod chiqarmaydi: u faqat spec'ni (`elon-uz.json`) ushlaydi, normalizatsiya qiladi va
// generatsiya tasklarini beradi. Generatsiya qilingan Kotlin klienti qo'shni `:dev:api-client`
// moduliga yoziladi. Spec — API'ning yagona manbasi.
//
// Tasklar:
//   cleanSwagger    — xom spec'ni normalizatsiya qilib build papkasiga tayyorlaydi
//   openApiGenerate — Kotlin/Multiplatform klientini `:dev:api-client` ga generatsiya qiladi
//   generateAllApi  — barcha generatsiyani bitta buyruq bilan ishga tushiradi (iym'dagidek nom)

plugins {
    alias(libs.plugins.openapi.generator)
}

// Kirish spec'i shu modulda yashaydi — backend bergan XOM (NestJS/Swagger) fayl.
val specFile = layout.projectDirectory.file("elon-uz.json")

// Tayyorlangan spec build papkasiga tushadi (iym'dagi `v2-processed.json` ekvivalenti).
val processedSpec = layout.buildDirectory.file("elon-uz-processed.json")

// Generatsiya qilingan kod SHU modulning `build/` papkasiga chiqadi, `:dev:api-client` esa uni
// srcDir sifatida ulaydi (`builtBy` orqali).
//
// Nega `../api-client` EMAS: outputDir butun modul papkasini qamrasa, Gradle uchun `api-client/build/`
// ham shu taskning chiqishi bo'lib qoladi — natijada o'sha papkaga yozadigan har bir task
// "uses this output without declaring dependency" validatsiya xatosini beradi.
val clientDir = layout.buildDirectory.dir("generated-client")

/**
 * Backend spec'ining bazaviy manzili. `paths` dan `/v1` prefiksi olib tashlanadi (pastga qarang),
 * shuning uchun versiya aynan shu yerda — server manzilida — turadi. Ilova ishlatadigan manzil
 * `dev.core.di.DEV_BASE_URL` da belgilanadi (u ham `/v1/` bilan tugaydi).
 */
val apiServerUrl = "https://api.studentclub.uz/v1"

/**
 * **cleanSwagger** — xom spec'ni generatorga yaroqli holatga keltiradi (iym'dagi `cleanSwaggerV2`
 * bosqichining ekvivalenti). Spec'ni NestJS chiqargani uchun unda generator tushunmaydigan bir
 * necha naqsh bor; hammasi shu yerda tuzatiladi, `elon-uz.json` faylining o'ziga TEGILMAYDI —
 * yangi spec kelsa uni shunchaki ustiga yozib, `generateAllApi` ni qayta ishga tushirasiz.
 *
 * 1. **Konvert yechiladi.** Har javob `allOf: [BaseResponseDto, { result: X }]` shaklida keladi.
 *    Klientda konvertni [dev.core.network.response.EnvelopeUnwrapPlugin] shaffof ochadi, shuning
 *    uchun generatsiya qilingan metod TO'G'RIDAN-TO'G'RI `X` ni qaytarishi kerak. `result` bo'sh
 *    (`No payload for this endpoint`) bo'lsa — javob tanasiz (`Unit`).
 * 2. **Xato javoblarining tanasi olib tashlanadi** — aks holda har bir 401/404 uchun keraksiz
 *    inline model generatsiya qilinadi. Xatolarni konvert plagini + `AppException` hal qiladi.
 * 3. **`/v1` prefiksi yo'llardan olib tashlanadi** — u bazaviy manzilda (`servers`) turadi.
 * 4. **Tipsiz nullable maydonlar tiplanadi.** NestJS `string | null` ni `{"type":"object",
 *    "nullable":true}` deb yozadi; generator undan `kotlin.Any?` chiqaradi va kotlinx.serialization
 *    uni kompilyatsiya qilolmaydi. Haqiqiy tip `format` → `example` → maydon nomi bo'yicha tiklanadi.
 * 5. **Butun sonlar `integer` ga o'tkaziladi** — NestJS hamma sonni `number` deb yozadi, natijada
 *    `sortOrder`/`viewsCount` kabi maydonlar `Double` bo'lib qolardi (kasrlilarda `format` bor).
 * 6. **operationId'lar qisqartiriladi** — `BusinessController_getMy` → `getMy`. Tag ichida nom
 *    takrorlansa, to'liq (prefiksli) nom saqlanadi.
 * 7. **Tag nomlari ASCII'ga keltiriladi va guruhlanadi** — `Auth — Business OTP` va qo'shnilari
 *    bitta `AuthBusinessApi` klassiga yig'iladi (aks holda 8 ta mayda API klassi chiqadi).
 * 8. **`$ref` yonidagi `nullable` saqlanadi** — OpenAPI 3.0 da `$ref` bilan yonma-yon turgan
 *    kalitlar e'tiborsiz qoladi, shuning uchun `{"nullable": true, "$ref": X}` `allOf` ichiga
 *    o'raladi (aks holda `UserProfileDto.gender` null bo'lolmay, javob parse bo'lmasdi).
 * 9. **Yetib bo'lmaydigan sxemalar kesiladi.** Backend bitta katta swagger chiqaradi: biznes
 *    spec'ida `components.schemas` ichida admin panel va talaba ilovasining ham (Admin*,
 *    Story*, Message*, Search*…) 200 dan ortiq modeli yotadi, holbuki 48 ta yo'lning
 *    birortasi ularga tegmaydi. Kesilmasa generator o'sha modellarni ham chiqaradi va
 *    `:dev:api-client` keraksiz yuzlab fayl bilan shishadi.
 */
val cleanSwagger = tasks.register("cleanSwagger") {
    group = "openapi"
    description = "Xom OpenAPI spec'ini normalizatsiya qilib build papkasiga tayyorlaydi"

    val input = specFile.asFile
    val output = processedSpec.get().asFile
    val serverUrl = apiServerUrl
    inputs.file(input)
    outputs.file(output)

    doLast {
        @Suppress("UNCHECKED_CAST")
        val root = JsonSlurper().parse(input) as MutableMap<String, Any?>

        // --- Tag nomi → ASCII klass nomi -------------------------------------------------
        fun camel(raw: String): String = raw
            .split(Regex("[^A-Za-z0-9]+"))
            .filter { it.isNotBlank() }
            .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }

        fun tagAlias(raw: String): String {
            val c = camel(raw)
            return when {
                c.startsWith("AuthStudent") -> "AuthStudent"
                c.startsWith("AuthBusiness") -> "AuthBusiness"
                c == "Profiles" -> "Profile"
                else -> c
            }
        }

        // --- operationId → metod nomi (qo'lda berilgan nomlar) ---------------------------
        // Qisqartirilgan nom spec bo'yicha noyob bo'lmasa, pastdagi qoida uni kontroller
        // nomi bilan prefikslaydi (`ListingSubmitController_submit` → `listingSubmitSubmit`).
        // Bir nechta joyda bu ham chirkin, ham chaqiruv joyini bekorga sindiradi, shuning
        // uchun to'qnashgan juftlarга nom SHU YERDA beriladi: har biriga o'z ma'nosidagi
        // nom, tarixiy chaqiruvchi esa qisqa nomni saqlab qoladi.
        val operationNames = mapOf(
            // `submit` — ikkitasi: e'lon (ilova ancha oldindan chaqiradi) va biznes (yangi).
            "ListingSubmitController_submit" to "submit",
            "BusinessController_submit" to "businessSubmit",
            // `verify` — OTP tasdiqlash (mavjud) va kassirning kod tekshiruvi (yangi).
            "BusinessOtpController_verify" to "verify",
            "RedemptionsController_verify" to "redeemVerify",
            "RedemptionsController_confirm" to "redeemConfirm",
            // Geo: kontrakt yo'llari (`/geo/regions`) va admin panel ishlatadigan eski
            // yo'llar (`/regions`) bitta servis ustida — ikkalasi ham spec'da qoladi.
            "RegionsController_getRegions" to "getRegions",
            "GeoRegionsController_getRegions" to "getGeoRegions",
            "DistrictsController_getDistricts" to "getDistricts",
            "GeoRegionsController_getDistricts" to "getGeoDistricts",
        )

        // --- Sxema tugunlarini tiplash ---------------------------------------------------
        // Tipi `format`/`example` dan aniqlanmaydigan mantiqiy maydonlar (NestJS ularni ham
        // tipsiz `object` deb yozadi).
        val booleanProps = setOf("multiple", "requiresCustomName")

        fun retype(node: MutableMap<String, Any?>, propName: String?) {
            val format = node["format"] as? String
            val example = node["example"]
            when {
                format == "int32" || format == "int64" -> node["type"] = "integer"
                format == "double" || format == "float" -> node["type"] = "number"
                propName != null && propName in booleanProps -> node["type"] = "boolean"
                example is Number -> {
                    node["type"] = "integer"
                    node["format"] = "int32"
                }
                else -> node["type"] = "string"
            }
        }

        fun fixTypes(node: Any?, propName: String?) {
            when (node) {
                is MutableMap<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val map = node as MutableMap<String, Any?>
                    // (8) `{"nullable": true, "$ref": X}` — OpenAPI 3.0 da `$ref` yonidagi kalitlar
                    // E'TIBORSIZ qoldiriladi, natijada generator maydonni null bo'lmaydigan qilib
                    // chiqaradi va backend `null` yuborganda deserializatsiya yiqiladi.
                    // `allOf` ichiga o'rash nullable'ni saqlaydi.
                    if (map.containsKey("\$ref") && map.size > 1) {
                        val ref = map.remove("\$ref")
                        map["allOf"] = listOf(mapOf("\$ref" to ref))
                    }

                    val hasShape = map.containsKey("properties") ||
                        map.containsKey("additionalProperties") ||
                        map.containsKey("\$ref") ||
                        map.containsKey("allOf")

                    // (4) tipsiz nullable object → haqiqiy tip
                    if (map["type"] == "object" && map["nullable"] == true && !hasShape) {
                        retype(map, propName)
                    }

                    // (5) `number` → `integer`. NestJS hamma sonni `number` deb yozadi;
                    // kasrlilarida odatда `format: double` bo'ladi, shuning uchun formatsiz
                    // `number` — deyarli har doim butun son (`sortOrder`, `viewsCount`).
                    //
                    // "Deyarli" — chunki istisno bor: `MetroStationDto.lat/lng` formatsiz
                    // `number`, lekin qiymati kasrli (41.27436). Ularni `Int` qilib qo'ysak
                    // javob parse bo'lmaydi, shuning uchun `example` kasrli bo'lsa u
                    // formatsizlikdan kuchliroq dalil — maydon `double` bo'lib qoladi.
                    if (map["type"] == "number") {
                        val format = map["format"] as? String
                        val fractionalExample = (map["example"] as? Number)
                            ?.let { it.toDouble() != it.toLong().toDouble() } == true
                        when {
                            fractionalExample -> map["format"] = "double"
                            format == null -> {
                                map["type"] = "integer"
                                map["format"] = "int32"
                            }
                            format == "int32" || format == "int64" -> map["type"] = "integer"
                        }
                    }

                    @Suppress("UNCHECKED_CAST")
                    val properties = map["properties"] as? MutableMap<String, Any?>
                    map.forEach { (key, value) ->
                        when {
                            // Maydon nomi tiplashda kerak (`multiple`, `requiresCustomName`).
                            key == "properties" && properties != null ->
                                properties.forEach { (name, schema) -> fixTypes(schema, name) }
                            // Massiv elementi ota-maydon nomini meros oladi.
                            key == "items" -> fixTypes(value, propName)
                            else -> fixTypes(value, null)
                        }
                    }
                }
                is List<*> -> node.forEach { fixTypes(it, propName) }
            }
        }

        // --- Yo'llar: `/v1` prefiksi, tag, operationId, javob konverti ---------------------
        @Suppress("UNCHECKED_CAST")
        val paths = root["paths"] as MutableMap<String, Any?>
        val methods = setOf("get", "post", "put", "patch", "delete", "head", "options")

        // OpenAPI operationId'lari BUTUN spec bo'yicha noyob bo'lishi shart, shuning uchun
        // qisqartirishdan oldin takrorlanadiganlarini global ro'yxatdan topamiz.
        val shortNames = mutableListOf<String>()
        paths.values.forEach { pathItem ->
            @Suppress("UNCHECKED_CAST")
            (pathItem as MutableMap<String, Any?>).forEach { (method, op) ->
                if (method !in methods) return@forEach
                @Suppress("UNCHECKED_CAST")
                val operation = op as MutableMap<String, Any?>
                val raw = operation["operationId"]?.toString().orEmpty()
                // Nomi qo'lda berilganlar to'qnashuvda qatnashmaydi.
                if (raw !in operationNames) shortNames.add(raw.substringAfter('_', raw))
            }
        }
        val ambiguous = shortNames.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

        val rewrittenPaths = linkedMapOf<String, Any?>()
        paths.forEach { (path, pathItem) ->
            @Suppress("UNCHECKED_CAST")
            val item = pathItem as MutableMap<String, Any?>
            item.forEach { (method, op) ->
                if (method !in methods) return@forEach
                @Suppress("UNCHECKED_CAST")
                val operation = op as MutableMap<String, Any?>

                // (7) tag → ASCII / guruhlangan nom
                val rawTag = (operation["tags"] as? List<*>)?.firstOrNull()?.toString().orEmpty()
                val tag = tagAlias(rawTag)
                operation["tags"] = listOf(tag)

                // (6) operationId → qisqa nom (spec bo'yicha noyob bo'lsa)
                val rawId = operation["operationId"]?.toString().orEmpty()
                val short = rawId.substringAfter('_', rawId)
                operation["operationId"] = operationNames[rawId] ?: if (short in ambiguous) {
                    val prefix = rawId.substringBefore('_').removeSuffix("Controller")
                    prefix.replaceFirstChar(Char::lowercaseChar) + short.replaceFirstChar(Char::uppercaseChar)
                } else {
                    short
                }

                // (1)(2) javoblar: konvertni yechish, xato tanalarini olib tashlash
                @Suppress("UNCHECKED_CAST")
                val responses = operation["responses"] as? MutableMap<String, Any?> ?: mutableMapOf()
                responses.entries.removeAll { (code, _) -> !code.startsWith("2") }
                responses.values.forEach { response ->
                    @Suppress("UNCHECKED_CAST")
                    val resp = response as MutableMap<String, Any?>
                    @Suppress("UNCHECKED_CAST")
                    val json = (resp["content"] as? Map<String, Any?>)
                        ?.get("application/json") as? MutableMap<String, Any?>
                    val allOf = (json?.get("schema") as? Map<*, *>)?.get("allOf") as? List<*>
                        ?: return@forEach

                    val payload = allOf
                        .filterIsInstance<Map<*, *>>()
                        .mapNotNull { it["properties"] as? Map<*, *> }
                        .mapNotNull { it["result"] }
                        .firstOrNull() as? Map<*, *>

                    val hasBody = payload != null && (
                        payload.containsKey("\$ref") ||
                            payload.containsKey("type") ||
                            payload.containsKey("allOf")
                        )
                    if (hasBody) json["schema"] = payload else resp.remove("content")
                }
            }
            // (3) `/v1` prefiksi bazaviy manzilga ko'chadi
            rewrittenPaths[path.removePrefix("/v1")] = item
        }
        root["paths"] = rewrittenPaths

        // (9) yetib bo'lmaydigan sxemalarni kesish — TIPLASHDAN OLDIN, aks holda mehnat
        // hech kim ishlatmaydigan 200 dan ortiq model ustida bekorga sarflanadi.
        @Suppress("UNCHECKED_CAST")
        val components = root["components"] as? MutableMap<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val schemas = components?.get("schemas") as? MutableMap<String, Any?>
        if (schemas != null) {
            fun refsOf(node: Any?, into: MutableSet<String>) {
                when (node) {
                    is Map<*, *> -> node.forEach { (key, value) ->
                        if (key == "\$ref" && value is String) {
                            into.add(value.substringAfterLast('/'))
                        } else {
                            refsOf(value, into)
                        }
                    }
                    is List<*> -> node.forEach { refsOf(it, into) }
                }
            }

            // Yo'llardan boshlab tranzitiv yopilma: sxema faqat boshqa saqlanadigan
            // sxemadan yoki yo'ldan havola qilinsa qoladi.
            val reachable = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            mutableSetOf<String>().also { refsOf(root["paths"], it) }.forEach {
                if (reachable.add(it)) queue.addLast(it)
            }
            while (queue.isNotEmpty()) {
                val next = mutableSetOf<String>()
                refsOf(schemas[queue.removeFirst()], next)
                next.forEach { if (reachable.add(it)) queue.addLast(it) }
            }

            val dropped = schemas.keys.size - reachable.size
            schemas.keys.retainAll(reachable)
            logger.lifecycle("cleanSwagger: $dropped ta ishlatilmaydigan sxema kesildi, ${schemas.size} tasi qoldi")
        }

        // (4)(5) barcha sxemalarni tiplash — komponentlar va inline sxemalar birgalikda
        fixTypes(root["components"], null)
        fixTypes(root["paths"], null)

        // Xom spec'da `servers` bo'sh — generator BASE_URL konstantasi uchun manzil kutadi.
        root["servers"] = listOf(mapOf("url" to serverUrl, "description" to "Dev"))

        output.parentFile.mkdirs()
        output.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(root)))
        logger.lifecycle("cleanSwagger: ${rewrittenPaths.size} ta yo'l normalizatsiya qilindi → ${output.name}")
    }
}

openApiGenerate {
    generatorName.set("kotlin")
    library.set("multiplatform")
    inputSpec.set(processedSpec.map { it.asFile.path })
    outputDir.set(clientDir.map { it.asFile.path })
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

/**
 * Generatsiyadan oldin eski chiqishni o'chiradi. Generator faylni faqat YOZADI — o'chirmaydi,
 * shuning uchun spec'dan chiqib ketgan endpoint/model (masalan eski `AuthApi`, `DiscountCardDto`)
 * tozalanmasa build papkasida qolib ketadi va "mavjud" ko'rinadi.
 */
val cleanGeneratedClient = tasks.register<Delete>("cleanGeneratedClient") {
    group = "openapi"
    description = "Generatsiya qilingan eski klient kodini o'chiradi"
    delete(clientDir)
}

// Generatsiya: eski chiqish o'chiriladi → spec tayyorlanadi → klient yoziladi.
tasks.named("openApiGenerate") {
    dependsOn(cleanGeneratedClient, cleanSwagger)
}

// iym'dagi `generateAllApi` taskining ekvivalenti — bitta joydan hamma API'ni generatsiya qiladi.
tasks.register("generateAllApi") {
    group = "openapi"
    description = "Barcha OpenAPI klientlarini generatsiya qiladi (elon-uz.json)"
    dependsOn("openApiGenerate")
}
