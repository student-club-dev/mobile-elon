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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.OutlineButton
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.media.rememberImagePicker
import dev.core.designsystem.theme.AppPalette
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.presentation.BranchesSection
import dev.feature.discounts.presentation.MessageBar
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import dev.feature.discounts.presentation.components.ErrorColor
import dev.feature.discounts.presentation.components.IconSquareButton

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
 * Ekranning umumiy karkasi: sarlavha → 6 ta blok → tugmalar.
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                IconSquareButton(onBack, AppIcons.ArrowLeft, palette)
                Column {
                    // Sarlavha rejimga ergashadi: chegirma e'lonida "Kafe chegirmasi",
                    // oddiy e'londa "Kafe e'loni" — "chegirma" so'zi yolg'on turmasin.
                    Text(
                        when {
                            state.editing -> "E'lonni tahrirlash"
                            state.isDiscount -> copy.screenTitle
                            else -> copy.screenTitleRegular
                        },
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Black, color = palette.ink),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        if (state.isDiscount) copy.screenSubtitle else copy.screenSubtitleRegular,
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = palette.inkFaint),
                    )
                }
            }

            // Kiyim-kechakда — avval jins tanlash (erkak/ayol kiyimi).
            if (genderGate) ClothingGenderSection(state, palette, vm)

            // Jins gate: tanlanmaguncha (kiyimda) qolgan forma ko'rinmaydi.
            if (!genderGate || state.listingGender != null) {
                // SODDALASHTIRILGAN forma: turi + nomi + rasm + narx + tel + joylashuv.
                // E'lon turi (Chegirma / Oddiy) — faqat tab belgilamagan bo'lsa (modeLocked=false).
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
                // Narx (chegirmada Oldingi + Hozirgi).
                PriceAndDiscountSection(state, copy, vm)
                // Telefon raqami.
                ContactSection(state, vm)
                // Joylashuv (filiallar / xarita).
                BranchesSection(state, palette, vm)
            }

            val message = state.message
            if (message != null) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    MessageBar(message, palette, onDismiss = vm::consumeMessage)
                }
            }

            Spacer(Modifier.height(4.dp))
        }

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.errors.isNotEmpty()) {
                Text(
                    "To'ldirilmagan ${state.errors.size} ta joy bor — yuqorida qizil bilan belgilandi.",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.Bold, color = ErrorColor),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlineButton("Qoralama", vm::saveDraft)
                }
                Box(Modifier.weight(1.4f)) {
                    PrimaryButton(
                        if (state.submitting) "Yuborilmoqda..." else "E'lonni joylash",
                        vm::publish,
                        enabled = !state.submitting,
                    )
                }
            }
        }
    }
}
