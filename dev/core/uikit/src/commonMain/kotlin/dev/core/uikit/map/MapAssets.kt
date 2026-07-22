package dev.core.uikit.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.core.uikit.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * MapLibre kutubxonasi — ilova ichiga joylangan (`composeResources/files/`), CDN'dan EMAS.
 *
 * CDN'dan `<script src>` bilan tortilsa xarita har ochilganda ~800 KB JS qaytadan tarmoqdan
 * yuklanardi: sekin aloqada oq ekranda osilib qolardi va offline umuman ishlamasdi. Endi
 * fayllar APK/IPA ichida — birinchi o'qishdan keyin xotirada keshlanadi.
 *
 * Bir marta o'qiladi va butun jarayon davomida saqlanadi (fayl o'zgarmaydi).
 */
private object MapLibreAssets {
    var js: String? = null
    var css: String? = null
    val loaded: Boolean get() = js != null && css != null
}

/**
 * Kutubxona fayllarini (bir marta) o'qiydi. `false` qaytsa xarita hali qurilmaydi —
 * bu bir necha millisekund, birinchi ochilishda bir marta.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun rememberMapLibreReady(): Boolean {
    var ready by remember { mutableStateOf(MapLibreAssets.loaded) }
    LaunchedEffect(Unit) {
        if (!MapLibreAssets.loaded) {
            runCatching {
                MapLibreAssets.js = Res.readBytes("files/maplibre-gl.js").decodeToString()
                MapLibreAssets.css = Res.readBytes("files/maplibre-gl.css").decodeToString()
            }
        }
        ready = MapLibreAssets.loaded
    }
    return ready
}

/** Sahifaga joylash uchun kutubxona matni. */
internal fun mapLibreJs(): String = MapLibreAssets.js.orEmpty()

internal fun mapLibreCss(): String = MapLibreAssets.css.orEmpty()
