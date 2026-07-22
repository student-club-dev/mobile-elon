package dev.feature.auth.presentation.main

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import dev.core.domain.model.ThemeMode
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.GlassRow
import dev.core.uikit.component.IconTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_notifications_title
import dev.core.uikit.resources.auth_settings_about
import dev.core.uikit.resources.auth_settings_about_body
import dev.core.uikit.resources.auth_settings_email
import dev.core.uikit.resources.auth_settings_push
import dev.core.uikit.resources.auth_settings_section_account
import dev.core.uikit.resources.auth_settings_section_general
import dev.core.uikit.resources.auth_settings_section_theme
import dev.core.uikit.resources.auth_settings_title
import dev.core.uikit.resources.auth_settings_version
import dev.core.uikit.resources.auth_theme_dark
import dev.core.uikit.resources.auth_theme_light
import dev.core.uikit.resources.auth_theme_system
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_logout
import dev.core.uikit.resources.profile_edit_action
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.profile.presentation.ProfileViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Sozlamalar ekrani (A3/C3). Hisob, bildirishnoma va ilova ma'lumotlari.
 *
 * Eslatma: bildirishnoma toggle'lari hozircha faqat UI holatida (local `remember`).
 * Doimiy saqlash va mavzu/til almashtirish uchun alohida `SettingsStore` + theme
 * override kerak — bu IMPLEMENTATION_PLAN.md dagi "SettingsStore" bandida bajariladi.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onLoggedOut: () -> Unit,
    vm: ProfileViewModel = koinViewModel(),
    settingsVm: SettingsViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    val settings by settingsVm.state.collectAsStateWithLifecycle()

    var aboutExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 54.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            BackButton(onBack, contentDescription = stringResource(Res.string.common_back), iconSize = 18.dp)
            Text(
                stringResource(Res.string.auth_settings_title),
                style = AppType.screenTitle.copy(fontSize = 20.sp, letterSpacing = 0.sp, color = palette.ink),
            )
        }
        Spacer(Modifier.height(18.dp))

        Column(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            SectionTitle(stringResource(Res.string.auth_settings_section_account), palette)
            SettingRow(AppIcons.Pencil, stringResource(Res.string.profile_edit_action), state.name, palette, onClick = onEditProfile)

            Spacer(Modifier.height(6.dp))
            SectionTitle(stringResource(Res.string.auth_settings_section_theme), palette)
            ThemeSelector(settings.themeMode, palette) { settingsVm.setThemeMode(it) }

            Spacer(Modifier.height(6.dp))
            SectionTitle(stringResource(Res.string.auth_notifications_title), palette)
            ToggleRow(AppIcons.Bell, stringResource(Res.string.auth_settings_push), settings.pushEnabled, palette) {
                settingsVm.setPush(it)
            }
            ToggleRow(AppIcons.Mail, stringResource(Res.string.auth_settings_email), settings.emailEnabled, palette) {
                settingsVm.setEmail(it)
            }

            Spacer(Modifier.height(6.dp))
            SectionTitle(stringResource(Res.string.auth_settings_section_general), palette)
            SettingRow(
                AppIcons.ShieldCheck,
                stringResource(Res.string.auth_settings_about),
                if (aboutExpanded) stringResource(Res.string.auth_settings_version) else null,
                palette,
            ) { aboutExpanded = !aboutExpanded }
            if (aboutExpanded) {
                Text(
                    stringResource(Res.string.auth_settings_about_body),
                    style = AppType.fieldLabel.copy(fontWeight = AppType.subtitle.fontWeight, color = palette.inkMuted),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().clip(AppRadius.lg).background(palette.dangerBg)
                    .clickable { vm.logout(onLoggedOut) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(AppIcons.LogOut, null, tint = palette.danger, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(Res.string.common_logout),
                    style = AppType.label.copy(fontSize = 13.5f.sp, fontWeight = AppType.button.fontWeight, color = palette.danger),
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ThemeSelector(current: ThemeMode, palette: AppPalette, onSelect: (ThemeMode) -> Unit) {
    val options = listOf(
        ThemeMode.SYSTEM to stringResource(Res.string.auth_theme_system),
        ThemeMode.LIGHT to stringResource(Res.string.auth_theme_light),
        ThemeMode.DARK to stringResource(Res.string.auth_theme_dark),
    )
    GlassRow(
        shape = AppRadius.lg,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        palette = palette,
    ) {
        options.forEach { (mode, label) ->
            val active = mode == current
            Box(
                Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(11.dp))
                    .background(if (active) palette.primary.copy(alpha = 0.16f) else Color.Transparent)
                    .clickable { onSelect(mode) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = AppType.label.copy(
                        fontSize = 12.5f.sp,
                        color = if (active) palette.primary else palette.inkMuted,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, palette: AppPalette) {
    Text(
        text,
        style = AppType.label.copy(fontSize = 12.5f.sp, color = palette.inkFaint),
        modifier = Modifier.padding(start = AppSpacing.xs, top = 2.dp, bottom = 2.dp),
    )
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, trailing: String?, palette: AppPalette, onClick: () -> Unit) {
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
private fun ToggleRow(icon: ImageVector, title: String, checked: Boolean, palette: AppPalette, onToggle: (Boolean) -> Unit) {
    GlassRow(
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        onClick = { onToggle(!checked) },
        palette = palette,
    ) {
        IconTile(icon, tint = palette.primary)
        Text(
            title,
            style = AppType.label.copy(fontSize = 13.5f.sp, color = palette.ink),
            modifier = Modifier.weight(1f),
        )
        SwitchTrack(checked, palette)
    }
}

/** Oddiy tugmacha — Material Switch o'rniga ilova uslubidagi variant. */
@Composable
private fun SwitchTrack(checked: Boolean, palette: AppPalette) {
    val knobOffset by animateDpAsState(if (checked) 20.dp else 2.dp)
    Box(
        Modifier.width(44.dp).height(26.dp).clip(AppRadius.pill)
            .background(if (checked) palette.primary else palette.inkFaint.copy(alpha = 0.35f)),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Tugmacha to'ldirilgan yo'lakcha USTIDA — har ikkala rejimda oq.
        Box(Modifier.padding(start = knobOffset).size(22.dp).clip(AppRadius.pill).background(palette.onPrimary))
    }
}
