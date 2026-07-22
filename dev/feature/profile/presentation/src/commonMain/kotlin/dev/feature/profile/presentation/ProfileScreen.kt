package dev.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GlassRow
import dev.core.uikit.component.GradientHeader
import dev.core.uikit.component.IconTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_logout
import dev.core.uikit.resources.profile_edit_action
import dev.core.uikit.resources.profile_my_business_subtitle
import dev.core.uikit.resources.profile_my_business_title
import dev.core.uikit.resources.profile_settings
import dev.core.uikit.resources.profile_title
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.profile.presentation.components.ProfileAvatar
import dev.feature.profile.presentation.components.ProfileSection
import dev.feature.profile.presentation.components.SectionList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import dev.core.uikit.component.AppBackHandler

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

    // Bo'lim ro'yxati ochiq bo'lsa "orqaga" profilga qaytaradi, ekrandan chiqarmaydi.
    AppBackHandler(enabled = section != null) { section = null }

    if (section != null) {
        SectionList(section!!, state, palette, onBack = { section = null }, onDeleteAd = vm::deleteAd, onEditAd = onEditAd)
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        GradientHeader(palette = palette) {
            Row(
                Modifier.padding(
                    start = AppSpacing.screenHorizontal,
                    end = AppSpacing.screenHorizontal,
                    top = 54.dp,
                    bottom = AppSpacing.xl,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Gradient ustidagi tugma — palitra emas, oq shaffof fon (har ikkala rejimda ham).
                Box(
                    Modifier.size(AppSize.iconButton).clip(AppRadius.md)
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
                    style = AppType.topBarTitle.copy(color = Color.White),
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
                        Text(state.name, style = AppType.cardTitle.copy(color = palette.ink))
                        Icon(AppIcons.ShieldCheck, null, tint = palette.success, modifier = Modifier.size(15.dp))
                    }
                    val sub = listOfNotNull(state.universityMonogram, state.courseLabel)
                        .joinToString(" · ")
                        .ifBlank { state.contact }
                    Text(sub, style = AppType.hint.copy(color = palette.inkFaint))
                    Spacer(Modifier.height(AppSpacing.xs))
                    Row(
                        Modifier.clip(AppRadius.sm).clickable(onClick = onEditProfile),
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
                // Urg'uli qator — ochiq ko'k aksent yuzasi, chegara yo'q.
                Row(
                    Modifier.fillMaxWidth()
                        .clip(AppRadius.md)
                        .background(palette.accentBg)
                        .clickable(onClick = onOpenMyBusiness)
                        .padding(AppSpacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    IconTile(
                        AppIcons.Store,
                        tint = palette.primary,
                        background = palette.card,
                        size = AppSize.iconButton,
                        iconSize = AppSize.iconMd,
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
                    .padding(AppSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(AppIcons.LogOut, null, tint = palette.danger, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(Res.string.common_logout),
                    style = AppType.subtitle.copy(fontWeight = AppType.button.fontWeight, color = palette.danger),
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
            style = AppType.subtitle.copy(fontWeight = AppType.label.fontWeight, color = palette.ink),
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(trailing, style = AppType.fieldLabel.copy(color = palette.inkFaint))
            Spacer(Modifier.width(6.dp))
        }
        Icon(AppIcons.ChevronRight, null, tint = palette.inkFaint, modifier = Modifier.size(17.dp))
    }
}
