package dev.feature.business

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import dev.feature.business.components.StatCard
import dev.feature.profile.presentation.ProfileViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Biznesmen profili — ideal ko'rinish. Talaba `ProfileScreen`iga aloqasi YO'Q. Biznes kartasi
 * (tur + tasdiq + reyting), statistika, biznes ma'lumotlari, menyu va chiqishdan iborat.
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
        GradientHeader(palette = palette) {
            Row(
                Modifier.padding(start = 20.dp, end = 20.dp, top = 54.dp, bottom = AppSpacing.screenBottom),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
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
                    style = AppType.screenTitle.copy(fontSize = 20.sp, color = Color.White),
                )
                HeaderIconButton(
                    AppIcons.Pencil,
                    onClick = onEdit,
                    contentDescription = stringResource(Res.string.common_edit),
                    iconSize = 17.dp,
                )
            }
        }

        Column(Modifier.fillMaxWidth().padding(AppSpacing.lg)) {
            // Biznes kartasi
            GlassRow(
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(AppSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                palette = palette,
            ) {
                IconTile(
                    typeIcon(businessTypeId.orEmpty()),
                    tint = palette.primary,
                    background = palette.primary.copy(alpha = 0.14f),
                    size = 62.dp,
                    iconSize = 30.dp,
                    shape = AppRadius.card,
                )
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            businessName,
                            style = AppType.sectionTitle.copy(
                                fontWeight = AppType.screenTitle.fontWeight,
                                color = palette.ink,
                            ),
                        )
                        Icon(
                            AppIcons.ShieldCheck,
                            stringResource(Res.string.business_verified),
                            tint = palette.successDeep,
                            modifier = Modifier.size(AppSize.iconSm),
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(businessTypeText, style = AppType.fieldLabel.copy(color = palette.inkMuted))
                    Spacer(Modifier.height(5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(AppIcons.Star, null, tint = palette.amber, modifier = Modifier.size(13.dp))
                        Text(
                            stringResource(Res.string.business_rating_new),
                            style = AppType.caption.copy(
                                fontWeight = AppType.bodyStrong.fontWeight,
                                color = palette.inkFaint,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Statistika
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(stringResource(Res.string.business_stat_active), "0", AppIcons.Tag, palette, Modifier.weight(1f))
                StatCard(stringResource(Res.string.business_stat_redemptions), "0", AppIcons.ScanFace, palette, Modifier.weight(1f))
                StatCard(stringResource(Res.string.business_stat_views), "0", AppIcons.Users, palette, Modifier.weight(1f))
            }

            Spacer(Modifier.height(14.dp))

            // Biznes ma'lumotlari
            GlassCard(shape = AppRadius.card, contentPadding = PaddingValues(AppSpacing.xs), palette = palette) {
                InfoRow(AppIcons.Store, stringResource(Res.string.business_info_type), businessTypeText, palette)
                if (phone != null) InfoRow(AppIcons.Phone, stringResource(Res.string.business_field_phone), phone, palette)
                if (email != null) InfoRow(AppIcons.Mail, stringResource(Res.string.business_info_email), email, palette)
                InfoRow(
                    AppIcons.Clock,
                    stringResource(Res.string.business_info_hours),
                    stringResource(Res.string.business_info_hours_unset),
                    palette,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Menyu
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                AccountRow(AppIcons.Tag, stringResource(Res.string.business_menu_listings), palette, onClick = onOpenListings)
                AccountRow(AppIcons.Settings, stringResource(Res.string.business_menu_settings), palette, onClick = onOpenSettings)
            }

            Spacer(Modifier.height(AppSpacing.lg))

            // Chiqish
            Row(
                Modifier.fillMaxWidth()
                    .clip(AppRadius.lg)
                    .background(palette.dangerBg)
                    .clickable { vm.logout(onLoggedOut) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(AppIcons.LogOut, null, tint = palette.danger, modifier = Modifier.size(AppSize.iconMd))
                Text(
                    stringResource(Res.string.common_logout),
                    style = AppType.label.copy(
                        fontSize = 13.5f.sp,
                        fontWeight = AppType.button.fontWeight,
                        color = palette.danger,
                    ),
                )
            }
            Spacer(Modifier.height(AppSpacing.xl))
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
