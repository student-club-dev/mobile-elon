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
import dev.core.uikit.component.OutlineButton
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenTopBar
import dev.core.uikit.media.rememberImagePicker
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.discounts_draft
import dev.core.uikit.resources.discounts_edit_listing_title
import dev.core.uikit.resources.discounts_form_errors
import dev.core.uikit.resources.discounts_publish
import dev.core.uikit.resources.discounts_submitting
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import dev.feature.discounts.presentation.components.LimitContext
import dev.feature.discounts.presentation.components.LimitHint
import dev.feature.discounts.presentation.components.MessageBar
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
    val imagePicker = rememberImagePicker { picked ->
        if (picked != null) vm.addImage(picked.bytes, picked.fileName)
    }

    // imePadding — klaviatura chiqqanda forma va pastdagi amal paneli uning ustiga ko'chadi;
    // scroll qismi qisqaradi, fokusdagi maydon esa o'zini ko'rinadigan joyga suradi
    // (`GlassTextField` ichidagi `keyboardAware`).
    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(top = 54.dp),
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
            // Xabar (publish xatosi, "Qoralama saqlandi", rasm xatosi) — AYNAN tugmalar
            // ustida. Ilgari u aylanadigan ro'yxatning eng oxirida edi: foydalanuvchi forma
            // boshida turib "E'lon qilish"ni bosса, server rad etgan xabar ekrandan tashqarida
            // qolar va bosish umuman javobsizdek ko'rinardi.
            val message = state.message
            if (message != null) {
                MessageBar(message, palette, onDismiss = vm::consumeMessage)
                // Chegara (429) bo'lsa — serverning xabari ostiga amaliy maslahat qo'shiladi:
                // "chegara to'ldi" o'zi nima qilish kerakligini aytmaydi.
                LimitHint(state.limitCode, palette, LimitContext.LISTING)
            }

            if (state.errors.isNotEmpty()) {
                Text(
                    stringResource(Res.string.discounts_form_errors, "${state.errors.size}"),
                    // Xato rangi palitradan — qorong'i rejimda ham o'qiladi.
                    style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.danger),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlineButton(stringResource(Res.string.discounts_draft), vm::saveDraft, palette = palette)
                }
                Box(Modifier.weight(1.4f)) {
                    PrimaryButton(
                        stringResource(
                            if (state.submitting) Res.string.discounts_submitting else Res.string.discounts_publish,
                        ),
                        vm::publish,
                        enabled = !state.submitting,
                        palette = palette,
                    )
                }
            }
        }
    }
}
