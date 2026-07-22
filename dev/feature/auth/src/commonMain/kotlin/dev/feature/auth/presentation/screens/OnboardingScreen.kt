package dev.feature.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_onboarding_chip_ai_teacher
import dev.core.uikit.resources.auth_onboarding_chip_order
import dev.core.uikit.resources.auth_onboarding_skip
import dev.core.uikit.resources.auth_onboarding_subtitle
import dev.core.uikit.resources.auth_onboarding_title
import dev.core.uikit.resources.common_next
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.screens.components.Dot
import dev.feature.auth.presentation.screens.components.FloatingChip
import dev.feature.auth.presentation.screens.components.clickableNoRipple
import org.jetbrains.compose.resources.stringResource

/**
 * 1d — Onboarding. Ilovaning bitta jumlalik va'dasi + suzuvchi xizmat chiplari.
 * "O'tkazib yuborish" ham, "Keyingisi" ham bir xil manzilga olib boradi.
 */
@Composable
fun OnboardingScreen(
    onNext: () -> Unit,
    onSkip: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(topPadding = 52.dp) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                stringResource(Res.string.auth_onboarding_skip),
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickableNoRipple(onSkip).padding(AppSpacing.xs),
                style = AppType.link.copy(fontWeight = AppType.label.fontWeight, color = palette.inkFaint),
            )
        }

        // Illyustratsiya + suzuvchi chiplar
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            Box(
                Modifier.align(Alignment.Center).size(180.dp).background(
                    Brush.radialGradient(listOf(palette.primary.copy(alpha = 0.20f), Color.Transparent)),
                    RoundedCornerShape(999.dp),
                ),
            )
            Box(
                Modifier.align(Alignment.Center).size(104.dp)
                    .background(palette.primaryBrush, RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center,
            ) {
                // Gradient USTIDAGI ikonka — palitraga bog'liq emas, har ikkala rejimda oq.
                Icon(AppIcons.GraduationCap, null, tint = palette.onPrimary, modifier = Modifier.size(52.dp))
            }
            FloatingChip(
                "🍕",
                stringResource(Res.string.auth_onboarding_chip_order),
                Modifier.align(Alignment.TopStart).offset(x = 4.dp, y = 40.dp),
                palette,
            )
            FloatingChip(
                "📚",
                stringResource(Res.string.auth_onboarding_chip_ai_teacher),
                Modifier.align(Alignment.CenterEnd).offset(x = (-4).dp, y = 30.dp),
                palette,
            )
            FloatingChip("🏠", null, Modifier.align(Alignment.TopEnd).offset(x = (-30).dp, y = 4.dp), palette)
            FloatingChip("💼", null, Modifier.align(Alignment.BottomStart).offset(x = 20.dp, y = (-8).dp), palette)
        }

        Spacer(Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(Res.string.auth_onboarding_title),
                style = AppType.screenTitle.copy(
                    lineHeight = 28.sp,
                    color = palette.ink,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                stringResource(Res.string.auth_onboarding_subtitle),
                style = AppType.subtitle.copy(color = palette.inkMuted, textAlign = TextAlign.Center),
                modifier = Modifier.padding(horizontal = AppSpacing.sm),
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Dot(false, palette); Dot(true, palette); Dot(false, palette)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton(stringResource(Res.string.common_next), onNext, trailingIcon = AppIcons.ArrowRight)
        }
    }
}
