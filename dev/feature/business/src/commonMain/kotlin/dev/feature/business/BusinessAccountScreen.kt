package dev.feature.business

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import dev.feature.profile.presentation.ProfileViewModel
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
    val businessType = state.profile?.businessType?.takeIf { it.isNotBlank() } ?: "Biznes"
    val phone = state.profile?.phoneNumber?.takeIf { it.isNotBlank() } ?: state.contact.takeIf { it.isNotBlank() }
    val email = state.profile?.email?.takeIf { it.isNotBlank() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Gradient sarlavha
        Box(
            Modifier.fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Color(0xFF6C47FF), Color(0xFF7C4DFF), Color(0xFF5B34D6))),
                    RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                )
                .padding(start = 20.dp, end = 20.dp, top = 54.dp, bottom = 26.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.ArrowLeft, "Orqaga", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text(
                    "Biznes profili",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White),
                )
                // Tahrirlash
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.Pencil, "Tahrirlash", tint = Color.White, modifier = Modifier.size(17.dp))
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            // Biznes kartasi
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(palette.glass)
                    .border(1.dp, palette.border, RoundedCornerShape(20.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    Modifier.size(62.dp).clip(RoundedCornerShape(18.dp)).background(palette.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(typeIcon(businessType), null, tint = palette.primary, modifier = Modifier.size(30.dp))
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            businessName,
                            style = TextStyle(fontFamily = AppFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Black, color = palette.ink),
                        )
                        Icon(AppIcons.ShieldCheck, "Tasdiqlangan", tint = palette.successDeep, modifier = Modifier.size(15.dp))
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        businessType,
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.inkMuted),
                    )
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(AppIcons.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                        Text(
                            "Yangi biznes",
                            style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = palette.inkFaint),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Statistika
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Faol e'lonlar", "0", AppIcons.Tag, palette, Modifier.weight(1f))
                StatCard("Foydalanishlar", "0", AppIcons.ScanFace, palette, Modifier.weight(1f))
                StatCard("Ko'rishlar", "0", AppIcons.Users, palette, Modifier.weight(1f))
            }

            Spacer(Modifier.height(14.dp))

            // Biznes ma'lumotlari
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(palette.glass)
                    .border(1.dp, palette.border, RoundedCornerShape(18.dp)).padding(4.dp),
            ) {
                InfoRow(AppIcons.Store, "Biznes turi", businessType, palette)
                if (phone != null) InfoRow(AppIcons.Phone, "Telefon", phone, palette)
                if (email != null) InfoRow(AppIcons.Mail, "Email", email, palette)
                InfoRow(AppIcons.Clock, "Ish vaqti", "Belgilanmagan", palette)
            }

            Spacer(Modifier.height(14.dp))

            // Menyu
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                AccountRow(AppIcons.Tag, "Mening e'lonlarim", palette, onClick = onOpenListings)
                AccountRow(AppIcons.Settings, "Sozlamalar", palette, onClick = onOpenSettings)
            }

            Spacer(Modifier.height(16.dp))

            // Chiqish
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFDC2626).copy(alpha = 0.10f))
                    .clickable { vm.logout(onLoggedOut) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(AppIcons.LogOut, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                Text("Chiqish", style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626)))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Biznes turiga mos ikonka (mavjud ikonkalar ichidan). */
private fun typeIcon(type: String): ImageVector = when {
    type.contains("O'quv", ignoreCase = true) -> AppIcons.GraduationCap
    type.contains("Kino", ignoreCase = true) -> AppIcons.Star
    type.contains("Texnika", ignoreCase = true) -> AppIcons.Building
    else -> AppIcons.Store
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, palette: AppPalette, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(palette.glass)
            .border(1.dp, palette.border, RoundedCornerShape(16.dp)).padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, null, tint = palette.primary, modifier = Modifier.size(18.dp))
        Text(value, style = TextStyle(fontFamily = AppFontFamily, fontSize = 19.sp, fontWeight = FontWeight.Black, color = palette.ink))
        Text(label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = palette.inkFaint))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, palette: AppPalette) {
    Row(
        Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = palette.primary, modifier = Modifier.size(18.dp))
        Text(label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, color = palette.inkFaint), modifier = Modifier.weight(1f))
        Text(value, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = palette.ink))
    }
}

@Composable
private fun AccountRow(icon: ImageVector, title: String, palette: AppPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.glass)
            .border(1.dp, palette.border, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = palette.ink, modifier = Modifier.size(19.dp))
        Text(title, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.Bold, color = palette.ink), modifier = Modifier.weight(1f))
        Icon(AppIcons.ChevronRight, null, tint = palette.chevron, modifier = Modifier.size(16.dp))
    }
}
