package dev.feature.discounts.presentation.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import dev.feature.discounts.presentation.components.BranchesSection
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
        // Kiyim-kechakда avval jins (erkak/ayol) tanlanadi.
        genderGate = type == BusinessType.CLOTHING,
    )
}

/**
 * Ekranning umumiy karkasi: sarlavha → bloklar → tugmalar.
 * Bloklarning ketma-ketligi barcha turlarda bir xil (o'rganish oson), yozuvlari — har xil.
 */
@Composable
private fun ListingFormScaffold(
    copy: ListingFormCopy,
    state: PostListingUiState,
    palette: AppPalette,
    vm: PostListingViewModel,
    onBack: () -> Unit,
    /** `true` (Kiyim-kechak) — avval jins tanlanadi, keyin qolgan forma ochiladi. */
    genderGate: Boolean = false,
) {
    val imagePicker = rememberImagePicker { picked ->
        if (picked != null) vm.addImage(picked.bytes, picked.fileName)
    }

    Column(Modifier.fillMaxSize()) {
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

            // Kiyim-kechakда — avval jins tanlash (erkak/ayol kiyimi).
            if (genderGate) ClothingGenderSection(state, palette, vm)

            // Jins gate: tanlanmaguncha (kiyimda) qolgan forma ko'rinmaydi.
            if (!genderGate || state.listingGender != null) {
                // SODDALASHTIRILGAN forma: turi + nomi + rasm + narx + tel + joylashuv.
                // Bo'lim (kategoriya) — horizontal scroll.
                CategorySection(state, copy, vm)
                // Kategoriyaga xos maydonlar — Futbolka → razmerlar, PlayStation → model (PS5/PS4).
                // Kategoriya tanlanmaguncha bo'sh bo'ladi va ko'rinmaydi.
                AttributesSection(state, vm)
                // Nomi + qo'shimcha ma'lumot.
                AboutSection(state, copy, vm)
                // Rasm.
                ImagesSection(state, copy, vm, onAdd = imagePicker::pick)
                // E'lon turi (Chegirma / Oddiy) — bevosita narx tepasida, chunki u narx
                // ko'rinishini belgilaydi: chegirmada 2 narx, oddiyda 1 narx.
                if (!state.modeLocked) ListingModeSection(state, vm)
                // Narx (birlik, chegirma turi va qiymati, shartlar).
                PriceAndDiscountSection(state, copy, vm)
                // Qo'shimchalar — "Hajmni tanlang" kabi guruhlar (ixtiyoriy).
                OptionGroupsSection(state, vm)
                // Talaba chegirmani qanday oladi + limitlar.
                RedemptionSection(state, vm)
                // Amal qilish muddati (boshlanish sanasi + davomiyligi).
                ValiditySection(state, vm)
                // Telefon raqami.
                ContactSection(state, vm)
                // E'lon qaysi filiallarda amal qiladi.
                BranchesSection(state, palette, vm)
            }

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
