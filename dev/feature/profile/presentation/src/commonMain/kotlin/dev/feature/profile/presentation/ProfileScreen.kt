package dev.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.Ad
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GlassRow
import dev.core.uikit.component.GradientHeader
import dev.core.uikit.component.IconTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_cancel
import dev.core.uikit.resources.common_delete
import dev.core.uikit.resources.common_edit
import dev.core.uikit.resources.common_logout
import dev.core.uikit.resources.profile_ad_delete_confirm
import dev.core.uikit.resources.profile_ad_delete_title
import dev.core.uikit.resources.profile_application_status_interview
import dev.core.uikit.resources.profile_application_status_rejected
import dev.core.uikit.resources.profile_application_status_sent
import dev.core.uikit.resources.profile_application_status_viewed
import dev.core.uikit.resources.profile_edit_action
import dev.core.uikit.resources.profile_my_business_subtitle
import dev.core.uikit.resources.profile_my_business_title
import dev.core.uikit.resources.profile_section_applications
import dev.core.uikit.resources.profile_section_my_ads
import dev.core.uikit.resources.profile_section_saved
import dev.core.uikit.resources.profile_settings
import dev.core.uikit.resources.profile_title
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.profile.presentation.components.ProfileAvatar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Profil ichidagi ro'yxat bo'limlari. Sarlavha matni resursdan olinadi. */
private enum class ProfileSection(val title: StringResource) {
    MY_ADS(Res.string.profile_section_my_ads),
    SAVED(Res.string.profile_section_saved),
    APPLICATIONS(Res.string.profile_section_applications),
}

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onEditProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onEditAd: (String) -> Unit = {},
    /** "Mening biznesim" — chegirma e'lonlari (feature:discounts). */
    onOpenMyBusiness: () -> Unit = {},
    /** Talaba shell'ida biznes kartasi yashiriladi — biznesmenda o'zining alohida bo'limi bor. */
    showMyBusiness: Boolean = true,
    vm: ProfileViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    var section by remember { mutableStateOf<ProfileSection?>(null) }

    if (section != null) {
        SectionList(section!!, state, palette, onBack = { section = null }, onDeleteAd = vm::deleteAd, onEditAd = onEditAd)
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        GradientHeader(palette = palette) {
            Row(
                Modifier.padding(start = 20.dp, end = 20.dp, top = 54.dp, bottom = AppSpacing.xl),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Gradient ustidagi tugma — palitra emas, oq shaffof fon (har ikkala rejimda ham).
                Box(
                    Modifier.size(40.dp).clip(AppRadius.md)
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppIcons.ArrowLeft,
                        stringResource(Res.string.common_back),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(AppSpacing.md))
                Text(
                    stringResource(Res.string.profile_title),
                    style = AppType.screenTitle.copy(fontSize = 20.sp, color = Color.White),
                )
            }
        }

        Column(Modifier.fillMaxWidth().padding(AppSpacing.lg)) {
            // Profil kartasi
            GlassRow(shape = AppRadius.card, palette = palette) {
                ProfileAvatar(
                    name = state.name,
                    size = 60.dp,
                    fontSize = 24.sp,
                    palette = palette,
                    avatarUrl = state.profile?.avatarUrl,
                )
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(state.name, style = AppType.screenTitle.copy(fontSize = 16.sp, color = palette.ink))
                        Icon(AppIcons.ShieldCheck, null, tint = palette.successDeep, modifier = Modifier.size(15.dp))
                    }
                    val sub = listOfNotNull(state.universityMonogram, state.courseLabel)
                        .joinToString(" · ")
                        .ifBlank { state.contact }
                    Text(sub, style = AppType.hint.copy(color = palette.inkFaint))
                    Spacer(Modifier.height(AppSpacing.xs))
                    Row(
                        Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onEditProfile),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(Res.string.profile_edit_action),
                            style = AppType.hint.copy(fontWeight = AppType.fieldLabel.fontWeight, color = palette.primary),
                        )
                        Icon(AppIcons.ChevronRight, null, tint = palette.primary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(AppSpacing.lg))

            // Mening biznesim — chegirma e'lonlarini shu yerdan qo'yiladi.
            // Faqat biznes egasida ko'rinadi (talaba shell'ida yashirinadi — showMyBusiness=false).
            if (showMyBusiness) {
                val highlightShape = RoundedCornerShape(16.dp)
                Row(
                    Modifier.fillMaxWidth()
                        .clip(highlightShape)
                        .background(palette.primary.copy(alpha = 0.10f))
                        .border(1.dp, palette.primary.copy(alpha = 0.22f), highlightShape)
                        .clickable(onClick = onOpenMyBusiness)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    IconTile(
                        AppIcons.Store,
                        tint = palette.primary,
                        background = palette.primary.copy(alpha = 0.16f),
                        size = 40.dp,
                        iconSize = 19.dp,
                        shape = AppRadius.md,
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(Res.string.profile_my_business_title),
                            style = AppType.bodyStrong.copy(fontWeight = AppType.button.fontWeight, color = palette.ink),
                        )
                        Text(
                            stringResource(Res.string.profile_my_business_subtitle),
                            style = AppType.hint.copy(color = palette.inkFaint),
                        )
                    }
                    Icon(AppIcons.ChevronRight, null, tint = palette.primary, modifier = Modifier.size(17.dp))
                }

                Spacer(Modifier.height(AppSpacing.lg))
            }

            // Menyu
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                MenuRow(AppIcons.FileText, stringResource(ProfileSection.MY_ADS.title), "${state.myAds.size}", palette) { section = ProfileSection.MY_ADS }
                MenuRow(AppIcons.Bookmark, stringResource(ProfileSection.SAVED.title), "${state.savedDiscounts.size}", palette) { section = ProfileSection.SAVED }
                MenuRow(AppIcons.Briefcase, stringResource(ProfileSection.APPLICATIONS.title), "${state.applications.size}", palette) { section = ProfileSection.APPLICATIONS }
                MenuRow(AppIcons.Settings, stringResource(Res.string.profile_settings), null, palette, onClick = onOpenSettings)
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
                Icon(AppIcons.LogOut, null, tint = palette.danger, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(Res.string.common_logout),
                    style = AppType.label.copy(fontSize = 13.5f.sp, fontWeight = AppType.button.fontWeight, color = palette.danger),
                )
            }
            Spacer(Modifier.height(AppSpacing.xl))
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    trailing: String?,
    palette: AppPalette,
    onClick: () -> Unit,
) {
    GlassRow(
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        onClick = onClick,
        palette = palette,
    ) {
        IconTile(icon, tint = palette.primary)
        Text(
            title,
            style = AppType.label.copy(fontSize = 13.5f.sp, color = palette.ink),
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(trailing, style = AppType.fieldLabel.copy(color = palette.inkFaint))
            Spacer(Modifier.width(6.dp))
        }
        Icon(AppIcons.ChevronRight, null, tint = palette.inkFaint, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun SectionList(
    section: ProfileSection,
    state: ProfileUiState,
    palette: AppPalette,
    onBack: () -> Unit,
    onDeleteAd: (String) -> Unit,
    onEditAd: (String) -> Unit,
) {
    var adToDelete by remember { mutableStateOf<Ad?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 54.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                Modifier.size(40.dp).clip(AppRadius.md)
                    .background(palette.glass)
                    .border(1.dp, palette.border, AppRadius.md)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    AppIcons.ArrowLeft,
                    stringResource(Res.string.common_back),
                    tint = palette.ink,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(stringResource(section.title), style = AppType.screenTitle.copy(fontSize = 18.sp, color = palette.ink))
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = AppSpacing.lg, end = AppSpacing.lg, bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            when (section) {
                ProfileSection.MY_ADS -> items(state.myAds, key = { it.id }) { ad ->
                    DeletableAdRow(
                        title = ad.title,
                        subtitle = "${ad.category} · ${ad.price}",
                        trailing = ad.createdAgo,
                        palette = palette,
                        onEdit = { onEditAd(ad.id) },
                        onDelete = { adToDelete = ad },
                    )
                }
                ProfileSection.SAVED -> items(state.savedDiscounts, key = { it.id }) {
                    SimpleRow("${it.merchant} — ${it.title}", "−${it.discountPercent}%", it.expiry ?: "", palette)
                }
                ProfileSection.APPLICATIONS -> items(state.applications, key = { it.id }) {
                    SimpleRow(it.jobTitle, it.company, statusLabel(it.status.name), palette)
                }
            }
        }
    }

    val target = adToDelete
    if (target != null) {
        AlertDialog(
            onDismissRequest = { adToDelete = null },
            title = {
                Text(stringResource(Res.string.profile_ad_delete_title), style = AppType.sectionTitle.copy(color = palette.ink))
            },
            text = {
                Text(
                    stringResource(Res.string.profile_ad_delete_confirm, target.title),
                    style = AppType.body.copy(color = palette.inkMuted),
                )
            },
            confirmButton = {
                TextButton(onClick = { onDeleteAd(target.id); adToDelete = null }) {
                    Text(stringResource(Res.string.common_delete), style = AppType.buttonSecondary.copy(color = palette.danger))
                }
            },
            dismissButton = {
                TextButton(onClick = { adToDelete = null }) {
                    Text(stringResource(Res.string.common_cancel), style = AppType.label.copy(color = palette.inkMuted))
                }
            },
        )
    }
}

@Composable
private fun DeletableAdRow(
    title: String,
    subtitle: String,
    trailing: String,
    palette: AppPalette,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), palette = palette) {
        Column(Modifier.weight(1f)) {
            Text(title, style = AppType.label.copy(fontSize = 13.5f.sp, fontWeight = AppType.button.fontWeight, color = palette.ink))
            Text(subtitle, style = AppType.hint.copy(color = palette.inkFaint))
            if (trailing.isNotBlank()) {
                Text(trailing, style = AppType.caption.copy(fontSize = 10.5f.sp, fontWeight = AppType.fieldLabel.fontWeight, color = palette.primary))
            }
        }
        IconTile(
            AppIcons.Pencil,
            contentDescription = stringResource(Res.string.common_edit),
            tint = palette.primary,
            size = 34.dp,
            iconSize = 15.dp,
            onClick = onEdit,
        )
        IconTile(
            AppIcons.Close,
            contentDescription = stringResource(Res.string.common_delete),
            tint = palette.danger,
            background = palette.dangerBg,
            size = 34.dp,
            iconSize = 16.dp,
            onClick = onDelete,
        )
    }
}

@Composable
private fun SimpleRow(title: String, subtitle: String, trailing: String, palette: AppPalette) {
    GlassRow(palette = palette) {
        Column(Modifier.weight(1f)) {
            Text(title, style = AppType.label.copy(fontSize = 13.5f.sp, fontWeight = AppType.button.fontWeight, color = palette.ink))
            Text(subtitle, style = AppType.hint.copy(color = palette.inkFaint))
        }
        if (trailing.isNotBlank()) {
            Text(trailing, style = AppType.caption.copy(fontWeight = AppType.fieldLabel.fontWeight, color = palette.primary))
        }
    }
}

@Composable
private fun statusLabel(status: String): String = when (status) {
    "SENT" -> stringResource(Res.string.profile_application_status_sent)
    "VIEWED" -> stringResource(Res.string.profile_application_status_viewed)
    "INTERVIEW" -> stringResource(Res.string.profile_application_status_interview)
    "REJECTED" -> stringResource(Res.string.profile_application_status_rejected)
    else -> status
}
