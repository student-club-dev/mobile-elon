package dev.feature.discounts.presentation.form

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_option_add
import dev.core.uikit.resources.discounts_option_available
import dev.core.uikit.resources.discounts_option_group_add
import dev.core.uikit.resources.discounts_option_group_name_hint
import dev.core.uikit.resources.discounts_option_group_remove
import dev.core.uikit.resources.discounts_option_max_hint
import dev.core.uikit.resources.discounts_option_min_hint
import dev.core.uikit.resources.discounts_option_name_hint
import dev.core.uikit.resources.discounts_option_price_delta_hint
import dev.core.uikit.resources.discounts_option_remove
import dev.core.uikit.resources.discounts_option_required
import dev.core.uikit.resources.discounts_options_section
import dev.core.uikit.resources.discounts_options_subtitle
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.domain.model.ListingValidator
import dev.feature.discounts.domain.model.OptionGroup
import dev.feature.discounts.domain.model.SelectionType
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import dev.feature.discounts.presentation.components.ChipFlow
import dev.feature.discounts.presentation.components.FormSection
import dev.feature.discounts.presentation.components.SelectChip
import org.jetbrains.compose.resources.stringResource

/**
 * 7. QO'SHIMCHALAR (`optionGroups`) — "Hajmni tanlang", "Qo'shimcha pishloq" mantiqidagi
 * guruhlar. Ixtiyoriy: guruh qo'shilmasa so'rovда bo'sh ro'yxat ketadi.
 *
 * Har guruh backenddagi `OptionGroupDto` bilan bir xil maydonlarni yig'adi: nom, tanlov turi,
 * majburiyligi, ko'p tanlovда eng kam/eng ko'p soni va variantlar (nom + narx farqi + mavjudligi).
 * `sortOrder` alohida so'ralmaydi — u ro'yxatdagi tartibdan olinadi.
 */
@Composable
fun OptionGroupsSection(state: PostListingUiState, vm: PostListingViewModel) {
    val palette = appPalette
    FormSection(
        title = stringResource(Res.string.discounts_options_section),
        subtitle = state.businessType?.let { ListingCatalog.optionGroupHint(it) }
            ?: stringResource(Res.string.discounts_options_subtitle),
        error = state.errorFor(ListingField.OPTIONS),
        palette = palette,
    ) {
        state.optionGroups.forEachIndexed { groupIndex, group ->
            OptionGroupCard(groupIndex, group, palette, vm)
        }

        if (state.optionGroups.size < ListingValidator.MAX_OPTION_GROUPS) {
            AddRow(stringResource(Res.string.discounts_option_group_add), palette, vm::addOptionGroup)
        }
    }
}

@Composable
private fun OptionGroupCard(
    index: Int,
    group: OptionGroup,
    palette: AppPalette,
    vm: PostListingViewModel,
) {
    Column(
        Modifier.fillMaxWidth().clip(AppRadius.sm).background(palette.accentBg).padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Box(Modifier.weight(1f)) {
                GlassTextField(
                    group.name,
                    { vm.onGroupName(index, it) },
                    stringResource(Res.string.discounts_option_group_name_hint),
                    height = AppSize.fieldHeight,
                    palette = palette,
                )
            }
            Icon(
                AppIcons.Close,
                stringResource(Res.string.discounts_option_group_remove),
                tint = palette.inkFaint,
                modifier = Modifier.size(AppSize.iconSm).clickable { vm.removeOptionGroup(index) },
            )
        }

        ChipFlow {
            SelectChip(
                SelectionType.SINGLE.label,
                group.selectionType == SelectionType.SINGLE,
                { vm.onGroupSelectionType(index, SelectionType.SINGLE) },
            )
            SelectChip(
                SelectionType.MULTIPLE.label,
                group.selectionType == SelectionType.MULTIPLE,
                { vm.onGroupSelectionType(index, SelectionType.MULTIPLE) },
            )
            SelectChip(
                stringResource(Res.string.discounts_option_required),
                group.isRequired,
                { vm.onGroupRequired(index, !group.isRequired) },
            )
        }

        // Eng kam/eng ko'p faqat ko'p tanlovли guruhда ma'noga ega (`minSelect`/`maxSelect`).
        if (group.selectionType == SelectionType.MULTIPLE) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Box(Modifier.weight(1f)) {
                    GlassTextField(
                        group.minSelect?.toString().orEmpty(),
                        { vm.onGroupMinSelect(index, it) },
                        stringResource(Res.string.discounts_option_min_hint),
                        height = AppSize.fieldHeight,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        palette = palette,
                    )
                }
                Box(Modifier.weight(1f)) {
                    GlassTextField(
                        group.maxSelect?.toString().orEmpty(),
                        { vm.onGroupMaxSelect(index, it) },
                        stringResource(Res.string.discounts_option_max_hint),
                        height = AppSize.fieldHeight,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        palette = palette,
                    )
                }
            }
        }

        group.options.forEachIndexed { optionIndex, option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Box(Modifier.weight(1.4f)) {
                    GlassTextField(
                        option.name,
                        { vm.onOptionName(index, optionIndex, it) },
                        stringResource(Res.string.discounts_option_name_hint),
                        height = 42.dp,
                        palette = palette,
                    )
                }
                Box(Modifier.weight(1f)) {
                    // Narx farqi manfiy ham bo'lishi mumkin ("kichik hajm — arzonroq").
                    GlassTextField(
                        option.priceDelta.takeIf { it != 0L }?.toString().orEmpty(),
                        { vm.onOptionPriceDelta(index, optionIndex, it) },
                        stringResource(Res.string.discounts_option_price_delta_hint),
                        height = 42.dp,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        palette = palette,
                    )
                }
                Icon(
                    AppIcons.Close,
                    stringResource(Res.string.discounts_option_remove),
                    tint = palette.inkFaint,
                    modifier = Modifier.size(AppSize.iconSm)
                        .clickable { vm.removeOption(index, optionIndex) },
                )
            }
            // `isAvailable` — vaqtincha tugagan variantni o'chirmasdan yashirish uchun.
            ChipFlow {
                SelectChip(
                    stringResource(Res.string.discounts_option_available),
                    option.isAvailable,
                    { vm.onOptionAvailable(index, optionIndex, !option.isAvailable) },
                )
            }
        }

        if (group.options.size < ListingValidator.MAX_OPTIONS_PER_GROUP) {
            AddRow(stringResource(Res.string.discounts_option_add), palette) { vm.addOption(index) }
        }
    }
}

/** "+ qo'shish" qatori — guruh va variant uchun bir xil ko'rinish. */
@Composable
private fun AddRow(text: String, palette: AppPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(AppRadius.sm).background(palette.card)
            .clickable(onClick = onClick).padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(AppIcons.Plus, null, tint = palette.primary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.size(6.dp))
        Text(
            text,
            style = AppType.link.copy(fontWeight = AppType.buttonSecondary.fontWeight, color = palette.primary),
        )
    }
}
