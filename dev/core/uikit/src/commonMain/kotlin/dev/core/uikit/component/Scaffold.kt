package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.appPalette

/**
 * Ekran foni — gradient + ikkita dekorativ blob + standart padding.
 *
 * [scroll] `true` bo'lsa kontent vertikal aylanadi. Padding qiymatlari `Dp` sifatida
 * beriladi (ilgari `Int` edi — birlik noaniq qolardi).
 */
@Composable
fun AppScreenScaffold(
    modifier: Modifier = Modifier,
    scroll: Boolean = false,
    horizontalPadding: Dp = AppSpacing.screenHorizontal,
    topPadding: Dp = AppSpacing.screenTop,
    palette: AppPalette = appPalette,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(palette.bgBrush)) {
        DecorBlob(Alignment.TopEnd, palette.blobPrimary, size = 210.dp, offsetX = 60f, offsetY = -80f)
        DecorBlob(Alignment.BottomStart, palette.blobCyan, size = 200.dp, offsetX = -70f, offsetY = 40f)

        val col = Modifier
            .fillMaxSize()
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = topPadding,
                bottom = AppSpacing.screenBottom,
            )
            .then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)
        Column(modifier = col, content = content)
    }
}

/** Fon burchagidagi yumshoq rangli dog' — sof dekorativ, tegishga javob bermaydi. */
@Composable
private fun BoxScope.DecorBlob(
    alignment: Alignment,
    color: Color,
    size: Dp,
    offsetX: Float,
    offsetY: Float,
) {
    Box(
        Modifier
            .align(alignment)
            .size(size)
            .graphicsLayer { translationX = offsetX; translationY = offsetY }
            .background(Brush.radialGradient(listOf(color, Color.Transparent)), AppRadius.pill),
    )
}
