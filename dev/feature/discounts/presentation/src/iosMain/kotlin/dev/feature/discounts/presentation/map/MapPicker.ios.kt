package dev.feature.discounts.presentation.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
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
    onCenterChanged: (MapPoint) -> Unit,
    modifier: Modifier,
    centerRequest: MapCenterRequest?,
) {
    val currentOnCenter by rememberUpdatedState(onCenterChanged)

    // Handler WKWebView'dan uzoq yashashi kerak — aks holda JS xabari kelguncha
    // u yig'ib yuboriladi va callback hech qachon chaqirilmaydi.
    val handler = remember { MapMessageHandler() }
    handler.onCenter = { point -> currentOnCenter(point) }

    val lastCenterId = remember { mutableStateOf<Int?>(null) }

    UIKitView(
        modifier = modifier,
        factory = {
            val controller = WKUserContentController()
            controller.addScriptMessageHandler(handler, name = MAP_BRIDGE)

            val config = WKWebViewConfiguration().apply { userContentController = controller }
            val webView = WKWebView(frame = CGRectZero.readValue(), configuration = config)
            webView.opaque = false

            val center = initial ?: DefaultMapCenter
            webView.loadHTMLString(
                string = mapHtml(center),
                baseURL = NSURL(string = TILE_HOST),
            )
            webView
        },
        update = { webView ->
            // Joylashuv keyinroq aniqlansa yoki "Mening joylashuvim" bosilsa — xaritani ko'chiramiz.
            if (centerRequest != null && centerRequest.id != lastCenterId.value) {
                lastCenterId.value = centerRequest.id
                webView.evaluateJavaScript(
                    jsSetCenter(centerRequest.point),
                    completionHandler = null,
                )
            }
        },
    )
}

private class MapMessageHandler : NSObject(), WKScriptMessageHandlerProtocol {

    var onCenter: (MapPoint) -> Unit = {}

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        val body = didReceiveScriptMessage.body as? Map<*, *> ?: return
        if (body["kind"] != "center") return
        val lat = (body["lat"] as? Number)?.toDouble() ?: return
        val lng = (body["lng"] as? Number)?.toDouble() ?: return
        onCenter(MapPoint(lat, lng))
    }
}
