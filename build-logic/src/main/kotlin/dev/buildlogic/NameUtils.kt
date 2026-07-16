package dev.buildlogic

/**
 * Modul yo'lidan Android namespace va iOS framework nomini hisoblaydi — shunda har bir
 * modul ularni qo'lda yozmaydi (iym-native-business dagi NameUtils ekvivalenti).
 */
object NameUtils {
    /** `:dev:feature:discounts:domain` -> `dev.feature.discounts.domain` */
    fun namespace(path: String): String = path.removePrefix(":").replace(":", ".")

    /**
     * iOS framework nomi — yo'l segmentlaridan `dev`/`feature`/`core` tashlanadi va qolganlari
     * camelCase birlashtiriladi:
     *   `:dev:feature:discounts:domain` -> `discountsDomain`
     *   `:dev:core:network`             -> `network`
     */
    fun frameworkName(path: String): String {
        val parts = path.removePrefix(":").split(":")
            .filter { it != "dev" && it != "feature" && it != "core" }
        return parts.mapIndexed { i, s ->
            if (i == 0) s else s.replaceFirstChar { c -> c.uppercase() }
        }.joinToString("")
    }
}
