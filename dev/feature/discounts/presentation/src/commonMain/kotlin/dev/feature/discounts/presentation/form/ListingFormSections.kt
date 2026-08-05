package dev.feature.discounts.presentation.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_business_section_subtitle
import dev.core.uikit.resources.discounts_category_custom_hint
import dev.core.uikit.resources.discounts_category_section
import dev.core.uikit.resources.discounts_mode_section
import dev.core.uikit.resources.discounts_mode_regular
import dev.core.uikit.resources.discounts_mode_discount
import dev.core.uikit.resources.discounts_mode_subtitle
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
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
import dev.core.uikit.component.AppFieldType

/**
 * E'lon formasining boshlang'ich bloklari: e'lon turi va kategoriya. Narx/rasm/promokod
 * bloklari [ListingFormDetailSections] da.
 *
 * Forma **ataylab qisqa**: e'lon turi, kategoriya, tafsilot, rasm, narx, promokod va telefon.
 * Qolgan hamma narsa (tarkibi, o'lchamlar, shartlar) — erkin **tavsifda**, chunki har bir
 * tafsilot uchun alohida maydon qilinsa forma to'ldirib bo'lmas darajada cho'zilib ketadi.
 */

/**
 * E'LON TURI — Chegirma yoki Oddiy. Tanlov narx blokini boshqaradi: chegirmada eski + yangi
 * narx va promokod ko'rinadi, oddiy e'londa bitta narx (promokod kerak emas).
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
                    height = AppSize.fieldHeight,
                    type = AppFieldType.LatinText,
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
            height = AppSize.fieldHeight,
            type = AppFieldType.LatinText,
        )
    }
}
