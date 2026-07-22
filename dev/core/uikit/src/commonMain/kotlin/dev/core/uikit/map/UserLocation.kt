package dev.core.uikit.map

import androidx.compose.runtime.Composable

/**
 * Foydalanuvchining joriy joylashuvi — chegirmagacha bo'lgan masofani ko'rsatish va joy
 * tanlash xaritasini to'g'ri joydan ochish uchun.
 *
 * `null` qaytaradi, agar: ruxsat berilmagan, joylashuv o'chirilgan, yoki hali aniqlanmagan.
 * Bunday holatda e'lonlar masofasiz ko'rsatiladi — ro'yxat baribir ishlaydi.
 *
 * Koordinata **saqlanmaydi** (na bazaga, na log'ga): faqat shu ekran ochiq turganda
 * xotirada bo'ladi.
 */
@Composable
expect fun rememberUserLocation(): MapPoint?
