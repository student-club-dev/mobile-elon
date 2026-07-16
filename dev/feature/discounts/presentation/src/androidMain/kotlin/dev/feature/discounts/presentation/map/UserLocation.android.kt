package dev.feature.discounts.presentation.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberUserLocation(): MapPoint? {
    val context = LocalContext.current
    var location by remember { mutableStateOf<MapPoint?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        // Foydalanuvchi rad etsa — masofasiz davom etamiz, ekran baribir ishlaydi.
        if (granted.values.any { it }) location = context.lastKnownPoint()
    }

    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            location = context.lastKnownPoint()
        } else {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ),
            )
        }
    }

    return location
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

/**
 * Oxirgi ma'lum joylashuv. Google Play Services (FusedLocation) ishlatilmaydi — bu
 * qo'shimcha bog'liqlik va Play Services yo'q qurilmalarda ishlamaydi. Masofani "1.2 km"
 * aniqligida ko'rsatish uchun tarmoq/GPS provayderining oxirgi nuqtasi yetarli.
 */
private fun Context.lastKnownPoint(): MapPoint? {
    if (!hasLocationPermission()) return null
    val manager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

    return try {
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { provider ->
                if (manager.isProviderEnabled(provider)) manager.getLastKnownLocation(provider) else null
            }
            .maxByOrNull { it.time }
            ?.let { MapPoint(it.latitude, it.longitude) }
    } catch (e: SecurityException) {
        null
    }
}
