package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.appPalette

/**
 * Ekran foni va standart paddingi.
 *
 * Yangi dizaynda fon — och ko'k-kulrang gradient (yuqori chapdan yumshoq yorug'lik), kontent
 * esa uning ustidagi oq kartalar. Ilgarigi dekorativ rangli "bloblar" olib tashlandi:
 * yangi vizual tilda ular yo'q va oq kartalar ostida faqat iflos dog' bo'lib ko'rinardi.
 */
@Composable
fun AppScreenScaffold(
    modifier: Modifier = Modifier,
    scroll: Boolean = false,
    horizontalPadding: Dp = AppSpacing.screenHorizontal,
    topPadding: Dp = AppSpacing.xl,
    palette: AppPalette = appPalette,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(palette.bgBrush)) {
        val col = Modifier
            .fillMaxSize()
            // Klaviatura ochilganda kontent maydoni qisqaradi. Ilova `enableEdgeToEdge()` bilan
            // ishlaydi — oyna o'zi kichraymaydi, faqat insets yuboradi; busiz pastdagi maydonlar
            // klaviatura ostida ko'rinmay qolardi (`keyboardAware` ham suradigan joy topolmasdi).
            .imePadding()
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
