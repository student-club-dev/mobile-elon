package dev.feature.business

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GlassCard
import dev.core.uikit.component.GlassRow
import dev.core.uikit.component.GradientHeader
import dev.core.uikit.component.HeaderIconButton
import dev.core.uikit.component.IconTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.business_account_title
import dev.core.uikit.resources.business_field_phone
import dev.core.uikit.resources.business_info_email
import dev.core.uikit.resources.business_info_hours
import dev.core.uikit.resources.business_info_hours_unset
import dev.core.uikit.resources.business_info_type
import dev.core.uikit.resources.business_menu_listings
import dev.core.uikit.resources.business_menu_settings
import dev.core.uikit.resources.business_rating_new
import dev.core.uikit.resources.business_stat_active
import dev.core.uikit.resources.business_stat_redemptions
import dev.core.uikit.resources.business_stat_views
import dev.core.uikit.resources.business_type_default
import dev.core.uikit.resources.business_verified
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_edit
import dev.core.uikit.resources.common_logout
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.business.components.AccountRow
import dev.feature.business.components.InfoRow
import dev.feature.business.components.RowPaddingHorizontal
import dev.feature.business.components.RowPaddingVertical
import dev.feature.business.components.StatCard
import dev.feature.profile.presentation.ProfileViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Handoff: gradient header ostki burchagi 36dp. */
private val HeaderCorner = 36.dp

/** Handoff: kartalar bloki header ostiga 18dp kirib turadi. */
private val CardOverlap = 18.dp

/** Handoff: bloklar orasidagi bir xil oraliq. */
private val BlockGap = 16.dp

/**
 * Biznesmen profili — ideal ko'rinish. Talaba `ProfileScreen`iga aloqasi YO'Q. Biznes kartasi
 * (tur + tasdiq + reyting), statistika, biznes ma'lumotlari, menyu va chiqishdan iborat.
 *
 * Tuzilishi handoff'dagi "SCREEN 2: Biznes profili" bilan bir xil.
 */
@Composable
fun BusinessAccountScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenListings: () -> Unit,
    onOpenSettings: () -> Unit,
    onLoggedOut: () -> Unit,
    vm: ProfileViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()

    val businessName = state.profile?.businessName?.takeIf { it.isNotBlank() } ?: state.name
    // Saqlangan tur — barqaror id. Ekranda tarjimasi ko'rsatiladi, ikonka esa id bo'yicha tanlanadi.
    val businessTypeId = state.profile?.businessType?.takeIf { it.isNotBlank() }
    val businessTypeText = businessTypeId?.let { businessTypeLabel(it) }
        ?: stringResource(Res.string.business_type_default)
    val phone = state.profile?.phoneNumber?.takeIf { it.isNotBlank() } ?: state.contact.takeIf { it.isNotBlank() }
    val email = state.profile?.email?.takeIf { it.isNotBlank() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Gradient sarlavha
        GradientHeader(cornerRadius = HeaderCorner, palette = palette) {
            // Dekorativ doira — o'ng yuqorida, header chetidan tashqariga chiqmasligi uchun
            // qirqiladi. Gradient USTIDA turgani uchun rangi palitradan emas, oq shaffof.
            Box(Modifier.matchParentSize().clip(AppRadius.headerBottom)) {
                Box(
                    Modifier.align(Alignment.TopEnd)
                        .offset(x = 40.dp, y = (-60).dp)
                        .size(200.dp)
                        .clip(AppRadius.pill)
                        .background(Color.White.copy(alpha = 0.10f)),
                )
            }
            Row(
                Modifier.padding(
                    start = AppSpacing.screenHorizontal,
                    end = AppSpacing.screenHorizontal,
                    top = 54.dp,
                    bottom = 30.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderIconButton(
                    AppIcons.ArrowLeft,
                    onClick = onBack,
                    contentDescription = stringResource(Res.string.common_back),
                )
                Text(
                    stringResource(Res.string.business_account_title),
                    modifier = Modifier.weight(1f),
                    // Gradient ustidagi matn — palitra emas, doim oq.
                    style = AppType.sheetTitle.copy(color = Color.White),
                    textAlign = TextAlign.Center,
                )
                HeaderIconButton(
                    AppIcons.Pencil,
                    onClick = onEdit,
                    contentDescription = stringResource(Res.string.common_edit),
                    iconSize = AppSize.iconSm,
                )
            }
        }

        Column(
            Modifier.fillMaxWidth()
                // Kartalar bloki header ostiga kirib turadi.
                .offset(y = -CardOverlap)
                .padding(horizontal = AppSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(BlockGap),
        ) {
            // Biznes kartasi
            GlassRow(
                shape = AppRadius.card,
                contentPadding = PaddingValues(RowPaddingHorizontal),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                palette = palette,
            ) {
                IconTile(
                    typeIcon(businessTypeId.orEmpty()),
                    tint = palette.primary,
                    background = palette.accentBg,
                    size = 58.dp,
                    iconSize = AppSize.iconFab,
                    shape = AppRadius.row,
                )
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(businessName, style = AppType.cardTitle.copy(color = palette.ink))
                        Icon(
                            AppIcons.ShieldCheck,
                            stringResource(Res.string.business_verified),
                            tint = palette.success,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(businessTypeText, style = AppType.secondary.copy(color = palette.inkMuted))
                    Spacer(Modifier.height(AppSpacing.xs))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        Icon(AppIcons.Star, null, tint = palette.warning, modifier = Modifier.size(13.dp))
                        Text(
                            stringResource(Res.string.business_rating_new),
                            style = AppType.groupLabel.copy(color = palette.warning),
                        )
                    }
                }
            }

            // Statistika
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                StatCard(stringResource(Res.string.business_stat_active), "0", AppIcons.Tag, palette, Modifier.weight(1f))
                StatCard(stringResource(Res.string.business_stat_redemptions), "0", AppIcons.ScanFace, palette, Modifier.weight(1f))
                StatCard(stringResource(Res.string.business_stat_views), "0", AppIcons.Users, palette, Modifier.weight(1f))
            }

            // Biznes ma'lumotlari — qatorlar orasida nozik ajratgich, oxirgisida yo'q.
            GlassCard(
                shape = AppRadius.card,
                contentPadding = PaddingValues(horizontal = RowPaddingHorizontal, vertical = 6.dp),
                palette = palette,
            ) {
                InfoRow(
                    AppIcons.Home,
                    stringResource(Res.string.business_info_type),
                    businessTypeText,
                    palette,
                    showDivider = true,
                )
                if (phone != null) {
                    InfoRow(
                        AppIcons.Phone,
                        stringResource(Res.string.business_field_phone),
                        phone,
                        palette,
                        showDivider = true,
                    )
                }
                if (email != null) {
                    InfoRow(
                        AppIcons.Mail,
                        stringResource(Res.string.business_info_email),
                        email,
                        palette,
                        showDivider = true,
                    )
                }
                InfoRow(
                    AppIcons.Clock,
                    stringResource(Res.string.business_info_hours),
                    stringResource(Res.string.business_info_hours_unset),
                    palette,
                )
            }

            // Menyu
            AccountRow(AppIcons.Tag, stringResource(Res.string.business_menu_listings), palette, onClick = onOpenListings)
            AccountRow(AppIcons.Settings, stringResource(Res.string.business_menu_settings), palette, onClick = onOpenSettings)

            // Chiqish
            Row(
                Modifier.fillMaxWidth()
                    .clip(AppRadius.row)
                    .background(palette.dangerBg)
                    .clickable { vm.logout(onLoggedOut) }
                    .padding(horizontal = RowPaddingHorizontal, vertical = RowPaddingVertical),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Icon(AppIcons.LogOut, null, tint = palette.danger, modifier = Modifier.size(AppSize.iconMd))
                Text(
                    stringResource(Res.string.common_logout),
                    style = AppType.rowTitle.copy(color = palette.danger),
                )
            }
            Spacer(Modifier.height(AppSpacing.section))
        }
    }
}

/** Biznes turiga mos ikonka (mavjud ikonkalar ichidan). Tur id'i bo'yicha — tarjimaga bog'liq emas. */
private fun typeIcon(type: String): ImageVector = when {
    type.contains("O'quv", ignoreCase = true) -> AppIcons.GraduationCap
    type.contains("Kino", ignoreCase = true) -> AppIcons.Star
    type.contains("Texnika", ignoreCase = true) -> AppIcons.Building
    else -> AppIcons.Store
}
