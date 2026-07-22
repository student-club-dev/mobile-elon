package dev.core.uikit.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette

/**
 * Modal Bottom Sheet — tanlov uchun asosiy naqsh (`design_handoff_studentclub_elonuz`).
 *
 * Biznes turi va viloyat kabi tanlovlar ekran ostidan sirg'alib chiqadigan oynada beriladi,
 * ilgarigi ochilib-yopiladigan ro'yxat o'rniga: ro'yxat kontentni surib yubormaydi va
 * uzun variantlar to'liq ekranda ko'rinadi.
 *
 * Animatsiya handoff bo'yicha: qoraytiruvchi qatlam 250 ms fade, oyna 300 ms pastdan yuqoriga.
 *
 * MUHIM: bu komponent o'zi to'liq ekranni egallaydi, shuning uchun uni ekran ildizidagi
 * `Box` ichida, kontentdan KEYIN chaqirish kerak — aks holda u kontent ostida qolib ketadi.
 */
@Composable
fun AppBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    palette: AppPalette = appPalette,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(200)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(palette.scrim)
                // Tashqariga bosilganda yopiladi. `indication = null` — qoraytiruvchi
                // qatlamda to'lqin effekti bo'lmasligi kerak.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(tween(300)) { it },
                exit = slideOutVertically(tween(250)) { it },
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f)
                        .clip(AppRadius.sheetTop)
                        .background(palette.card)
                        // Oyna ichidagi bosish tashqariga o'tmasin (aks holda yopilib ketadi).
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                        .padding(horizontal = AppSpacing.screenHorizontal)
                        .padding(top = 10.dp, bottom = AppSpacing.screenBottom),
                ) {
                    // Tortish chizig'i — oyna sudraladigan ekanini bildiradi.
                    Box(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(5.dp)
                            .clip(AppRadius.pill)
                            .background(palette.divider),
                    )
                    Spacer(Modifier.height(AppSpacing.lg))
                    Text(title, style = AppType.sheetTitle.copy(color = palette.ink))
                    Spacer(Modifier.height(14.dp))
                    Column(
                        Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        content = content,
                    )
                }
            }
        }
    }
}

/**
 * Sheet ichidagi tanlov qatori — chapda ikonka, o'ngda tanlangani ko'k doiradagi belgi.
 */
@Composable
fun BottomSheetOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    palette: AppPalette = appPalette,
) {
    androidx.compose.foundation.layout.Row(
        modifier
            .fillMaxWidth()
            .clip(AppRadius.md)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        if (icon != null) {
            Icon(icon, null, tint = palette.primary, modifier = Modifier.size(20.dp))
        }
        Text(
            label,
            style = AppType.body.copy(fontWeight = AppType.rowTitle.fontWeight, color = palette.ink),
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Box(
                Modifier.size(20.dp).clip(AppRadius.pill).background(palette.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    AppIcons.Check,
                    null,
                    tint = palette.onPrimary,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}
