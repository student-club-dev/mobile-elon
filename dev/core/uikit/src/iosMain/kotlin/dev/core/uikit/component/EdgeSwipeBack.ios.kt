package dev.core.uikit.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Ishorani boshlash mumkin bo'lgan chap chekka kengligi — iOS'dagi kabi tor. */
private val EdgeWidth = 22.dp

/** Shuncha masofa surilsa "orqaga" ishlaydi. */
private val TriggerDistance = 72.dp

/** Barmoq ortidan ekran ko'pi bilan shuncha suriladi — javob berayotgani bilinsin. */
private val MaxFollow = 56.dp

/** Barmoq harakatining shuncha qismi ekranga uzatiladi (to'liq emas — qarshilik hissi). */
private const val FollowRatio = 0.35f

@Composable
actual fun EdgeSwipeBack(enabled: Boolean, onBack: () -> Unit, content: @Composable () -> Unit) {
    val latestBack by rememberUpdatedState(onBack)
    val scope = rememberCoroutineScope()
    val shift = remember { Animatable(0f) }

    Box(Modifier.fillMaxSize()) {
        // Butun ekran barmoq ortidan biroz suriladi — ishora qabul qilinganini ko'rsatadi.
        Box(Modifier.fillMaxSize().graphicsLayer { translationX = shift.value }) { content() }

        if (enabled) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(EdgeWidth)
                    // Faqat shu tor chekka ushlaydi — ichkaridagi ro'yxat va xaritalar
                    // o'z ishoralarini erkin oladi.
                    .pointerInput(Unit) {
                        val triggerPx = TriggerDistance.toPx()
                        val maxPx = MaxFollow.toPx()
                        var dragged = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragged = 0f },
                            onDragCancel = {
                                scope.launch { shift.animateTo(0f, tween(160)) }
                            },
                            onDragEnd = {
                                val fire = dragged >= triggerPx
                                scope.launch { shift.animateTo(0f, tween(160)) }
                                // Avval ekran ichidagi ustki qatlam yopiladi; bo'lmasa —
                                // bir ekran ortga.
                                if (fire && !IosBackDispatcher.dispatch()) latestBack()
                            },
                            onHorizontalDrag = { _, delta ->
                                dragged = (dragged + delta).coerceAtLeast(0f)
                                scope.launch { shift.snapTo((dragged * FollowRatio).coerceAtMost(maxPx)) }
                            },
                        )
                    },
            )
        }
    }
}
