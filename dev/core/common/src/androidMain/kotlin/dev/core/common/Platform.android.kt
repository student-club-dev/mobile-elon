package dev.core.common

actual val platformName: String = "Android"

actual val deviceName: String = listOf(android.os.Build.MANUFACTURER, android.os.Build.MODEL)
    .filter { it.isNotBlank() }
    .joinToString(" ")
    .ifBlank { "Android" }
