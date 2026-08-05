package dev.core.uikit.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fokusdagi maydonni klaviatura ustiga suradi.
 *
 * Nega kerak: ilova `enableEdgeToEdge()` bilan ishlaydi, ya'ni oyna klaviatura uchun o'zi
 * kichraymaydi — u faqat insets yuboradi. Shu sabab ikkita qism kerak:
 * 1. ekran ildizi `imePadding()` bilan qisqaradi (`AppScreenScaffold`, `AppBottomSheet`),
 * 2. maydonning o'zi scroll ichida ko'rinadigan joyga suriladi — shu modifikator.
 *
 * Suriladigan to'rtburchak maydondan [extraSpace] ga baland olinadi: aks holda maydon aynan
 * klaviatura labiga tegib turardi va ostidagi xato matni ko'rinmasdi.
 *
 * [LaunchedEffect] `imeBottom` ga ham bog'langan — klaviatura ochilish animatsiyasi davomida
 * ko'rinadigan balandlik o'zgaraveradi, so'rov esa har safar yangilanib oxirgi holatда
 * to'g'ri joyга tushadi.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.keyboardAware(extraSpace: Dp = 24.dp): Modifier = composed {
    val requester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSizePx(0, 0)) }
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)

    LaunchedEffect(focused, imeBottom, size) {
        if (focused && size.height > 0) {
            requester.bringIntoView(
                Rect(
                    left = 0f,
                    top = 0f,
                    right = size.width.toFloat(),
                    bottom = size.height + with(density) { extraSpace.toPx() },
                ),
            )
        }
    }

    this
        .bringIntoViewRequester(requester)
        .onSizeChanged { size = IntSizePx(it.width, it.height) }
        // `hasFocus` (`isFocused` emas) — modifikator odatda maydonni O'RAB turgan qatorga
        // qo'yiladi, fokus esa uning ichidagi `BasicTextField` da bo'ladi.
        .onFocusChanged { focused = it.hasFocus }
}

/** Maydon o'lchami (px) — [LaunchedEffect] kaliti sifatida taqqoslash uchun bitta qiymat. */
private data class IntSizePx(val width: Int, val height: Int)
