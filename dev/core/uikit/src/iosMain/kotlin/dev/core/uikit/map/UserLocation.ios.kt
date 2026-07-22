package dev.core.uikit.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberUserLocation(): MapPoint? {
    var location by remember { mutableStateOf<MapPoint?>(null) }

    // Delegate va manager Compose qayta chizilishlari orasida saqlanishi kerak — aks holda
    // joylashuv kelguncha ular yig'ib yuboriladi va callback hech qachon chaqirilmaydi.
    val delegate = remember { LocationDelegate() }
    val manager = remember { CLLocationManager().apply { this.delegate = delegate } }

    DisposableEffect(Unit) {
        delegate.onLocation = { point -> location = point }

        // Info.plist da NSLocationWhenInUseUsageDescription bo'lishi shart.
        manager.requestWhenInUseAuthorization()
        manager.requestLocation()

        onDispose {
            delegate.onLocation = {}
            manager.delegate = null
        }
    }

    return location
}

@OptIn(ExperimentalForeignApi::class)
private class LocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {

    var onLocation: (MapPoint) -> Unit = {}

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return
        location.coordinate.useContents {
            onLocation(MapPoint(lat = latitude, lng = longitude))
        }
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: platform.Foundation.NSError) {
        // Ruxsat berilmadi yoki joylashuv o'chirilgan — masofasiz davom etamiz.
    }
}
