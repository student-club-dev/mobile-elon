package dev.feature.discounts.presentation.form

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_attribute_label
import dev.core.uikit.resources.discounts_attributes_section
import dev.core.uikit.resources.discounts_attributes_subtitle
import dev.core.uikit.resources.discounts_no
import dev.core.uikit.resources.discounts_number_hint
import dev.core.uikit.resources.discounts_yes
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.domain.model.AttributeKind
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import dev.feature.discounts.presentation.categoryAttributes
import dev.feature.discounts.presentation.components.FormSection
import dev.feature.discounts.presentation.components.SelectChip
import org.jetbrains.compose.resources.stringResource

/**
 * 1b. KATEGORIYAGA XOS MAYDONLAR — masalan Game Club'da "PlayStation" tanlansa model/joystik,
 * "Billiard" tanlansa stol turi so'raladi. Kategoriya tanlanmagunча yoki maydon bo'lmasa —
 * bo'lim ko'rinmaydi.
 */
@Composable
fun AttributesSection(state: PostListingUiState, vm: PostListingViewModel) {
    val specs = state.categoryAttributes()
    if (specs.isEmpty()) return
    val palette = appPalette

    FormSection(
        title = stringResource(Res.string.discounts_attributes_section),
        subtitle = stringResource(Res.string.discounts_attributes_subtitle),
        error = state.errorFor(ListingField.ATTRIBUTES),
        palette = palette,
    ) {
        specs.forEach { spec ->
            val value = state.attributeValues[spec.key].orEmpty()
            val suffix = spec.suffix
            Text(
                if (suffix != null) {
                    stringResource(Res.string.discounts_attribute_label, spec.label, suffix)
                } else {
                    spec.label
                },
                style = AppType.fieldLabel.copy(color = palette.label),
            )
            when (spec.kind) {
                AttributeKind.TEXT, AttributeKind.TAGS ->
                    GlassTextField(
                        value,
                        { vm.onAttribute(spec.key, it) },
                        spec.hint.ifBlank { spec.label },
                        height = 46.dp,
                        palette = palette,
                    )

                AttributeKind.NUMBER ->
                    GlassTextField(
                        value, { vm.onAttribute(spec.key, it.filter { c -> c.isDigit() }) },
                        spec.hint.ifBlank { stringResource(Res.string.discounts_number_hint) },
                        height = 46.dp,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        palette = palette,
                    )

                AttributeKind.SELECT ->
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        spec.options.forEach { opt ->
                            SelectChip(text = opt, selected = value == opt, onClick = { vm.onAttribute(spec.key, opt) })
                        }
                    }

                // Bir nechta variant: mavjud razmerlar kabi. Vergul bilan saqlanadi ("S,M,L")
                // va katalogdagi asl tartibda yoziladi.
                AttributeKind.MULTI_SELECT -> {
                    val selected = value.split(",").mapTo(mutableSetOf()) { it.trim() }
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        spec.options.forEach { opt ->
                            val on = opt in selected
                            SelectChip(
                                text = opt,
                                selected = on,
                                onClick = {
                                    val next = if (on) selected - opt else selected + opt
                                    vm.onAttribute(spec.key, spec.options.filter { it in next }.joinToString(","))
                                },
                            )
                        }
                    }
                }

                AttributeKind.BOOLEAN ->
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        SelectChip(
                            stringResource(Res.string.discounts_yes),
                            value == "true",
                            onClick = { vm.onAttribute(spec.key, "true") },
                        )
                        SelectChip(
                            stringResource(Res.string.discounts_no),
                            value == "false",
                            onClick = { vm.onAttribute(spec.key, "false") },
                        )
                    }
            }
        }
    }
}
