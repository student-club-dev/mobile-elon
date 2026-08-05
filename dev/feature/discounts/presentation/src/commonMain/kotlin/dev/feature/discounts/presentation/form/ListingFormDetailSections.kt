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
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.domain.model.ListingValidator
import dev.feature.discounts.domain.model.formatSum
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import dev.feature.discounts.presentation.components.AddImageTile
import dev.feature.discounts.presentation.components.FormSection
import dev.feature.discounts.presentation.components.ImageThumb
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
        // Narx birligi ("soat uchun", "dona uchun") formada so'ralmaydi — u biznes turining
        // odatiy qiymatidan olinadi (`prefillFromBusiness`).
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
            // Chegirma **yangi narx** sifatida so'raladi (`SPECIAL_PRICE`): foizni tizim o'zi
            // hisoblaydi va pastda ko'rsatadi. Chegirma turini tanlash, shartlar matni va
            // "qo'shimchalarga tegishli" bayrog'i formadan olib tashlangan.
            MiniLabel(stringResource(Res.string.discounts_price_new_label), palette)
            GlassTextField(
                state.discountValue, vm::onDiscountValue,
                stringResource(Res.string.discounts_price_new_hint),
                height = AppSize.fieldHeight,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailing = { Suffix(stringResource(Res.string.discounts_sum_suffix), palette) },
                palette = palette,
            )

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

/**
 * 5. Promokod — talaba chegirmani shu kod bilan oladi (`redemption.promoCode`).
 *
 * **Faqat chegirma e'lonida** ko'rinadi (oddiy e'londa beriladigan chegirma yo'q, demak kod
 * ham keraksiz — qarang `TypeForms`). Usul tanlash (talaba ID / QR / onlayn havola) va
 * foydalanish limitlari formadan olib tashlangan: usul har doim `PROMO_CODE`.
 */
@Composable
fun RedemptionSection(state: PostListingUiState, vm: PostListingViewModel) {
    val palette = appPalette
    FormSection(
        title = stringResource(Res.string.discounts_redemption_section),
        subtitle = state.redemptionMethod.hint,
        error = state.errorFor(ListingField.PROMO_CODE),
        palette = palette,
    ) {
        GlassTextField(
            state.promoCode,
            vm::onPromoCode,
            stringResource(Res.string.discounts_promo_hint),
            height = AppSize.fieldHeight,
        )
    }
}

/** Maydon oxiridagi o'lchov birligi ("so'm"). */
@Composable
private fun Suffix(text: String, palette: AppPalette) {
    Text(text, style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.inkFaint))
}
