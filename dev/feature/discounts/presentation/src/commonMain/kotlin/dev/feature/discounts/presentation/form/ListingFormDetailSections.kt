package dev.feature.discounts.presentation.form

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_about_subtitle
import dev.core.uikit.resources.discounts_contact_hint
import dev.core.uikit.resources.discounts_contact_subtitle
import dev.core.uikit.resources.discounts_days
import dev.core.uikit.resources.discounts_images_subtitle
import dev.core.uikit.resources.discounts_percent_off
import dev.core.uikit.resources.discounts_phone_label
import dev.core.uikit.resources.discounts_price_label
import dev.core.uikit.resources.discounts_price_new_hint
import dev.core.uikit.resources.discounts_price_new_label
import dev.core.uikit.resources.discounts_price_old_hint
import dev.core.uikit.resources.discounts_price_old_label
import dev.core.uikit.resources.discounts_price_section
import dev.core.uikit.resources.discounts_price_subtitle_discount
import dev.core.uikit.resources.discounts_price_subtitle_regular
import dev.core.uikit.resources.discounts_promo_hint
import dev.core.uikit.resources.discounts_redemption_section
import dev.core.uikit.resources.discounts_student_pays
import dev.core.uikit.resources.discounts_sum_suffix
import dev.core.uikit.resources.discounts_validity_section
import dev.core.uikit.resources.discounts_validity_subtitle
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.domain.model.ListingValidator
import dev.feature.discounts.domain.model.RedemptionMethod
import dev.feature.discounts.domain.model.formatSum
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import dev.feature.discounts.presentation.components.AddImageTile
import dev.feature.discounts.presentation.components.ChipFlow
import dev.feature.discounts.presentation.components.FormSection
import dev.feature.discounts.presentation.components.ImageThumb
import dev.feature.discounts.presentation.components.SelectChip
import org.jetbrains.compose.resources.stringResource

/**
 * E'lon formasining ikkinchi yarmi: rasm, tavsif, narx, aloqa, olish usuli va muddat.
 * Boshlang'ich bloklar (tur, kategoriya, tafsilotlar) — [ListingFormSections] da.
 */

/** 2. Rasmlar. */
@Composable
fun ImagesSection(
    state: PostListingUiState,
    copy: ListingFormCopy,
    vm: PostListingViewModel,
    onAdd: () -> Unit,
) {
    FormSection(
        title = stringResource(copy.imagesSection),
        subtitle = stringResource(
            Res.string.discounts_images_subtitle,
            stringResource(copy.imagesHint),
            "${ListingValidator.MAX_IMAGES}",
        ),
        error = state.errorFor(ListingField.IMAGES),
    ) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            state.images.forEachIndexed { index, source ->
                ImageThumb(source, onRemove = { vm.removeImage(index) })
            }
            if (state.images.size < ListingValidator.MAX_IMAGES) {
                AddImageTile(onClick = onAdd, loading = state.uploadingImage)
            }
        }
    }
}

/**
 * 3. Sarlavha va tavsif. Tavsif — **hamma qolgan tafsilot uchun**: tarkibi, o'lchamlar,
 * ish vaqti, qo'shimcha shartlar. Shu sabab alohida o'nlab maydon kerak emas.
 */
@Composable
fun AboutSection(
    state: PostListingUiState,
    copy: ListingFormCopy,
    vm: PostListingViewModel,
) {
    FormSection(
        title = stringResource(copy.aboutSection),
        subtitle = stringResource(Res.string.discounts_about_subtitle),
        error = state.errorFor(ListingField.TITLE),
    ) {
        GlassTextField(state.title, vm::onTitle, stringResource(copy.titleHint), height = 48.dp)
        GlassTextField(state.description, vm::onDescription, stringResource(copy.descriptionHint), height = 110.dp)
    }
}

/** 4. Narx — chegirmada Oldingi + Hozirgi narx (foiz/turlar yo'q); oddiyда bitta narx. */
@Composable
fun PriceAndDiscountSection(
    state: PostListingUiState,
    copy: ListingFormCopy,
    vm: PostListingViewModel,
) {
    val palette = appPalette

    FormSection(
        title = stringResource(Res.string.discounts_price_section),
        subtitle = stringResource(
            if (state.isDiscount) Res.string.discounts_price_subtitle_discount
            else Res.string.discounts_price_subtitle_regular,
        ),
        error = state.errorFor(ListingField.PRICE) ?: state.errorFor(ListingField.DISCOUNT),
        palette = palette,
    ) {
        MiniLabel(
            stringResource(
                if (state.isDiscount) Res.string.discounts_price_old_label else Res.string.discounts_price_label,
            ),
            palette,
        )
        GlassTextField(
            state.originalPrice, vm::onPrice, stringResource(Res.string.discounts_price_old_hint),
            height = 48.dp,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailing = { Suffix(stringResource(Res.string.discounts_sum_suffix), palette) },
            palette = palette,
        )

        if (state.isDiscount) {
            MiniLabel(stringResource(Res.string.discounts_price_new_label), palette)
            GlassTextField(
                state.discountValue, vm::onDiscountValue, stringResource(Res.string.discounts_price_new_hint),
                height = 48.dp,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailing = { Suffix(stringResource(Res.string.discounts_sum_suffix), palette) },
                palette = palette,
            )

            val old = state.originalPrice.toLongOrNull() ?: 0
            val new = state.discountValue.toLongOrNull() ?: 0
            if (old > 0 && new in 1 until old) {
                val percent = (old - new) * 100 / old
                Row(
                    Modifier.fillMaxWidth().clip(AppRadius.md)
                        .background(palette.successBg).padding(horizontal = AppSpacing.md, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text(
                        stringResource(Res.string.discounts_student_pays, new.formatSum()),
                        style = AppType.label.copy(
                            fontWeight = AppType.screenTitle.fontWeight,
                            color = palette.successDeep,
                        ),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(Res.string.discounts_percent_off, "$percent"),
                        style = AppType.fieldLabel.copy(
                            fontWeight = AppType.buttonSecondary.fontWeight,
                            color = palette.successDeep,
                        ),
                    )
                }
            }
        }
    }
}

/** Aloqa — telefon raqami. */
@Composable
fun ContactSection(state: PostListingUiState, vm: PostListingViewModel) {
    FormSection(
        title = stringResource(Res.string.discounts_phone_label),
        subtitle = stringResource(Res.string.discounts_contact_subtitle),
    ) {
        GlassTextField(
            state.contactPhone, vm::onContactPhone, stringResource(Res.string.discounts_contact_hint),
            height = 48.dp,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
    }
}

/** Maydon ustidagi kichik yorliq. */
@Composable
private fun MiniLabel(text: String, palette: AppPalette) {
    Text(text, style = AppType.fieldLabel.copy(color = palette.label))
}

/** 5. Qanday ishlatiladi — uchta variant, limitlar yo'q (odatiy: kuniga 1 marta). */
@Composable
fun RedemptionSection(state: PostListingUiState, vm: PostListingViewModel) {
    FormSection(
        title = stringResource(Res.string.discounts_redemption_section),
        subtitle = state.redemptionMethod.hint,
        error = state.errorFor(ListingField.PROMO_CODE),
    ) {
        ChipFlow {
            RedemptionMethod.entries.forEach { method ->
                SelectChip(method.label, state.redemptionMethod == method, { vm.onRedemptionMethod(method) })
            }
        }
        if (state.redemptionMethod == RedemptionMethod.PROMO_CODE) {
            GlassTextField(
                state.promoCode,
                vm::onPromoCode,
                stringResource(Res.string.discounts_promo_hint),
                height = 46.dp,
            )
        }
    }
}

private val durationOptions = listOf(7, 14, 30, 60, 90)

/** 6. Amal qilish muddati. */
@Composable
fun ValiditySection(state: PostListingUiState, vm: PostListingViewModel) {
    FormSection(
        title = stringResource(Res.string.discounts_validity_section),
        subtitle = stringResource(Res.string.discounts_validity_subtitle, "${state.durationDays}"),
        error = state.errorFor(ListingField.VALIDITY),
    ) {
        ChipFlow {
            durationOptions.forEach { days ->
                SelectChip(
                    stringResource(Res.string.discounts_days, "$days"),
                    state.durationDays == days,
                    { vm.onDuration(days) },
                )
            }
        }
    }
}

/** Maydon oxiridagi o'lchov birligi ("so'm"). */
@Composable
private fun Suffix(text: String, palette: AppPalette) {
    Text(text, style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.inkFaint))
}
