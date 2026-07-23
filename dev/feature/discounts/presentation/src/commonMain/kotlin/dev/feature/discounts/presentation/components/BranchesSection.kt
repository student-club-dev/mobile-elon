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
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_close
import dev.core.uikit.resources.discounts_branches_add_note
import dev.core.uikit.resources.discounts_branches_empty
import dev.core.uikit.resources.discounts_branches_pick_subtitle
import dev.core.uikit.resources.discounts_branches_title
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import org.jetbrains.compose.resources.stringResource

/**
 * Filiallar bo'limi — e'lon **qaysi filiallarda** amal qilishini belgilaydi (`branchIds`).
 *
 * Bu yerда yangi filial ochilmaydi: backendда filial alohida resurs
 * (`POST /business/{id}/branches`) va tuman, ish vaqti kabi maydonlarni talab qiladi.
 * Shuning uchun filial biznes profilida yaratiladi, e'lon esa faqat mavjudlaridan tanlaydi —
 * aks holda serverda yo'q id yuborilib, e'lon rad etilardi.
 */
@Composable
fun BranchesSection(state: PostListingUiState, palette: AppPalette, vm: PostListingViewModel) {
    FormSection(
        title = stringResource(Res.string.discounts_branches_title),
        subtitle = stringResource(Res.string.discounts_branches_pick_subtitle),
        error = state.errorFor(ListingField.LOCATION),
        palette = palette,
    ) {
        if (state.branches.isEmpty()) {
            Text(
                stringResource(Res.string.discounts_branches_empty),
                style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.inkMuted),
            )
            return@FormSection
        }

        state.branches.forEach { branch ->
            val selected = branch.id in state.selectedBranchIds
            Row(
                Modifier.fillMaxWidth().clip(AppRadius.sm)
                    .background(if (selected) palette.accentBg else palette.card)
                    .clickable { vm.toggleBranch(branch.id) }
                    .padding(11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                // Belgi — tanlanganда brend rangli doira, aks holда bo'sh kontur o'rniga
                // sust yuza (dizaynда checkbox yo'q, chiplar bilan bir xil til).
                Box(
                    Modifier.size(22.dp).clip(AppRadius.pill)
                        .background(if (selected) palette.primary else palette.fieldBg),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(AppIcons.Check, null, tint = palette.onPrimary, modifier = Modifier.size(13.dp))
                    }
                }
                Column(Modifier.weight(1f)) {
                    val name = branch.name?.takeIf { it.isNotBlank() }
                    if (name != null) {
                        Text(name, style = AppType.fieldLabel.copy(color = palette.ink), maxLines = 1)
                    }
                    Text(
                        branch.address,
                        style = AppType.caption.copy(color = palette.inkMuted),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Text(
            stringResource(Res.string.discounts_branches_add_note),
            style = AppType.caption.copy(color = palette.inkFaint),
        )
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
