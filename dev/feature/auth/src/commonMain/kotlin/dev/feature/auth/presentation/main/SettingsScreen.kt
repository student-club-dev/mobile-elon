package dev.feature.auth.presentation.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.ThemeMode
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import dev.feature.profile.presentation.ProfileViewModel
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
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(palette.glass)
                    .border(1.dp, palette.border, RoundedCornerShape(12.dp)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(AppIcons.ArrowLeft, "Orqaga", tint = palette.ink, modifier = Modifier.size(18.dp)) }
            Text("Sozlamalar", style = TextStyle(fontFamily = AppFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Black, color = palette.ink))
        }
        Spacer(Modifier.height(18.dp))

        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            SectionTitle("Hisob", palette)
            SettingRow(AppIcons.Pencil, "Profilni tahrirlash", state.name, palette, onClick = onEditProfile)

            Spacer(Modifier.height(6.dp))
            SectionTitle("Mavzu", palette)
            ThemeSelector(settings.themeMode, palette) { settingsVm.setThemeMode(it) }

            Spacer(Modifier.height(6.dp))
            SectionTitle("Bildirishnomalar", palette)
            ToggleRow(AppIcons.Bell, "Push bildirishnomalar", settings.pushEnabled, palette) { settingsVm.setPush(it) }
            ToggleRow(AppIcons.Mail, "Email xabarnomalar", settings.emailEnabled, palette) { settingsVm.setEmail(it) }

            Spacer(Modifier.height(6.dp))
            SectionTitle("Umumiy", palette)
            SettingRow(AppIcons.ShieldCheck, "Ilova haqida", if (aboutExpanded) "Versiya 1.0.0" else null, palette) { aboutExpanded = !aboutExpanded }
            if (aboutExpanded) {
                Text(
                    "StudentClubs — talabalar uchun super-app: chegirmalar, ishlar, e'lonlar va xabarlar.\nVersiya 1.0.0",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, color = palette.inkMuted),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFDC2626).copy(alpha = 0.10f)).clickable { vm.logout(onLoggedOut) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(AppIcons.LogOut, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                Text("Chiqish", style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626)))
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ThemeSelector(current: ThemeMode, palette: AppPalette, onSelect: (ThemeMode) -> Unit) {
    val options = listOf(
        ThemeMode.SYSTEM to "Tizim",
        ThemeMode.LIGHT to "Yorug'",
        ThemeMode.DARK to "Tungi",
    )
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(14.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (mode, label) ->
            val active = mode == current
            Box(
                Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(11.dp))
                    .background(if (active) palette.primary.copy(alpha = 0.16f) else Color.Transparent)
                    .clickable { onSelect(mode) },
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = if (active) palette.primary else palette.inkMuted))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, palette: AppPalette) {
    Text(text, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = palette.inkFaint), modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp))
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, trailing: String?, palette: AppPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(palette.primary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = palette.primary, modifier = Modifier.size(17.dp))
        }
        Text(title, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.Bold, color = palette.ink), modifier = Modifier.weight(1f))
        if (trailing != null) {
            Text(trailing, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.inkFaint))
            Spacer(Modifier.width(6.dp))
        }
        Icon(AppIcons.ChevronRight, null, tint = palette.inkFaint, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun ToggleRow(icon: ImageVector, title: String, checked: Boolean, palette: AppPalette, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(14.dp)).clickable { onToggle(!checked) }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(palette.primary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = palette.primary, modifier = Modifier.size(17.dp))
        }
        Text(title, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.Bold, color = palette.ink), modifier = Modifier.weight(1f))
        SwitchTrack(checked, palette)
    }
}

@Composable
private fun SwitchTrack(checked: Boolean, palette: AppPalette) {
    val knobOffset by animateDpAsState(if (checked) 20.dp else 2.dp)
    Box(
        Modifier.width(44.dp).height(26.dp).clip(RoundedCornerShape(999.dp))
            .background(if (checked) palette.primary else palette.inkFaint.copy(alpha = 0.35f)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.padding(start = knobOffset).size(22.dp).clip(RoundedCornerShape(999.dp)).background(Color.White))
    }
}
