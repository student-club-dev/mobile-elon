package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_close
import dev.core.uikit.resources.discounts_branch_name_hint
import dev.core.uikit.resources.discounts_branch_pick_first
import dev.core.uikit.resources.discounts_branch_pick_more
import dev.core.uikit.resources.discounts_branch_remove
import dev.core.uikit.resources.discounts_branches_subtitle
import dev.core.uikit.resources.discounts_branches_title
import dev.core.uikit.resources.discounts_resolving_address
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import org.jetbrains.compose.resources.stringResource

/**
 * Filiallar bo'limi. Manzil qo'lda yozilmaydi: "+" bosiladi, xaritadan joy tanlanadi,
 * manzil teskari geokodlash bilan o'zi to'ladi.
 */
@Composable
fun BranchesSection(state: PostListingUiState, palette: AppPalette, vm: PostListingViewModel) {
    FormSection(
        title = stringResource(Res.string.discounts_branches_title),
        subtitle = stringResource(Res.string.discounts_branches_subtitle),
        error = state.errorFor(ListingField.LOCATION),
        palette = palette,
    ) {
        state.branches.forEachIndexed { index, branch ->
            Column(
                Modifier.fillMaxWidth().clip(AppRadius.sm)
                    .background(palette.accentBg).padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Box(
                        Modifier.size(28.dp).clip(AppRadius.sm)
                            .background(palette.card),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            style = AppType.fieldLabel.copy(
                                fontWeight = AppType.screenTitle.fontWeight,
                                color = palette.primary,
                            ),
                        )
                    }
                    Text(
                        branch.address,
                        style = AppType.fieldLabel.copy(color = palette.ink),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        AppIcons.Close,
                        stringResource(Res.string.discounts_branch_remove),
                        tint = palette.inkFaint,
                        modifier = Modifier.size(AppSize.iconSm).clickable { vm.removeBranch(index) },
                    )
                }

                GlassTextField(
                    branch.name.orEmpty(),
                    { vm.onBranchName(index, it) },
                    stringResource(Res.string.discounts_branch_name_hint),
                    height = AppSize.fieldHeight,
                    palette = palette,
                )
            }
        }

        if (state.resolvingAddress) {
            Text(
                stringResource(Res.string.discounts_resolving_address),
                style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.primary),
            )
        }

        Row(
            Modifier.fillMaxWidth().clip(AppRadius.sm)
                .background(palette.accentBg)
                .clickable(onClick = vm::openMap)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(AppIcons.Plus, null, tint = palette.primary, modifier = Modifier.size(17.dp))
            Spacer(Modifier.size(7.dp))
            Text(
                stringResource(
                    if (state.branches.isEmpty()) Res.string.discounts_branch_pick_first
                    else Res.string.discounts_branch_pick_more,
                ),
                style = AppType.link.copy(
                    fontWeight = AppType.buttonSecondary.fontWeight,
                    color = palette.primary,
                ),
            )
        }
    }
}

/** Bir martalik xabar (rasm xatosi, "Qoralama saqlandi"). */
@Composable
fun MessageBar(message: String, palette: AppPalette, onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(AppRadius.md)
            .background(palette.primary.copy(alpha = 0.10f))
            .clickable(onClick = onDismiss)
            .padding(horizontal = AppSpacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            message,
            style = AppType.fieldLabel.copy(color = palette.primary),
            modifier = Modifier.weight(1f),
        )
        Icon(
            AppIcons.Close,
            stringResource(Res.string.common_close),
            tint = palette.primary,
            modifier = Modifier.size(14.dp),
        )
    }
}
