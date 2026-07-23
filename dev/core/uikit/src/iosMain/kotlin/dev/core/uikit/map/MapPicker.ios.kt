package dev.core.uikit.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.map_drag_hint
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import org.jetbrains.compose.resources.stringResource
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapPicker(
    initial: MapPoint?,
    dark: Boolean,
    onCenterChanged: (MapPoint) -> Unit,
    modifier: Modifier,
    centerRequest: MapCenterRequest?,
    onStatus: (MapStatus) -> Unit,
) {
    val currentOnCenter by rememberUpdatedState(onCenterChanged)
    val currentOnStatus by rememberUpdatedState(onStatus)

    val hint = stringResource(Res.string.map_drag_hint)

    // MapLibre ilova ichidan o'qiladi (CDN emas) — tayyor bo'lmaguncha WebView qurilmaydi.
    if (!rememberMapLibreReady()) return

    // Boshlang'ich markaz bir marta olinadi; keyingi ko'chishlar JS orqali (reload YO'Q).
    val initialCenter = remember { initial ?: DefaultMapCenter }
    val html = remember(dark, hint) { pickerMapHtml(initialCenter, dark, hint) }

    // Handler WKWebView'dan uzoq yashashi kerak — aks holda JS xabari kelguncha
    // u yig'ib yuboriladi va callback hech qachon chaqirilmaydi.
    val handler = remember { MapMessageHandler() }
    // Yon ta'sirni kompozitsiya tanasida emas, SideEffect'da o'rnatamiz (bekor qilingan
    // kompozitsiyada eski lambda o'rnatilib qolmasin).
    SideEffect {
        handler.onCenter = { point -> currentOnCenter(point) }
        handler.onStatus = { status -> currentOnStatus(status) }
    }

    val htmlHolder = remember { Holder("") }
    val lastCenterId = remember { Holder<Int?>(null) }

    UIKitView(
        modifier = modifier,
        factory = {
            val controller = WKUserContentController()
            controller.addScriptMessageHandler(handler, name = MAP_BRIDGE)

            val config = WKWebViewConfiguration().apply { userContentController = controller }
            val webView = WKWebView(frame = CGRectZero.readValue(), configuration = config)
            webView.opaque = false
            webView.loadHTMLString(html, baseURL = NSURL(string = MAP_BASE_URL))
            htmlHolder.value = html
            webView
        },
        update = { webView ->
            // Mavzu (yoki til) o'zgardi — sahifani qayta qurish shart.
            if (htmlHolder.value != html) {
                htmlHolder.value = html
                webView.loadHTMLString(html, baseURL = NSURL(string = MAP_BASE_URL))
                return@UIKitView
            }
            // Joylashuv keyinroq aniqlansa yoki "Mening joylashuvim" bosilsa — xaritani ko'chiramiz.
            if (centerRequest != null && centerRequest.id != lastCenterId.value) {
                lastCenterId.value = centerRequest.id
                webView.evaluateJavaScript(jsSetCenter(centerRequest.point), completionHandler = null)
            }
        },
        // Ekran yopilganda handler'ni yechamiz — aks holda WKUserContentController handler'ni
        // KUCHLI ushlaydi, handler esa Composition'ni; natijada WKWebView va uning alohida
        // WebContent jarayoni ekran yopilgandan keyin ham yashab, xotira o'sib boradi.
        onRelease = { webView ->
            // AVVAL `map.remove()` — WebGL kontekstini va animatsiya siklini bo'shatadi
            // (qarang `jsDestroyMap` izohi), keyin handler yechiladi.
            webView.evaluateJavaScript(jsDestroyMap(), completionHandler = null)
            webView.configuration.userContentController
                .removeScriptMessageHandlerForName(MAP_BRIDGE)
            webView.stopLoading()
            webView.loadHTMLString("", baseURL = null)
        },
    )
}

private class MapMessageHandler : NSObject(), WKScriptMessageHandlerProtocol {

    var onCenter: (MapPoint) -> Unit = {}
    var onStatus: (MapStatus) -> Unit = {}

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        val body = didReceiveScriptMessage.body as? Map<*, *> ?: return
        when (body["kind"]) {
            "ready" -> return onStatus(MapStatus.READY)
            "error" -> return onStatus(MapStatus.FAILED)
            "center" -> Unit
            else -> return
        }
        val lat = (body["lat"] as? Number)?.toDouble() ?: return
        val lng = (body["lng"] as? Number)?.toDouble() ?: return
        onCenter(MapPoint(lat, lng))
    }
}
