package dev.feature.discounts.presentation.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.BannerTone
import dev.core.uikit.component.OutlineButton
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenTopBar
import dev.core.uikit.component.ToastEffect
import dev.core.uikit.component.screenTopInset
import dev.core.uikit.media.rememberMultiImagePicker
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.discounts_draft
import dev.core.uikit.resources.discounts_edit_listing_title
import dev.core.uikit.resources.discounts_form_errors
import dev.core.uikit.resources.discounts_publish
import dev.core.uikit.resources.discounts_save_changes
import dev.core.uikit.resources.discounts_submitting
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.ListingValidator
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import dev.feature.discounts.presentation.components.LimitContext
import dev.feature.discounts.presentation.components.limitHintText
import org.jetbrains.compose.resources.stringResource

/**
 * Har bir biznes turining **o'z ekrani**.
 *
 * Ekranlar bir xil bloklardan yig'iladi, lekin yozuvlari har xil: kafeda "Taom nomi",
 * game club'da "Sessiya", o'quv markazda "Kurs nomi". Shu sabab bitta umumiy forma emas —
 * [ListingFormCopy] orqali har turga alohida matn beriladi va ekran o'sha turga tegishli
 * bo'ladi. Blok mantiqi esa bitta joyda ([ListingFormSections]) turadi, takrorlanmaydi.
 */
@Composable
fun TypeListingForm(
    type: BusinessType,
    state: PostListingUiState,
    palette: AppPalette,
    vm: PostListingViewModel,
    onBack: () -> Unit,
) {
    ListingFormScaffold(
        copy = ListingFormCopy.of(type),
        state = state,
        palette = palette,
        vm = vm,
        onBack = onBack,
    )
}

/**
 * Ekranning umumiy karkasi: sarlavha → bloklar → tugmalar.
 *
 * Forma **ataylab qisqa** — faqat e'lonni ma'noli qiladigan bloklar: e'lon turi, kategoriya,
 * tafsilot, rasm, narx, promokod (chegirmada) va telefon. Turga xos atributlar, qo'shimchalar (option groups),
 * amal qilish muddati, chegirma turi, foydalanish limitlari va filial tanlash formadan olib
 * tashlangan. E'lon biznesning **barcha filiallarida** amal qiladi (`branchIds` — hammasi).
 */
@Composable
private fun ListingFormScaffold(
    copy: ListingFormCopy,
    state: PostListingUiState,
    palette: AppPalette,
    vm: PostListingViewModel,
    onBack: () -> Unit,
) {
    // E'longa 5 tagacha rasm qo'yiladi — galereyadan ularni BIR MARTADA tanlash mumkin.
    // Chegara qolgan bo'sh joyga qarab beriladi, shunda tizim tanlagichining o'zi ortiqcha
    // tanlashga yo'l qo'ymaydi (foydalanuvchi tanlab bo'lgach "sig'madi" deb aytishdan yaxshiroq).
    val remainingSlots = (ListingValidator.MAX_IMAGES - state.images.size).coerceAtLeast(1)
    val imagePicker = rememberMultiImagePicker(maxItems = remainingSlots) { picked ->
        vm.addImages(picked.map { it.bytes to it.fileName })
    }

    // imePadding — klaviatura chiqqanda forma va pastdagi amal paneli uning ustiga ko'chadi;
    // scroll qismi qisqaradi, fokusdagi maydon esa o'zini ko'rinadigan joyga suradi
    // (`GlassTextField` ichidagi `keyboardAware`).
    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).screenTopInset(AppSpacing.md),
            // Flat dizayn — bo'limlar orasi kengroq; yon padding har bo'lim ichida.
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
        ) {
            // Sarlavha rejimga ergashadi: chegirma e'lonida "Kafe chegirmasi",
            // oddiy e'londa "Kafe e'loni" — "chegirma" so'zi yolg'on turmasin.
            ScreenTopBar(
                title = stringResource(
                    when {
                        state.editing -> Res.string.discounts_edit_listing_title
                        state.isDiscount -> copy.screenTitle
                        else -> copy.screenTitleRegular
                    },
                ),
                subtitle = stringResource(
                    if (state.isDiscount) copy.screenSubtitle else copy.screenSubtitleRegular,
                ),
                onBack = onBack,
                backContentDescription = stringResource(Res.string.common_back),
                modifier = Modifier.padding(horizontal = AppSpacing.lg),
                palette = palette,
            )

            // E'lon turi — Chegirma yoki Oddiy (narx bloki va promokod shunga bog'liq).
            ListingModeSection(state, vm)
            // Bo'lim (kategoriya) — horizontal scroll.
            CategorySection(state, copy, vm)
            // Nomi + qo'shimcha ma'lumot (qolgan hamma tafsilot shu yerda yoziladi).
            AboutSection(state, copy, vm)
            // Rasm.
            ImagesSection(state, copy, vm, onAdd = imagePicker::pick)
            // Narx: asl narx + chegirma foizi.
            PriceAndDiscountSection(state, copy, vm)
            // Promokod — faqat chegirma e'lonida (oddiy e'londa chegirma berilmaydi).
            if (state.isDiscount) RedemptionSection(state, vm)
            // Telefon raqami.
            ContactSection(state, vm)

            Spacer(Modifier.height(AppSpacing.xs))
        }

        Column(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            // Ikkala tugma ham teng kenglikda (`weight(1f)`), bir xil balandlik va radiusда:
            // ilgari "E'lon qilish" 1.4 barobar keng, "Qoralama" esa pastroq va boshqa
            // burchakli edi — juftlik nomutanosib ko'rinar, qoralama esa ikkinchi darajali
            // emas, o'chirilgandek tuyulardi. Soya ham ikkalasida (qorong'i rejimda ham).
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlineButton(
                        stringResource(Res.string.discounts_draft),
                        vm::saveDraft,
                        height = AppSize.buttonHeight,
                        shape = AppRadius.button,
                        elevated = true,
                        palette = palette,
                    )
                }
                Box(Modifier.weight(1f)) {
                    // Tahrirlashда tugma "E'lon qilish" emas: e'lon allaqachon chop etilgan
                    // va bu forma faqat o'zgarishlarni saqlaydi.
                    PrimaryButton(
                        stringResource(
                            when {
                                state.submitting -> Res.string.discounts_submitting
                                state.editing -> Res.string.discounts_save_changes
                                else -> Res.string.discounts_publish
                            },
                        ),
                        vm::publish,
                        enabled = !state.submitting,
                        loading = state.submitting,
                        palette = palette,
                    )
                }
            }
        }
    }

    // Xabar (publish xatosi, "Qoralama saqlandi", rasm xatosi) va to'ldirilmagan maydonlar
    // haqidagi ogohlantirish — endi toastда, tugmalar ustidagi yozuv emas.
    //
    // Sabab: bu yozuvlar formaning pastida turardi va uzun formada foydalanuvchi ularni
    // umuman ko'rmasdi; "Qoralama saqlandi" esa ekranда qolib, tugmalarni yuqoriga surib
    // yuborardi. Toast kontent USTIDA chiqadi va o'zi yo'qoladi.
    ToastEffect(
        message = state.message,
        tone = if (state.errors.isEmpty()) BannerTone.SUCCESS else BannerTone.DANGER,
        hint = limitHintText(state.limitCode, LimitContext.LISTING),
        onConsumed = vm::consumeMessage,
    )
    val errorSummary = if (state.errors.isEmpty()) {
        null
    } else {
        stringResource(Res.string.discounts_form_errors, "${state.errors.size}")
    }
    ToastEffect(
        message = errorSummary,
        tone = BannerTone.DANGER,
        // Maydonlar ro'yxati holatда qoladi (ular qizil bo'lib turadi) — toast faqat
        // e'tiborni tortadi, shuning uchun "iste'mol qilish" kerak emas.
        onConsumed = {},
    )
}
