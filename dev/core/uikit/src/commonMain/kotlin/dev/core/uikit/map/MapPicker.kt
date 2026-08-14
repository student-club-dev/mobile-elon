package dev.core.uikit.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Xaritada tanlangan nuqta. */
data class MapPoint(val lat: Double, val lng: Double)

/**
 * Xaritaning holati — ekran shunga qarab loader yoki xato ko'rsatadi.
 *
 * Nega kerak: tanlash belgisi (`#pin`) — oddiy HTML element va u xarita yuklanmasa ham
 * ko'rinib turadi. Shu sabab uslub/plitkalar kelmaganda ekran "tayyor"dek ko'rinardi:
 * foydalanuvchi xaritani surardi, lekin hech qanday joy tanlanmasdi.
 */
enum class MapStatus {
    /** Uslub yuklandi, surish hodisalari keladi. */
    READY,

    /** Uslub yoki plitkalar yuklanmadi — surish hech narsa bermaydi. */
    FAILED,
}

/** Toshkent markazi — foydalanuvchi joylashuvi noma'lum bo'lsa xarita shu yerdan ochiladi. */
val DefaultMapCenter = MapPoint(lat = 41.311081, lng = 69.240562)

/**
 * Xaritani boshqa joyga ko'chirish so'rovi ("Mening joylashuvim" tugmasi yoki joylashuv
 * birinchi marta aniqlanganda). [id] — takroriy bosishlarni ajratish uchun: bir xil nuqta
 * qayta yuborilsa ham xarita yana o'sha joyga qaytadi.
 */
data class MapCenterRequest(val point: MapPoint, val id: Int)

/**
 * Xaritadan joy tanlash — **Yandex uslubi**: lokatsiya belgisi ekran markazida qotib turadi,
 * foydalanuvchi xaritani suradi va belgi ostidagi joy o'zgaradi. Har surishdan keyin
 * [onCenterChanged] yangi koordinata bilan chaqiriladi, manzil esa teskari geokodlash bilan
 * o'zi to'ladi — foydalanuvchi hech narsa yozmaydi.
 *
 * @param initial xarita ochilishidagi markaz (odatda foydalanuvchining joylashuvi)
 * @param dark qorong'i mavzu uslubi — odatda `appPalette.dark` uzatiladi
 * @param onCenterChanged belgi ostidagi joy o'zgardi (surish, zoom, qidiruv natijasi)
 * @param centerRequest xaritani ko'chirish so'rovi (joylashuv keyinroq aniqlansa yoki qidiruv)
 * @param onStatus xarita tayyor bo'ldi yoki yuklanmadi. Chaqirilmaguncha ekran "yuklanmoqda"
 *   holatida turadi — belgi ko'rinib tursa ham.
 */
@Composable
expect fun MapPicker(
    initial: MapPoint?,
    dark: Boolean,
    onCenterChanged: (MapPoint) -> Unit,
    modifier: Modifier = Modifier,
    centerRequest: MapCenterRequest? = null,
    onStatus: (MapStatus) -> Unit = {},
)

/** Xarita surilganda JS shu ko'prik orqali koordinatani Kotlin'ga uzatadi. */
internal const val MAP_BRIDGE = "ElonUzMap"

/** Kotlin tomondan chaqiriladi: xaritani (va u bilan belgini) boshqa joyga olib boradi. */
internal fun jsSetCenter(point: MapPoint): String = "setCenter(${point.lat}, ${point.lng})"

/**
 * Ekran yopilishidan OLDIN chaqiriladi — MapLibre'ning WebGL kontekstini va animatsiya
 * siklini bo'shatadi.
 *
 * Nega kerak: brauzer bir jarayonda cheklangan sondagi WebGL kontekstini ko'taradi (Android
 * WebView'da odatda ~16 ta). `WebView.destroy()` ni o'zi kontekstni darrov qaytarmaydi,
 * shuning uchun xaritani bir necha marta ochib-yopgach yangisiga kontekst yetmay qoladi:
 * xarita qotib qoladi, surish hech qanday hodisa bermaydi. `map.remove()` esa uni
 * **aniq** bo'shatadi.
 */
internal fun jsDestroyMap(): String = "destroyMap()"

/**
 * Joy tanlash sahifasi — MapLibre GL JS ustida.
 *
 * Belgi xaritaga qo'yilmaydi, u **ekran markazida** turgan oddiy HTML element: xarita
 * surilganda belgi qimirlamaydi, ostidagi joy o'zgaradi. Shuning uchun tanlangan nuqta
 * har doim `map.getCenter()` ga teng — alohida marker holatini kuzatish shart emas.
 *
 * @param hint xarita ustidagi ko'rsatma matni — resursdan keladi (bu yerda qotib qolmaydi,
 *   shunda ru/en tillarida ham to'g'ri chiqadi)
 */
internal fun pickerMapHtml(center: MapPoint, dark: Boolean, hint: String): String = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <style>${mapLibreCss()}</style>
  <style>
    html, body, #map { margin: 0; padding: 0; height: 100%; width: 100%; overflow: hidden; }
    #map { position: fixed; top: 0; left: 0; right: 0; bottom: 0;
           background: ${mapBackgroundColor(dark)}; }
    /* Tanlash belgisi — Yandex'dagidek EKRAN MARKAZIDA qotib turadi. */
    #pin { position: fixed; left: 50%; top: 50%; width: 36px; height: 36px;
           margin-left: -18px; margin-top: -36px; pointer-events: none; z-index: 9; }
    #pin svg { width: 36px; height: 36px; filter: drop-shadow(0 3px 4px rgba(0,0,0,.35)); }
    /* Belgi uchi turgan aniq nuqta. */
    #tip { position: fixed; left: 50%; top: 50%; width: 6px; height: 6px;
           margin-left: -3px; margin-top: -3px; border-radius: 50%;
           background: ${if (dark) "rgba(255,255,255,.6)" else "rgba(20,16,45,.55)"};
           pointer-events: none; z-index: 9; }
    .hint { position: fixed; top: 10px; left: 50%; transform: translateX(-50%); z-index: 10;
            background: rgba(20,16,45,.85); color: #fff; padding: 7px 12px; border-radius: 10px;
            white-space: nowrap; font: 600 12px -apple-system, Roboto, sans-serif;
            transition: opacity .4s; }
    .maplibregl-ctrl-group { border-radius: 11px !important; overflow: hidden; }
    /* Zoom boshqaruvi — pastdan yuqoriroqda: uning TAGIDA "mening joylashuvim"
       tugmasi turadi (u Compose tomonida chiziladi). */
    .maplibregl-ctrl-bottom-right { margin-bottom: 58px; margin-right: 8px; }
    /* Atribut: matn yig'ilgan, faqat (i) tugmasi ko'rinadi. Yozuvning o'zi ekranning
       pastki qismini butunlay egallab, "Pick this spot" tugmasi bilan qo'shilib ketardi.
       Bosilganda matn ochiladi — OpenStreetMap talabi buziladigan joyi yo'q. */
    .maplibregl-ctrl-attrib { background: transparent !important; }
    .maplibregl-ctrl-attrib-inner { display: none !important; }
    .maplibregl-ctrl-attrib.maplibregl-compact-show .maplibregl-ctrl-attrib-inner {
      display: block !important; background: rgba(255,255,255,.92); padding: 2px 6px;
      border-radius: 8px;
    }
    .maplibregl-ctrl-bottom-left { margin-bottom: 6px; margin-left: 6px; }
  </style>
</head>
<body>
  <div id="map"></div>
  <div id="tip"></div>
  <div id="pin">
    <svg viewBox="0 0 24 24" fill="#7C5CFF" stroke="#fff" stroke-width="1.6">
      <path d="M12 22s7-6.1 7-11a7 7 0 1 0-14 0c0 4.9 7 11 7 11z"/>
      <circle cx="12" cy="11" r="2.6" fill="#fff" stroke="none"/>
    </svg>
  </div>
  <div class="hint" id="hint">$hint</div>

  <script>${mapLibreJs()}</script>
  <script>
    var map = new maplibregl.Map({
      container: 'map',
      style: '${mapStyleUrl(dark)}',
      center: [${center.lng}, ${center.lat}],
      zoom: $MAP_PICKER_ZOOM,
      attributionControl: false
    });
    map.addControl(new maplibregl.AttributionControl({ compact: true }));
    map.addControl(new maplibregl.NavigationControl({ showCompass: false }), 'bottom-right');

    /**
     * Kotlin'ga hodisa yuborish.
     * kind: 'center' — joy o'zgardi, 'ready' — xarita tayyor, 'error' — yuklanmadi.
     */
    function post(kind, lat, lng) {
      if (window.$MAP_BRIDGE && window.$MAP_BRIDGE.onEvent) {
        window.$MAP_BRIDGE.onEvent(kind, lat, lng);
      } else if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.$MAP_BRIDGE) {
        window.webkit.messageHandlers.$MAP_BRIDGE.postMessage({ kind: kind, lat: lat, lng: lng });
      }
    }

    /** Belgi doim markazda — tanlangan joy = xarita markazi. */
    function sendCenter() {
      var c = map.getCenter();
      post('center', c.lat, c.lng);
    }

    /**
     * Kotlin tomondan chaqiriladi: "Mening joylashuvim" tugmasi va joylashuv birinchi marta
     * aniqlanganda. `jumpTo` — animatsiyasiz, aks holda `moveend` kech kelib manzil
     * eskisicha qolib ketardi.
     */
    function setCenter(lat, lng) {
      map.jumpTo({ center: [lng, lat], zoom: Math.max(map.getZoom(), $MAP_PICKER_ZOOM) });
      sendCenter();
    }

    // Surish/zoom tugaganda bir marta xabar beramiz — har kadrga geokodlash so'rovi ketmasin.
    map.on('moveend', sendCenter);

    /**
     * Uslub yoki plitka yuklanmasa MapLibre `load` ni UMUMAN chiqarmaydi va surish ham
     * hodisa bermaydi. Bunda ekranda faqat fon rangi va belgi qoladi — foydalanuvchi
     * xaritani surayotganday bo'ladi, joy esa hech qachon tanlanmaydi. Shuning uchun
     * xatoni Kotlin'ga aytamiz.
     */
    map.on('error', function () { post('error', 0, 0); });

    /**
     * WebGL konteksti yo'qolsa xarita jimgina qotadi: rasm turadi, surish ishlamaydi.
     * Foydalanuvchi buni "xarita ishlamayapti" deb ko'radi — shuning uchun xato deb aytamiz.
     */
    map.getCanvas().addEventListener('webglcontextlost', function () { post('error', 0, 0); });

    /** Kotlin ekran yopilishidan oldin chaqiradi — kontekst va rAF sikli bo'shatiladi. */
    function destroyMap() {
      try { map.remove(); } catch (e) {}
    }

    map.on('load', function () {
      post('ready', 0, 0);
      sendCenter(); // boshlang'ich joy — manzil darrov aniqlanadi
      // Ko'rsatma bir necha soniyadan keyin xaritani to'smasin.
      setTimeout(function () {
        var h = document.getElementById('hint');
        if (h) { h.style.opacity = '0'; setTimeout(function () { h.remove(); }, 500); }
      }, 3500);
    });
  </script>
</body>
</html>
""".trimIndent()
