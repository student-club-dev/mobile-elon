package dev.core.uikit.map

/**
 * Ilovadagi BARCHA xaritalar uchun yagona manba.
 *
 * Xarita dvigateli — **MapLibre GL JS + OpenStreetMap** vektor plitkalari, WebView ichida.
 * Ilgari ElonUz o'zining qo'lda yozilgan raster plitka dvigatelida ishlardi; u sekin edi va
 * surish/zoom ishoralari tizim xaritalaridan sezilarli farq qilardi. Endi StudentClubs bilan
 * bir xil dvigatel ishlatiladi.
 *
 * Ikkala uslub ham bepul va API kalit talab qilmaydi (ma'lumot — OpenStreetMap).
 */
internal fun mapStyleUrl(dark: Boolean): String =
    if (dark) "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"
    else "https://tiles.openfreemap.org/styles/liberty" // sariq yo'llar, POI — Yandex uslubi

/** Plitkalar kelguncha ko'rinadigan fon — xarita "oq ekran" bo'lib turmasin. */
internal fun mapBackgroundColor(dark: Boolean): String = if (dark) "#12101F" else "#E8E6F2"

/**
 * WebView'ning baza manzili. Sahifa plitka serveri bilan bir origin'da bo'lsa WebView
 * so'rovlarni to'smaydi.
 */
internal const val MAP_BASE_URL = "https://tiles.openfreemap.org"

/** Joy tanlashda yaqin turamiz — uy raqami darajasida aniqlik kerak. */
internal const val MAP_PICKER_ZOOM = 16.0

/**
 * Platformaga xos `actual` funksiyalarda holatni Compose snapshot'isiz saqlash uchun.
 * `AndroidView`/`UIKitView` ning `update` bloki ichida o'qish/yozish rekompozitsiya
 * sikliga olib kelmasligi kerak — shuning uchun oddiy konteyner.
 */
internal class Holder<T>(var value: T)
