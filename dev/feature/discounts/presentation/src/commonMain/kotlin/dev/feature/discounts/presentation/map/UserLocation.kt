package dev.feature.discounts.presentation.map

import androidx.compose.runtime.Composable

/**
 * Talabaning joriy joylashuvi — chegirmagacha bo'lgan masofani ko'rsatish uchun.
 *
 * `null` qaytaradi, agar: ruxsat berilmagan, joylashuv o'chirilgan, yoki hali aniqlanmagan.
 * Bunday holatda e'lonlar masofasiz ko'rsatiladi — ro'yxat baribir ishlaydi.
 *
 * Koordinata **saqlanmaydi** (na bazaga, na log'ga): faqat shu ekran ochiq turganda
 * xotirada bo'ladi va masofa hisoblashga ishlatiladi.
 */
@Composable
expect fun rememberUserLocation(): MapPoint?
