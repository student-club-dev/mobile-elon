package dev.feature.discounts.presentation.map

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun MapPicker(
    initial: MapPoint?,
    onCenterChanged: (MapPoint) -> Unit,
    modifier: Modifier,
    centerRequest: MapCenterRequest?,
) {
    // WebView faqat bir marta quriladi, `onPick` esa har rekompozitsiyada yangilanishi mumkin —
    // ko'prik eng oxirgi callback'ni chaqirishi uchun uni shu yerda ushlab turamiz.
    val currentOnCenter by rememberUpdatedState(onCenterChanged)

    // Bir xil so'rovni ikki marta bajarmaslik uchun oxirgi bajarilgan id saqlanadi.
    val lastCenterId = remember { mutableStateOf<Int?>(null) }

    // Sahifa yuklanmasidan turib `setCenter(...)` ni chaqirsak, u hali mavjud emas va so'rov
    // yo'qoladi (xarita boshlang'ich markazida qolib ketadi). Shuning uchun tayyor bo'lgunicha
    // saqlab turamiz va `onPageFinished` da bajaramiz.
    val pageReady = remember { mutableStateOf(false) }
    val pending = remember { mutableStateOf<MapCenterRequest?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(0)

                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onConsoleMessage(m: android.webkit.ConsoleMessage): Boolean {
                        android.util.Log.i("MapPicker", "JS: ${m.message()} @${m.sourceId()}:${m.lineNumber()}")
                        return true
                    }
                }

                webViewClient = object : android.webkit.WebViewClient() {
                    override fun onReceivedError(
                        view: WebView,
                        request: android.webkit.WebResourceRequest,
                        error: android.webkit.WebResourceError,
                    ) {
                        android.util.Log.e("MapPicker", "XATO ${error.errorCode} ${error.description} <- ${request.url}")
                    }

                    /**
                     * Xarita ochilmasa birinchi tekshiriladigan joy: konteyner o'lchami va
                     * plitkalar soni. (Aynan shu yerdan `map = 360x0` topilgan edi — CSS
                     * `height: 100%` bu WebView'da 0 bergan, plitkalar kesilib ko'rinmagan.)
                     */
                    override fun onPageFinished(view: WebView, url: String) {
                        pageReady.value = true

                        // Sahifa yuklanguncha kelgan "bu yerga ko'ch" so'rovini endi bajaramiz.
                        pending.value?.let { request ->
                            pending.value = null
                            view.evaluateJavascript(jsSetCenter(request.point), null)
                        }

                        view.evaluateJavascript(
                            "(function(){var i=document.querySelector('img');return " +
                                "document.getElementById('map').clientWidth + 'x' + document.getElementById('map').clientHeight" +
                                " + ' z=' + z + ' plitka=' + document.querySelectorAll('img').length" +
                                " + ' img=' + (i ? i.width + 'x' + i.height + ' natural=' + i.naturalWidth : '-')})()",
                        ) { android.util.Log.i("MapPicker", "xarita: $it") }
                    }
                }

                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onEvent(kind: String, lat: Double, lng: Double) {
                            if (kind != "center") return
                            // JS oqimidan keladi — Compose holatiga faqat asosiy oqimdan tegamiz.
                            post { currentOnCenter(MapPoint(lat, lng)) }
                        }
                    },
                    MAP_BRIDGE,
                )

                val center = initial ?: DefaultMapCenter
                // Baza manzili plitka serveri — shunda plitkalar sahifa bilan bir origin'da
                // bo'ladi va WebView ularni to'smaydi.
                loadDataWithBaseURL(
                    TILE_HOST,
                    mapHtml(center),
                    "text/html",
                    "utf-8",
                    null,
                )
            }
        },
        update = { webView ->
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
    )
}
