package dev.core.uikit.component

import androidx.compose.runtime.Composable

/** Android'da tizim "orqaga" tugmasi/ishorasi — `activity-compose` beradigan handler. */
@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}
