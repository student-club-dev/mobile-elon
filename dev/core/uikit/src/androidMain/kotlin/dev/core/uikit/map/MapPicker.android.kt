package dev.core.uikit.map

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.map_drag_hint
import org.jetbrains.compose.resources.stringResource

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun MapPicker(
    initial: MapPoint?,
    dark: Boolean,
    onCenterChanged: (MapPoint) -> Unit,
    modifier: Modifier,
    centerRequest: MapCenterRequest?,
    onStatus: (MapStatus) -> Unit,
) {
    // WebView faqat bir marta quriladi, callback esa har rekompozitsiyada yangilanishi mumkin —
    // ko'prik eng oxirgisini chaqirishi uchun uni shu yerda ushlab turamiz.
    val currentOnCenter by rememberUpdatedState(onCenterChanged)
    val currentOnStatus by rememberUpdatedState(onStatus)

    val hint = stringResource(Res.string.map_drag_hint)

    // MapLibre ilova ichidan o'qiladi (CDN emas) — tayyor bo'lmaguncha WebView qurilmaydi,
    // aks holda sahifa kutubxonasiz yuklanib bo'sh qolardi.
    if (!rememberMapLibreReady()) return

    // Boshlang'ich markaz FAQAT bir marta olinadi: keyingi ko'chishlar `centerRequest` orqali
    // JS bilan bajariladi, sahifa qayta yuklanmaydi.
    val initialCenter = remember { initial ?: DefaultMapCenter }
    val html = remember(dark, hint) { pickerMapHtml(initialCenter, dark, hint) }

    // Bu holatlar Compose snapshot'i EMAS — `update` ichida o'qish/yozish sikl keltirmaydi.
    val pageReady = remember { Holder(false) }
    val lastCenterId = remember { Holder<Int?>(null) }
    val lastHtml = remember { Holder("") }

    // Sahifa yuklanmasidan turib `setCenter(...)` ni chaqirsak, u hali mavjud emas va so'rov
    // yo'qoladi (xarita boshlang'ich markazida qolib ketadi). Shuning uchun tayyor bo'lgunicha
    // saqlab turamiz va `onPageFinished` da bajaramiz.
    val pending = remember { Holder<MapCenterRequest?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(0)

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        pageReady.value = true
                        // Sahifa yuklanguncha kelgan "bu yerga ko'ch" so'rovini endi bajaramiz.
                        pending.value?.let { request ->
                            pending.value = null
                            view.evaluateJavascript(jsSetCenter(request.point), null)
                        }
                    }
                }

                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onEvent(kind: String, lat: Double, lng: Double) {
                            // JS oqimidan keladi — Compose holatiga faqat asosiy oqimdan tegamiz.
                            when (kind) {
                                "center" -> post { currentOnCenter(MapPoint(lat, lng)) }
                                "ready" -> post { currentOnStatus(MapStatus.READY) }
                                "error" -> post { currentOnStatus(MapStatus.FAILED) }
                            }
                        }
                    },
                    MAP_BRIDGE,
                )

                lastHtml.value = html
                loadDataWithBaseURL(MAP_BASE_URL, html, "text/html", "utf-8", null)
            }
        },
        update = { webView ->
            // Mavzu (yoki til) o'zgardi — sahifani qayta qurish shart, uslub havolasi va
            // ko'rsatma matni HTML ichida.
            if (lastHtml.value != html) {
                lastHtml.value = html
                pageReady.value = false
                webView.loadDataWithBaseURL(MAP_BASE_URL, html, "text/html", "utf-8", null)
                return@AndroidView
            }

            // Joylashuv WebView qurilgandan KEYIN aniqlansa (yoki tugma bosilsa) — xaritani
            // shu yerdan ko'chiramiz. Sahifa hali tayyor bo'lmasa — navbatga qo'yamiz.
            if (centerRequest != null && centerRequest.id != lastCenterId.value) {
                lastCenterId.value = centerRequest.id
                if (pageReady.value) {
                    webView.evaluateJavascript(jsSetCenter(centerRequest.point), null)
                } else {
                    pending.value = centerRequest
                }
            }
        },
        // Ekran yopilganda WebView'ni to'liq bo'shatamiz — MapLibre'ning WebGL konteksti va
        // animatsiya sikli tirik qolsa, xaritani bir necha marta ochib-yopgach yangi kontekst
        // ochilmay xarita qora ekranga aylanadi.
        onRelease = { webView ->
            // AVVAL `map.remove()` — WebGL kontekstini aniq bo'shatadi. `destroy()` ning o'zi
            // uni darrov qaytarmaydi, shuning uchun xaritani bir necha marta ochib-yopgach
            // yangisiga kontekst yetmay qolardi: xarita qotib, surish ishlamay qo'yardi.
            webView.evaluateJavascript(jsDestroyMap(), null)
            webView.removeJavascriptInterface(MAP_BRIDGE)
            // JS bajarilishiga navbat beramiz — `about:blank` sahifani darrov yiqitsa,
            // `map.remove()` ulgurmay qolishi mumkin.
            webView.post {
                webView.stopLoading()
                webView.loadUrl("about:blank")
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.destroy()
            }
        },
    )
}
