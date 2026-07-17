package dev.feature.discounts.presentation.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import androidx.compose.ui.graphics.Color
import dev.feature.discounts.domain.model.AttributeKind
import dev.feature.discounts.domain.model.DiscountType
import dev.feature.discounts.domain.model.Gender
import dev.feature.discounts.domain.model.ListingCatalog
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.domain.model.ListingValidator
import dev.feature.discounts.domain.model.RedemptionMethod
import dev.feature.discounts.domain.model.formatSum
import dev.feature.discounts.presentation.PostListingUiState
import dev.feature.discounts.presentation.PostListingViewModel
import dev.feature.discounts.presentation.categories
import dev.feature.discounts.presentation.categoryAttributes
import dev.feature.discounts.presentation.components.AddImageTile
import dev.feature.discounts.presentation.components.ChipFlow
import dev.feature.discounts.presentation.components.FormSection
import dev.feature.discounts.presentation.components.ImageThumb
import dev.feature.discounts.presentation.components.SectionHPad
import dev.feature.discounts.presentation.components.SectionHeader
import dev.feature.discounts.presentation.components.SelectChip

/**
 * E'lon formasining umumiy bloklari. Har bir tur ekrani ([TypeForms]) shu bloklarni
 * o'z matnlari ([ListingFormCopy]) bilan yig'adi.
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
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.padding(horizontal = SectionHPad)) {
            SectionHeader("E'lon turi", "Chegirmali yoki oddiy e'lon")
        }
        LazyRow(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = SectionHPad),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SelectChip("Chegirma e'loni", state.isDiscount, { vm.onListingMode(true) }) }
            item { SelectChip("Oddiy e'lon", !state.isDiscount, { vm.onListingMode(false) }) }
        }
    }
}

/**
 * 0. TURI (bo'lim) — formaning ENG TEPASIDA, HORIZONTAL SCROLL bilan. Masalan Game Club'da
 * PlayStation / Stol tennis / Billiard ... bir qatorда suriladi.
 */
/**
 * Kiyim-kechak e'lonида **birinchi qadam** — erkak yoki ayol kiyimi. Tanlangач qolgan forma
 * (kategoriyalar shu jinsга moslangan holda) ochiladi.
 */
@Composable
fun ClothingGenderSection(state: PostListingUiState, palette: AppPalette, vm: PostListingViewModel) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.padding(horizontal = SectionHPad)) {
            SectionHeader("Kimlar uchun kiyim", "Turini tanlang — kategoriyalar shunga moslanadi")
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = SectionHPad),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GenderCard("👨", "Erkaklar", state.listingGender == Gender.MALE, { vm.onListingGender(Gender.MALE) }, Modifier.weight(1f), palette)
            GenderCard("👩", "Ayollar", state.listingGender == Gender.FEMALE, { vm.onListingGender(Gender.FEMALE) }, Modifier.weight(1f), palette)
        }
    }
}

@Composable
private fun GenderCard(emoji: String, label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier, palette: AppPalette) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (active) palette.primary else palette.glass)
            .then(if (active) Modifier else Modifier.border(1.dp, palette.border, shape))
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(emoji, style = TextStyle(fontSize = 30.sp))
        Text(
            label,
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else palette.ink),
        )
    }
}

@Composable
fun CategorySection(
    state: PostListingUiState,
    copy: ListingFormCopy,
    vm: PostListingViewModel,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.padding(horizontal = SectionHPad)) {
            SectionHeader("Turi", copy.categoryHint)
            state.errorFor(ListingField.CATEGORY)?.let {
                Text(it, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.Bold, color = appPalette.primary))
            }
        }
        // Edge-to-edge horizontal scroll — chiplar ekran chetigacha suriladi.
        LazyRow(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = SectionHPad),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                GlassTextField(state.customCategoryName, vm::onCustomCategory, "Nimaga amal qiladi?", height = 46)
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
        title = copy.businessSection,
        subtitle = "Talaba e'lonni shu nom ostida ko'radi",
        error = state.errorFor(ListingField.BUSINESS_NAME),
    ) {
        GlassTextField(state.businessName, vm::onBusinessName, copy.businessHint, height = 48)
    }
}

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
        title = "Tafsilotlar",
        subtitle = "Tanlangan bo'limga mos ma'lumotlar",
        error = state.errorFor(ListingField.ATTRIBUTES),
    ) {
        specs.forEach { spec ->
            val value = state.attributeValues[spec.key].orEmpty()
            Text(
                spec.label + if (spec.suffix != null) " (${spec.suffix})" else "",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.label),
            )
            when (spec.kind) {
                AttributeKind.TEXT, AttributeKind.TAGS ->
                    GlassTextField(value, { vm.onAttribute(spec.key, it) }, spec.hint.ifBlank { spec.label }, height = 46)

                AttributeKind.NUMBER ->
                    GlassTextField(
                        value, { vm.onAttribute(spec.key, it.filter { c -> c.isDigit() }) },
                        spec.hint.ifBlank { "0" },
                        height = 46,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )

                AttributeKind.SELECT ->
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectChip("Ha", value == "true", onClick = { vm.onAttribute(spec.key, "true") })
                        SelectChip("Yo'q", value == "false", onClick = { vm.onAttribute(spec.key, "false") })
                    }
            }
        }
    }
}

/** 2. Rasmlar. */
@Composable
fun ImagesSection(
    state: PostListingUiState,
    copy: ListingFormCopy,
    vm: PostListingViewModel,
    onAdd: () -> Unit,
) {
    FormSection(
        title = copy.imagesSection,
        subtitle = "${copy.imagesHint} · maksimal ${ListingValidator.MAX_IMAGES} ta",
        error = state.errorFor(ListingField.IMAGES),
    ) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
        title = copy.aboutSection,
        subtitle = "Tafsilotlarni shu yerga erkin yozing — alohida maydonlar kerak emas",
        error = state.errorFor(ListingField.TITLE),
    ) {
        GlassTextField(state.title, vm::onTitle, copy.titleHint, height = 48)
        GlassTextField(state.description, vm::onDescription, copy.descriptionHint, height = 110)
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
        title = "Narx",
        subtitle = if (state.isDiscount) "Oldingi va hozirgi (chegirmali) narx" else "E'lon narxi",
        error = state.errorFor(ListingField.PRICE) ?: state.errorFor(ListingField.DISCOUNT),
    ) {
        MiniLabel(if (state.isDiscount) "Oldingi narx" else "Narx", palette)
        GlassTextField(
            state.originalPrice, vm::onPrice, "Masalan: 50 000",
            height = 48,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailing = { Suffix("so'm", palette) },
        )

        if (state.isDiscount) {
            MiniLabel("Hozirgi narx (chegirmali)", palette)
            GlassTextField(
                state.discountValue, vm::onDiscountValue, "Masalan: 35 000",
                height = 48,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailing = { Suffix("so'm", palette) },
            )

            val old = state.originalPrice.toLongOrNull() ?: 0
            val new = state.discountValue.toLongOrNull() ?: 0
            if (old > 0 && new in 1 until old) {
                val percent = (old - new) * 100 / old
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(palette.successBg).padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Talaba to'laydi: ${new.formatSum()} so'm",
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Black, color = palette.successDeep),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "-$percent%",
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = palette.successDeep),
                    )
                }
            }
        }
    }
}

/** Aloqa — telefon raqami. */
@Composable
fun ContactSection(state: PostListingUiState, vm: PostListingViewModel) {
    FormSection(title = "Telefon raqami", subtitle = "Talaba shu raqamga bog'lanadi") {
        GlassTextField(
            state.contactPhone, vm::onContactPhone, "+998 90 123 45 67",
            height = 48,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
    }
}

/** Maydon ustidagi kichik yorliq. */
@Composable
private fun MiniLabel(text: String, palette: dev.core.designsystem.theme.AppPalette) {
    Text(
        text,
        style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.label),
    )
}

/** 5. Qanday ishlatiladi — uchta variant, limitlar yo'q (odatiy: kuniga 1 marta). */
@Composable
fun RedemptionSection(state: PostListingUiState, vm: PostListingViewModel) {
    FormSection(
        title = "Talaba qanday oladi",
        subtitle = state.redemptionMethod.hint,
        error = state.errorFor(ListingField.PROMO_CODE),
    ) {
        ChipFlow {
            RedemptionMethod.entries.forEach { method ->
                SelectChip(method.label, state.redemptionMethod == method, { vm.onRedemptionMethod(method) })
            }
        }
        if (state.redemptionMethod == RedemptionMethod.PROMO_CODE) {
            GlassTextField(state.promoCode, vm::onPromoCode, "NAVRUZ20", height = 46)
        }
    }
}

private val durationOptions = listOf(7, 14, 30, 60, 90)

/** 6. Amal qilish muddati. */
@Composable
fun ValiditySection(state: PostListingUiState, vm: PostListingViewModel) {
    FormSection(
        title = "Qancha vaqt amal qiladi",
        subtitle = "Bugundan boshlab ${state.durationDays} kun",
        error = state.errorFor(ListingField.VALIDITY),
    ) {
        ChipFlow {
            durationOptions.forEach { days ->
                SelectChip("$days kun", state.durationDays == days, { vm.onDuration(days) })
            }
        }
    }
}

@Composable
private fun Suffix(text: String, palette: AppPalette) {
    Text(
        text,
        style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.Bold, color = palette.inkFaint),
    )
}

@Composable
fun SectionSpacer() = Box(Modifier.padding(top = 0.dp))
