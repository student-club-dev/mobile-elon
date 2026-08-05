package dev.feature.auth.presentation.main

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.AdType
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.GlassRow
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.IconTile
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_ad_add_photo
import dev.core.uikit.resources.auth_ad_category_label
import dev.core.uikit.resources.auth_ad_category_placeholder
import dev.core.uikit.resources.auth_ad_description_label
import dev.core.uikit.resources.auth_ad_description_placeholder
import dev.core.uikit.resources.auth_ad_price_label
import dev.core.uikit.resources.auth_ad_price_placeholder
import dev.core.uikit.resources.auth_ad_submit
import dev.core.uikit.resources.auth_ad_title_label
import dev.core.uikit.resources.auth_ad_title_placeholder
import dev.core.uikit.resources.auth_ad_type_job
import dev.core.uikit.resources.auth_ad_type_job_desc
import dev.core.uikit.resources.auth_ad_type_other
import dev.core.uikit.resources.auth_ad_type_other_desc
import dev.core.uikit.resources.auth_ad_type_rental
import dev.core.uikit.resources.auth_ad_type_rental_desc
import dev.core.uikit.resources.auth_ad_type_sale
import dev.core.uikit.resources.auth_ad_type_sale_desc
import dev.core.uikit.resources.auth_ad_type_service
import dev.core.uikit.resources.auth_ad_type_service_desc
import dev.core.uikit.resources.auth_post_ad
import dev.core.uikit.resources.auth_post_ad_question
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_close
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** E'lon turi va uning ekrandagi tavsifi (matnlar resursdan). */
private data class AdTypeInfo(
    val type: AdType,
    val title: StringResource,
    val subtitle: StringResource,
    val icon: ImageVector,
)

private val adTypes = listOf(
    AdTypeInfo(AdType.JOB, Res.string.auth_ad_type_job, Res.string.auth_ad_type_job_desc, AppIcons.Briefcase),
    AdTypeInfo(AdType.RENTAL, Res.string.auth_ad_type_rental, Res.string.auth_ad_type_rental_desc, AppIcons.Home),
    AdTypeInfo(AdType.SALE, Res.string.auth_ad_type_sale, Res.string.auth_ad_type_sale_desc, AppIcons.Store),
    AdTypeInfo(AdType.SERVICE, Res.string.auth_ad_type_service, Res.string.auth_ad_type_service_desc, AppIcons.GraduationCap),
    AdTypeInfo(AdType.OTHER, Res.string.auth_ad_type_other, Res.string.auth_ad_type_other_desc, AppIcons.MessageSquare),
)

@Composable
fun PostAdScreen(onClose: () -> Unit, editAdId: String? = null, vm: PostAdViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()

    // Tahrirlash rejimi — mavjud e'lonni forma'ga yuklaymiz.
    LaunchedEffect(editAdId) { if (editAdId != null) vm.loadForEdit(editAdId) }
    LaunchedEffect(state.posted) { if (state.posted) onClose() }

    if (state.type == null) {
        AdTypePicker(palette, onClose = onClose, onPick = vm::selectType)
    } else {
        AdForm(state, palette, onBack = vm::backToTypes, vm = vm)
    }
}

// 1s — tur tanlash
@Composable
private fun AdTypePicker(palette: AppPalette, onClose: () -> Unit, onPick: (AdType) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = AppSpacing.lg).padding(top = 54.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            BackButton(
                onClose,
                icon = AppIcons.Close,
                contentDescription = stringResource(Res.string.common_close),
                iconSize = 18.dp,
            )
            Text(
                stringResource(Res.string.auth_post_ad),
                style = AppType.topBarTitle.copy(color = palette.ink),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(Res.string.auth_post_ad_question),
            style = AppType.link.copy(color = palette.inkMuted),
        )
        Spacer(Modifier.height(AppSpacing.lg))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            adTypes.forEach { info ->
                GlassRow(
                    shape = AppRadius.md,
                    onClick = { onPick(info.type) },
                    palette = palette,
                ) {
                    IconTile(
                        info.icon,
                        tint = palette.primary,
                        background = palette.accentBg,
                        size = AppSize.iconTile,
                        iconSize = AppSize.iconLg,
                        shape = AppRadius.sm,
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(info.title),
                            style = AppType.body.copy(fontWeight = AppType.button.fontWeight, color = palette.ink),
                        )
                        Text(
                            stringResource(info.subtitle),
                            style = AppType.hint.copy(color = palette.inkFaint),
                        )
                    }
                    Icon(AppIcons.ChevronRight, null, tint = palette.inkFaint, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// 1v — forma
@Composable
private fun AdForm(state: PostAdUiState, palette: AppPalette, onBack: () -> Unit, vm: PostAdViewModel) {
    val info = adTypes.first { it.type == state.type }
    // imePadding — klaviatura chiqqanda forma uning ustiga ko'chadi, pastdagi maydonlar
    // klaviatura ostida qolmaydi.
    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg).padding(top = 54.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                BackButton(onBack, contentDescription = stringResource(Res.string.common_back), iconSize = 18.dp)
                Text(
                    stringResource(info.title),
                    style = AppType.sectionTitle.copy(color = palette.ink),
                )
            }
            Spacer(Modifier.height(AppSpacing.lg))

            // Rasm qo'shish (placeholder)
            // Rasm maydoni — chegara emas, ochiq ko'k aksent yuzasi (yangi dizayn tili).
            Box(
                Modifier.fillMaxWidth().height(120.dp).clip(AppRadius.md)
                    .background(palette.accentBg),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(AppIcons.ImageIcon, null, tint = palette.inkFaint, modifier = Modifier.size(26.dp))
                    Text(
                        stringResource(Res.string.auth_ad_add_photo),
                        style = AppType.fieldLabel.copy(color = palette.inkFaint),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            FieldTitle(stringResource(Res.string.auth_ad_title_label), palette)
            GlassTextField(state.title, vm::onTitle, stringResource(Res.string.auth_ad_title_placeholder), height = AppSize.fieldHeight)
            Spacer(Modifier.height(AppSpacing.md))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    FieldTitle(stringResource(Res.string.auth_ad_category_label), palette)
                    GlassTextField(
                        state.category,
                        vm::onCategory,
                        stringResource(Res.string.auth_ad_category_placeholder),
                        height = AppSize.fieldHeight,
                    )
                }
                Column(Modifier.weight(1f)) {
                    FieldTitle(stringResource(Res.string.auth_ad_price_label), palette)
                    GlassTextField(
                        state.price,
                        vm::onPrice,
                        stringResource(Res.string.auth_ad_price_placeholder),
                        height = AppSize.fieldHeight,
                    )
                }
            }
            Spacer(Modifier.height(AppSpacing.md))
            FieldTitle(stringResource(Res.string.auth_ad_description_label), palette)
            GlassTextField(
                state.description,
                vm::onDescription,
                stringResource(Res.string.auth_ad_description_placeholder),
                height = 96.dp,
            )
            Spacer(Modifier.height(AppSpacing.lg))
        }
        // Sticky tugma
        Box(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)) {
            PrimaryButton(stringResource(Res.string.auth_ad_submit), vm::submit, enabled = state.canSubmit)
        }
    }
}

@Composable
private fun FieldTitle(text: String, palette: AppPalette) {
    Text(text, style = AppType.fieldLabel.copy(color = palette.inkMuted))
    Spacer(Modifier.height(7.dp))
}
