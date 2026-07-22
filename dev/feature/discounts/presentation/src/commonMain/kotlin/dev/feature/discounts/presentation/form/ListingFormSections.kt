package dev.feature.discounts.presentation.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_business_section_subtitle
import dev.core.uikit.resources.discounts_category_custom_hint
import dev.core.uikit.resources.discounts_category_section
import dev.core.uikit.resources.discounts_gender_female
import dev.core.uikit.resources.discounts_gender_male
import dev.core.uikit.resources.discounts_gender_section
import dev.core.uikit.resources.discounts_gender_subtitle
import dev.core.uikit.resources.discounts_mode_discount
import dev.core.uikit.resources.discounts_mode_regular
import dev.core.uikit.resources.discounts_mode_section
import dev.core.uikit.resources.discounts_mode_subtitle
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import dev.feature.discounts.presentation.categories
import dev.feature.discounts.presentation.components.FormSection
import dev.feature.discounts.presentation.components.SectionHPad
import dev.feature.discounts.presentation.components.SectionHeader
import dev.feature.discounts.presentation.components.SelectChip
import org.jetbrains.compose.resources.stringResource

/**
 * E'lon formasining boshlang'ich bloklari: e'lon turi, jins darvozasi, kategoriya,
 * biznes nomi. Kategoriyaga xos maydonlar [ListingFormAttributes] da, narx/rasm/muddat
 * bloklari [ListingFormDetailSections] da.
 *
 * Forma **ataylab qisqa**: biznes nomi, chegirma nimaga amal qilishi, rasm, narx va chegirma,
 * muddat, filial. Qolgan hamma narsa (tarkibi, o'lchamlar, shartlar) — erkin **tavsifda**,
 * chunki har bir tafsilot uchun alohida maydon qilinsa forma to'ldirib bo'lmas darajada
 * cho'zilib ketadi.
 */

/**
 * 00. E'LON TURI — Chegirma yoki Oddiy. Tanlovga qarab narx bo'limidagi chegirma maydonlari
 * ko'rinadi yoki yashiriladi.
 */
@Composable
fun ListingModeSection(state: PostListingUiState, vm: PostListingViewModel) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Column(Modifier.padding(horizontal = SectionHPad)) {
            SectionHeader(
                stringResource(Res.string.discounts_mode_section),
                stringResource(Res.string.discounts_mode_subtitle),
            )
        }
        LazyRow(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = SectionHPad),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            item {
                SelectChip(
                    stringResource(Res.string.discounts_mode_discount),
                    state.isDiscount,
                    { vm.onListingMode(true) },
                )
            }
            item {
                SelectChip(
                    stringResource(Res.string.discounts_mode_regular),
                    !state.isDiscount,
                    { vm.onListingMode(false) },
                )
            }
        }
    }
}

/**
 * Kiyim-kechak e'lonида **birinchi qadam** — erkak yoki ayol kiyimi. Tanlangач qolgan forma
 * (kategoriyalar shu jinsга moslangan holda) ochiladi.
 */
@Composable
fun ClothingGenderSection(state: PostListingUiState, palette: AppPalette, vm: PostListingViewModel) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.padding(horizontal = SectionHPad)) {
            SectionHeader(
                stringResource(Res.string.discounts_gender_section),
                stringResource(Res.string.discounts_gender_subtitle),
                palette,
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = SectionHPad),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GenderCard(
                "👨",
                stringResource(Res.string.discounts_gender_male),
                state.listingGender == Gender.MALE,
                { vm.onListingGender(Gender.MALE) },
                Modifier.weight(1f),
                palette,
            )
            GenderCard(
                "👩",
                stringResource(Res.string.discounts_gender_female),
                state.listingGender == Gender.FEMALE,
                { vm.onListingGender(Gender.FEMALE) },
                Modifier.weight(1f),
                palette,
            )
        }
    }
}

@Composable
private fun GenderCard(
    emoji: String,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    palette: AppPalette,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (active) palette.primary else palette.glass)
            .then(if (active) Modifier else Modifier.border(1.dp, palette.border, shape))
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(emoji, style = TextStyle(fontSize = 30.sp))
        Text(
            label,
            style = AppType.body.copy(
                fontWeight = AppType.label.fontWeight,
                // Brend fon USTIDA — kontent rangi `onPrimary` dan olinadi.
                color = if (active) palette.onPrimary else palette.ink,
            ),
        )
    }
}

/** Kategoriya (bo'lim) — horizontal scroll bilan, ekran chetigacha suriladi. */
@Composable
fun CategorySection(
    state: PostListingUiState,
    copy: ListingFormCopy,
    vm: PostListingViewModel,
) {
    val palette = appPalette
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Column(Modifier.padding(horizontal = SectionHPad)) {
            SectionHeader(
                stringResource(Res.string.discounts_category_section),
                stringResource(copy.categoryHint),
                palette,
            )
            state.errorFor(ListingField.CATEGORY)?.let {
                Text(it, style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.primary))
            }
        }
        // Edge-to-edge horizontal scroll — chiplar ekran chetigacha suriladi.
        LazyRow(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = SectionHPad),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            items(state.categories()) { category ->
                SelectChip(
                    text = category.nameUz,
                    selected = state.categoryKey == category.key,
                    onClick = { vm.onCategory(category.key) },
                )
            }
        }

        if (state.categoryKey == ListingCatalog.OTHER_KEY) {
            Box(Modifier.padding(horizontal = SectionHPad)) {
                GlassTextField(
                    state.customCategoryName,
                    vm::onCustomCategory,
                    stringResource(Res.string.discounts_category_custom_hint),
                    height = 46.dp,
                    palette = palette,
                )
            }
        }
    }
}

/** 1. Biznes nomi. */
@Composable
fun BusinessAndScopeSection(
    state: PostListingUiState,
    copy: ListingFormCopy,
    vm: PostListingViewModel,
) {
    FormSection(
        title = stringResource(copy.businessSection),
        subtitle = stringResource(Res.string.discounts_business_section_subtitle),
        error = state.errorFor(ListingField.BUSINESS_NAME),
    ) {
        GlassTextField(
            state.businessName,
            vm::onBusinessName,
            stringResource(copy.businessHint),
            height = 48.dp,
        )
    }
}
