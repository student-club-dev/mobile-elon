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
import dev.core.uikit.resources.discounts_applies_to_options
import dev.core.uikit.resources.discounts_conditions_hint
import dev.core.uikit.resources.discounts_conditions_label
import dev.core.uikit.resources.discounts_contact_hint
import dev.core.uikit.resources.discounts_contact_subtitle
import dev.core.uikit.resources.discounts_days
import dev.core.uikit.resources.discounts_discount_type_label
import dev.core.uikit.resources.discounts_duration_label
import dev.core.uikit.resources.discounts_images_subtitle
import dev.core.uikit.resources.discounts_limit_unlimited_hint
import dev.core.uikit.resources.discounts_per_user_limit_label
import dev.core.uikit.resources.discounts_percent_off
import dev.core.uikit.resources.discounts_percent_suffix
import dev.core.uikit.resources.discounts_phone_label
import dev.core.uikit.resources.discounts_price_label
import dev.core.uikit.resources.discounts_price_new_hint
import dev.core.uikit.resources.discounts_price_new_label
import dev.core.uikit.resources.discounts_price_old_hint
import dev.core.uikit.resources.discounts_price_old_label
import dev.core.uikit.resources.discounts_price_section
import dev.core.uikit.resources.discounts_price_subtitle_discount
import dev.core.uikit.resources.discounts_price_subtitle_regular
import dev.core.uikit.resources.discounts_price_unit_label
import dev.core.uikit.resources.discounts_promo_hint
import dev.core.uikit.resources.discounts_redemption_section
import dev.core.uikit.resources.discounts_redemption_url_hint
import dev.core.uikit.resources.discounts_start_date_hint
import dev.core.uikit.resources.discounts_start_date_invalid
import dev.core.uikit.resources.discounts_start_date_label
import dev.core.uikit.resources.discounts_student_pays
import dev.core.uikit.resources.discounts_sum_suffix
import dev.core.uikit.resources.discounts_total_limit_label
import dev.core.uikit.resources.discounts_validity_section
import dev.core.uikit.resources.discounts_validity_subtitle
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.PriceUnit
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.domain.model.ListingValidator
import dev.feature.discounts.domain.model.RedemptionMethod
import dev.feature.discounts.domain.model.RedemptionPeriod
import dev.feature.discounts.domain.model.formatSum
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import dev.feature.discounts.presentation.components.AddImageTile
import dev.feature.discounts.presentation.components.ChipFlow
import dev.feature.discounts.presentation.components.FormSection
import dev.feature.discounts.presentation.components.ImageThumb
import dev.feature.discounts.presentation.components.SelectChip
import org.jetbrains.compose.resources.stringResource
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.util.UZ_DIALING_CODE

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
                // Birinchi rasm — muqova (`images[0]`), buni foydalanuvchi ko'rib turishi kerak.
                ImageThumb(source, onRemove = { vm.removeImage(index) }, cover = index == 0)
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
        GlassTextField(state.title, vm::onTitle, stringResource(copy.titleHint), height = AppSize.fieldHeight, type = AppFieldType.LatinText)
        GlassTextField(state.description, vm::onDescription, stringResource(copy.descriptionHint), height = 110.dp, type = AppFieldType.LatinText)
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
        // Narx birligi (`priceUnit`) — biznes turiga ruxsat etilganlari. Bitta variant
        // bo'lsa tanlash shart emas (masalan kiyim doim "dona uchun").
        // Tur tanlanmagan bo'lsa cheklamaymiz — `priceUnits` barcha birliklarni beradi.
        val units = state.businessType?.let(ListingCatalog::priceUnits) ?: PriceUnit.entries
        if (units.size > 1) {
            MiniLabel(stringResource(Res.string.discounts_price_unit_label), palette)
            ChipFlow {
                units.forEach { unit ->
                    SelectChip(unit.label, state.priceUnit == unit, { vm.onPriceUnit(unit) })
                }
            }
        }

        MiniLabel(
            stringResource(
                if (state.isDiscount) Res.string.discounts_price_old_label else Res.string.discounts_price_label,
            ),
            palette,
        )
        GlassTextField(
            state.originalPrice, vm::onPrice, stringResource(Res.string.discounts_price_old_hint),
            height = AppSize.fieldHeight,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailing = { Suffix(stringResource(Res.string.discounts_sum_suffix), palette) },
            palette = palette,
        )

        if (state.isDiscount) {
            // Chegirma turi (`discount.type`) — har biri qiymat maydonini boshqacha o'qiydi.
            MiniLabel(stringResource(Res.string.discounts_discount_type_label), palette)
            ChipFlow {
                DiscountType.entries.forEach { type ->
                    SelectChip(type.label, state.discountType == type, { vm.onDiscountType(type) })
                }
            }

            // "1+1" da qiymat yo'q — nima berilishi shartda yoziladi.
            if (state.discountType != DiscountType.FREE_ITEM) {
                // Yorliq va misol tur bilan birga keladi ("Foiz (%)" / "20").
                MiniLabel(state.discountType.valueLabel, palette)
                GlassTextField(
                    state.discountValue, vm::onDiscountValue,
                    state.discountType.hint,
                    height = AppSize.fieldHeight,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailing = {
                        Suffix(
                            stringResource(
                                if (state.discountType == DiscountType.PERCENT) {
                                    Res.string.discounts_percent_suffix
                                } else {
                                    Res.string.discounts_sum_suffix
                                },
                            ),
                            palette,
                        )
                    },
                    palette = palette,
                )
            }

            // `discount.conditions` — "faqat ish kunlari", "ikkinchi kofe bepul".
            MiniLabel(stringResource(Res.string.discounts_conditions_label), palette)
            GlassTextField(
                state.conditions, vm::onConditions, stringResource(Res.string.discounts_conditions_hint),
                height = AppSize.fieldHeight,
                palette = palette,
            )

            // `discount.appliesToOptions` — faqat qo'shimchalar bo'lganda ma'noga ega.
            if (state.optionGroups.isNotEmpty()) {
                ChipFlow {
                    SelectChip(
                        stringResource(Res.string.discounts_applies_to_options),
                        state.appliesToOptions,
                        { vm.onAppliesToOptions(!state.appliesToOptions) },
                    )
                }
            }

            val old = state.originalPrice.toLongOrNull() ?: 0
            val new = state.finalPrice
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
                            color = palette.success,
                        ),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(Res.string.discounts_percent_off, "$percent"),
                        style = AppType.fieldLabel.copy(
                            fontWeight = AppType.buttonSecondary.fontWeight,
                            color = palette.success,
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
            height = AppSize.fieldHeight,
            leadingContent = { Text(UZ_DIALING_CODE, style = AppType.bodyStrong.copy(color = appPalette.ink)) },
            type = AppFieldType.UzPhone,
        )
    }
}

/** Maydon ustidagi kichik yorliq. */
@Composable
private fun MiniLabel(text: String, palette: AppPalette) {
    Text(text, style = AppType.fieldLabel.copy(color = palette.inkMuted))
}

/** 5. Qanday ishlatiladi — usul, kod/havola va limitlar (`redemption`). */
@Composable
fun RedemptionSection(state: PostListingUiState, vm: PostListingViewModel) {
    val palette = appPalette
    FormSection(
        title = stringResource(Res.string.discounts_redemption_section),
        subtitle = state.redemptionMethod.hint,
        error = state.errorFor(ListingField.PROMO_CODE)
            ?: state.errorFor(ListingField.REDEMPTION_URL)
            ?: state.errorFor(ListingField.LIMITS),
        palette = palette,
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
                height = AppSize.fieldHeight,
            )
        }
        // "Onlayn havola" usulida talaba chegirmani tashqi saytda ishlatadi — backend
        // `redemption.url` ni kutadi, shu sabab maydon usul bilan birga chiqadi.
        if (state.redemptionMethod == RedemptionMethod.ONLINE_LINK) {
            GlassTextField(
                state.redemptionUrl,
                vm::onRedemptionUrl,
                stringResource(Res.string.discounts_redemption_url_hint),
                height = AppSize.fieldHeight,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )
        }

        // `perUserLimit` + `perUserPeriod` — bo'sh maydon "cheksiz" degani.
        MiniLabel(stringResource(Res.string.discounts_per_user_limit_label), palette)
        GlassTextField(
            state.perUserLimit,
            vm::onPerUserLimit,
            stringResource(Res.string.discounts_limit_unlimited_hint),
            height = AppSize.fieldHeight,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            palette = palette,
        )
        ChipFlow {
            RedemptionPeriod.entries.forEach { period ->
                SelectChip(period.label, state.perUserPeriod == period, { vm.onPerUserPeriod(period) })
            }
        }

        // `totalLimit` — to'lgach e'lon avtomatik SOLD_OUT bo'ladi (spec §6.5).
        MiniLabel(stringResource(Res.string.discounts_total_limit_label), palette)
        GlassTextField(
            state.totalLimit,
            vm::onTotalLimit,
            stringResource(Res.string.discounts_limit_unlimited_hint),
            height = AppSize.fieldHeight,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            palette = palette,
        )
    }
}

private val durationOptions = listOf(7, 14, 30, 60, 90)

/** 6. Amal qilish muddati — boshlanish sanasi (`validFrom`) va davomiyligi (`validTo`). */
@Composable
fun ValiditySection(state: PostListingUiState, vm: PostListingViewModel) {
    val palette = appPalette
    FormSection(
        title = stringResource(Res.string.discounts_validity_section),
        subtitle = stringResource(Res.string.discounts_validity_subtitle, "${state.durationDays}"),
        error = state.errorFor(ListingField.VALIDITY),
        palette = palette,
    ) {
        // Bo'sh qoldirilsa — bugundan. Kelajakdagi sana e'lonni belgilangan kunga rejalashtiradi
        // (server uni `SCHEDULED` qilib qo'yadi va vaqti kelganda yoqadi).
        MiniLabel(stringResource(Res.string.discounts_start_date_label), palette)
        GlassTextField(
            state.startDate,
            vm::onStartDate,
            stringResource(Res.string.discounts_start_date_hint),
            height = AppSize.fieldHeight,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            palette = palette,
        )
        if (!state.startDateValid) {
            Text(
                stringResource(Res.string.discounts_start_date_invalid),
                style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.danger),
            )
        }

        MiniLabel(stringResource(Res.string.discounts_duration_label), palette)
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
