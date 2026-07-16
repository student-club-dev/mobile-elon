package dev.feature.discounts.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Xaritada tanlangan nuqta. */
data class MapPoint(val lat: Double, val lng: Double)

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
 * Ishoralar: **bitta barmoq** — surish, **ikki barmoq** — kattalashtirish/kichraytirish,
 * bosish — xaritani o'sha nuqtaga markazlash. Tugmalar (+/−) ham bor.
 *
 * Xarita — **CARTO Voyager plitkalari** (OSM ma'lumoti), WebView ichida. Tekin, API kalit
 * talab qilmaydi; Android va iOS bitta HTML'ni yuklaydi.
 *
 * @param initial xarita ochilishidagi markaz (odatda foydalanuvchining joylashuvi)
 * @param onCenterChanged belgi ostidagi joy o'zgardi (surish, zoom, qidiruv natijasi)
 * @param centerRequest xaritani ko'chirish so'rovi (joylashuv keyinroq aniqlansa yoki qidiruv)
 */
@Composable
expect fun MapPicker(
    initial: MapPoint?,
    onCenterChanged: (MapPoint) -> Unit,
    modifier: Modifier = Modifier,
    centerRequest: MapCenterRequest? = null,
)

/** Xarita bosilganda JS shu ko'prik orqali koordinatani Kotlin'ga uzatadi. */
internal const val MAP_BRIDGE = "StudentClubsMap"

/**
 * Plitka serveri — **CARTO Voyager** (OpenStreetMap ma'lumoti ustiga toza dizayn).
 *
 * Nega OSM'ning o'z plitkalari emas: Voyager Yandex/Google'ga yaqinroq ko'rinadi (yumshoq
 * ranglar, o'qiladigan yorliqlar, kamroq shovqin) va **@2x retina** plitkalarni beradi —
 * telefon ekranida ancha aniq chiqadi. Ikkalasi ham tekin, API kalit va hisob talab qilmaydi.
 *
 * Yorliqlar OSM'dan keladi, ya'ni o'zbekcha ("Yunusobod mahallasi") — Yandex bilan bir xil manba.
 *
 * Plitka kelmasa (bloklangan yoki xizmat ishlamayotgan bo'lsa) sahifa avtomatik
 * [FALLBACK_TILE_HOST] ga o'tadi — xarita baribir chiqadi.
 */
internal const val TILE_HOST = "https://basemaps.cartocdn.com"

/** Zaxira: OSM'ning o'z plitkalari (sodda ko'rinish, lekin doim ishlaydi). */
internal const val FALLBACK_TILE_HOST = "https://tile.openstreetmap.org"

/** Kotlin tomondan chaqiriladi: xaritani (va u bilan belgini) boshqa joyga olib boradi. */
internal fun jsSetCenter(point: MapPoint): String = "setCenter(${point.lat}, ${point.lng})"

/**
 * Xarita sahifasi — **hech qanday tashqi kutubxonasiz** (Leaflet ham, CDN ham yo'q).
 *
 * Sabab: CDN'dan kelgan skript yuklanmasa xarita butunlay ochilmaydi. Endi sahifa faqat OSM
 * plitka rasmlarini so'raydi, qolgan hammasi — shu yerdagi toza JS: Web Mercator proyeksiyasi,
 * surish, **ikki barmoqli zoom**, belgi qo'yish.
 *
 * O'lchamlar `position: fixed` + `vw/vh` da: `height: 100%` bu WebView'da 0 balandlik bergan
 * va plitkalar yuklangani bilan ko'rinmay qolgan edi.
 */
internal fun mapHtml(center: MapPoint): String = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <style>
    html, body { margin: 0; padding: 0; overflow: hidden;
                 background: #E8E6F2; font: 600 12px -apple-system, Roboto, sans-serif; }
    #map { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
           touch-action: none; overflow: hidden; }
    #map img { position: absolute; width: 256px; height: 256px; user-select: none;
               -webkit-user-drag: none; pointer-events: none; }
    /* Tanlash belgisi — Yandex'dagidek EKRAN MARKAZIDA qotib turadi. Xarita surilganda
       belgi ostidagi joy o'zgaradi, ya'ni tanlangan nuqta xarita bilan birga "suriladi". */
    #pin { position: fixed; left: 50%; top: 50%; width: 36px; height: 36px;
           margin-left: -18px; margin-top: -36px; pointer-events: none; z-index: 9; }
    #pin svg { width: 36px; height: 36px; filter: drop-shadow(0 3px 4px rgba(0,0,0,.35)); }
    /* Belgi uchi turgan aniq nuqta. */
    #tip { position: fixed; left: 50%; top: 50%; width: 6px; height: 6px;
           margin-left: -3px; margin-top: -3px; border-radius: 50%;
           background: rgba(20,16,45,.55); pointer-events: none; z-index: 9; }
    .hint { position: fixed; top: 10px; left: 50%; transform: translateX(-50%); z-index: 10;
            background: rgba(20,16,45,.85); color: #fff; padding: 7px 12px; border-radius: 10px;
            white-space: nowrap; }
    .zoom { position: fixed; right: 10px; bottom: 34px; z-index: 10; display: flex;
            flex-direction: column; gap: 6px; }
    .zoom div { width: 38px; height: 38px; border-radius: 10px; background: #fff; color: #14102D;
                display: flex; align-items: center; justify-content: center; font-size: 20px;
                box-shadow: 0 2px 6px rgba(0,0,0,.2); }
    .attr { position: fixed; right: 4px; bottom: 4px; z-index: 10; font-size: 9px;
            color: #555; background: rgba(255,255,255,.7); padding: 1px 4px; border-radius: 4px; }
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
  <div class="hint" id="hint">Xaritani suring — belgi joyni ko'rsatadi</div>
  <div class="zoom"><div id="zin">+</div><div id="zout">−</div></div>
  <div class="attr">© OpenStreetMap · © CARTO</div>

  <script>
    var TILE = '$TILE_HOST';
    var FALLBACK = '$FALLBACK_TILE_HOST';
    var TS = 256;
    var useFallback = false;

    /**
     * Plitka manzili. CARTO Voyager @2x — 512px rasm 256px joyda ko'rsatiladi, shuning uchun
     * retina ekranda aniq chiqadi. Zaxira rejimda oddiy OSM plitkasi.
     */
    function tileUrl(z, x, y) {
      if (useFallback) return FALLBACK + '/' + z + '/' + x + '/' + y + '.png';
      return TILE + '/rastertiles/voyager/' + z + '/' + x + '/' + y + '@2x.png';
    }
    var MIN_Z = 3, MAX_Z = 19;

    var z = 16;
    var center = { lat: ${center.lat}, lng: ${center.lng} };

    var map = document.getElementById('map');
    var hint = document.getElementById('hint');
    var tiles = {};

    function scale() { return TS * Math.pow(2, z); }

    // Oyna o'lchami — konteynerning clientWidth/Height ga ishonmaymiz: u ba'zi WebView'larda
    // 0 qaytardi va xarita ko'rinmay qoldi.
    function vw() { return window.innerWidth; }
    function vh() { return window.innerHeight; }

    function project(lat, lng) {
      var s = scale();
      var sinLat = Math.sin(lat * Math.PI / 180);
      return {
        x: (lng + 180) / 360 * s,
        y: (0.5 - Math.log((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI)) * s
      };
    }

    function unproject(x, y) {
      var s = scale();
      var n = Math.PI - 2 * Math.PI * y / s;
      return {
        lat: 180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n))),
        lng: x / s * 360 - 180
      };
    }

    /** Ekranning chap-yuqori burchagi jahon pikselida. */
    function origin() {
      var c = project(center.lat, center.lng);
      return { x: c.x - vw() / 2, y: c.y - vh() / 2 };
    }

    /** Ekrandagi (x, y) nuqta ostidagi koordinata. */
    function screenToLatLng(x, y) {
      var o = origin();
      return unproject(o.x + x, o.y + y);
    }

    function render() {
      // O'lchamni JS'da ham qo'yamiz — ba'zi WebView'larda CSS balandligi qo'llanmadi.
      map.style.width = vw() + 'px';
      map.style.height = vh() + 'px';

      var o = origin();
      var maxTile = Math.pow(2, z);
      var x0 = Math.floor(o.x / TS), x1 = Math.floor((o.x + vw()) / TS);
      var y0 = Math.floor(o.y / TS), y1 = Math.floor((o.y + vh()) / TS);

      var visible = {};
      for (var tx = x0; tx <= x1; tx++) {
        for (var ty = y0; ty <= y1; ty++) {
          if (ty < 0 || ty >= maxTile) continue;
          var wx = ((tx % maxTile) + maxTile) % maxTile;
          var key = z + '/' + wx + '/' + ty;
          visible[key] = true;

          var img = tiles[key];
          if (!img) {
            img = document.createElement('img');
            img.onerror = onTileError;
            img.src = tileUrl(z, wx, ty);
            tiles[key] = img;
            map.appendChild(img);
          }
          img.style.left = (tx * TS - o.x) + 'px';
          img.style.top = (ty * TS - o.y) + 'px';
        }
      }

      for (var key in tiles) {
        if (!visible[key]) { map.removeChild(tiles[key]); delete tiles[key]; }
      }

    }

    /**
     * Plitka kelmadi — bir marta zaxira serverga o'tamiz va hammasini qayta yuklaymiz.
     * Xarita bo'sh qolgandan ko'ra soddaroq ko'rinishda bo'lgani yaxshi.
     */
    function onTileError() {
      if (useFallback) return;
      useFallback = true;
      for (var key in tiles) { map.removeChild(tiles[key]); delete tiles[key]; }
      document.querySelector('.attr').textContent = '© OpenStreetMap';
      render();
    }

    /** Kotlin'ga hodisa yuborish. kind: 'center' — belgi ostidagi joy o'zgardi. */
    function post(kind, lat, lng) {
      if (window.$MAP_BRIDGE && window.$MAP_BRIDGE.onEvent) {
        window.$MAP_BRIDGE.onEvent(kind, lat, lng);
      } else if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.$MAP_BRIDGE) {
        window.webkit.messageHandlers.$MAP_BRIDGE.postMessage({ kind: kind, lat: lat, lng: lng });
      }
    }

    /** Belgi doim markazda — shuning uchun tanlangan joy = xarita markazi. */
    function sendCenter() {
      post('center', center.lat, center.lng);
    }

    /**
     * Kotlin tomondan chaqiriladi: "Mening joylashuvim" tugmasi va joylashuv birinchi marta
     * aniqlanganda. [mark] — ko'k nuqta qo'yiladimi.
     */
    function setCenter(lat, lng, mark) {
      center = { lat: lat, lng: lng };
      if (z < 16) z = 16;
      render();
      sendCenter();
    }

    /** Zoom, lekin ekrandagi (ax, ay) nuqta joyida qoladi (barmoqlar orasi yoki markaz). */
    function zoomTo(newZ, ax, ay) {
      newZ = Math.max(MIN_Z, Math.min(MAX_Z, newZ));
      if (newZ === z) return;

      var anchor = screenToLatLng(ax, ay); // zoomdan OLDINGI koordinata
      z = newZ;

      // Yangi masshtabda o'sha koordinata yana (ax, ay) da turishi uchun markazni suramiz.
      var p = project(anchor.lat, anchor.lng);
      center = unproject(p.x - ax + vw() / 2, p.y - ay + vh() / 2);
      render();
      sendCenter();
    }

    // -----------------------------------------------------------------------
    // Ishoralar: 1 barmoq — surish, 2 barmoq — zoom (pinch), bosish — belgi
    // -----------------------------------------------------------------------
    var pointers = {};   // faol barmoqlar
    var moved = 0;       // umumiy siljish (bosish/surishni ajratish uchun)
    var pinch = null;    // {startDist, startZ, midX, midY}

    function pointerList() {
      var list = [];
      for (var id in pointers) list.push(pointers[id]);
      return list;
    }

    function distance(a, b) {
      var dx = a.x - b.x, dy = a.y - b.y;
      return Math.sqrt(dx * dx + dy * dy);
    }

    map.addEventListener('pointerdown', function (e) {
      map.setPointerCapture(e.pointerId);
      pointers[e.pointerId] = { x: e.clientX, y: e.clientY };
      moved = 0;

      var list = pointerList();
      if (list.length === 2) {
        pinch = {
          startDist: distance(list[0], list[1]),
          startZ: z,
          midX: (list[0].x + list[1].x) / 2,
          midY: (list[0].y + list[1].y) / 2
        };
      }
    });

    map.addEventListener('pointermove', function (e) {
      var p = pointers[e.pointerId];
      if (!p) return;

      var dx = e.clientX - p.x, dy = e.clientY - p.y;
      p.x = e.clientX;
      p.y = e.clientY;
      moved += Math.abs(dx) + Math.abs(dy);

      var list = pointerList();

      if (list.length >= 2 && pinch) {
        // Ikki barmoq: masofa nisbati -> zoom. Barmoqlar orasidagi nuqta joyida qoladi.
        var d = distance(list[0], list[1]);
        if (d > 0 && pinch.startDist > 0) {
          var newZ = Math.round(pinch.startZ + Math.log(d / pinch.startDist) / Math.LN2);
          zoomTo(newZ, pinch.midX, pinch.midY);
        }
        return;
      }

      // Bitta barmoq: surish.
      var o = project(center.lat, center.lng);
      center = unproject(o.x - dx, o.y - dy);
      render();
    });

    function endPointer(e) {
      var wasPinching = pinch !== null;
      delete pointers[e.pointerId];

      if (pointerList().length < 2) pinch = null;

      if (pointerList().length > 0) return;

      // Bosish (surish emas): xaritani o'sha nuqtaga markazlaymiz — belgi o'sha joyga tushadi.
      if (!wasPinching && moved < 10) {
        var p = screenToLatLng(e.clientX, e.clientY);
        center = p;
        render();
      }

      // Surish tugadi — belgi ostidagi joy o'zgargan bo'lishi mumkin.
      sendCenter();
    }

    map.addEventListener('pointerup', endPointer);
    map.addEventListener('pointercancel', endPointer);

    document.getElementById('zin').addEventListener('click', function () {
      zoomTo(z + 1, vw() / 2, vh() / 2);
    });
    document.getElementById('zout').addEventListener('click', function () {
      zoomTo(z - 1, vw() / 2, vh() / 2);
    });

    window.addEventListener('resize', render);
    render();
    sendCenter(); // boshlang'ich joy (manzil darrov aniqlanadi)
  </script>
</body>
</html>
""".trimIndent()
