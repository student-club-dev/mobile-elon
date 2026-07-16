package dev.feature.auth.presentation.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.AppScreenScaffold
import dev.core.designsystem.theme.ModuleFood
import dev.core.designsystem.theme.appPalette
import dev.feature.auth.presentation.flow.Role

/**
 * Login'dan OLDIN chiqadigan rol tanlash — foydalanuvchi Talaba yoki Biznesmen sifatida davom etadi.
 * Tanlangan rol [Role] sifatida qaytadi (AuthFlowViewModel'ga yoziladi) va ro'yxatdan o'tishda
 * ishlatiladi; login'dan keyin RootShell shu rol bo'yicha mos shell'ni ochadi.
 */
@Composable
fun RoleChoiceScreen(
    onPick: (Role) -> Unit,
    onBack: () -> Unit = {},
) {
    val palette = appPalette

    AppScreenScaffold {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 48.dp, bottom = 24.dp),
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
                    .background(palette.primary.copy(alpha = 0.10f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.ArrowLeft, "Orqaga", tint = palette.primary, modifier = Modifier.size(19.dp))
            }

            Spacer(Modifier.height(28.dp))
            Text(
                "Kim sifatida davom etamiz?",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 26.sp, fontWeight = FontWeight.Black, color = palette.ink),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Rolni tanlang — ilova siz uchun mos bo'limlarni ko'rsatadi. Keyinroq profildan o'zgartirish mumkin.",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, color = palette.inkMuted),
            )

            Spacer(Modifier.height(28.dp))
            RoleCard(
                title = "Talaba",
                desc = "Chegirmalar, ishlar, studentlar va e'lonlar",
                icon = AppIcons.GraduationCap,
                accent = palette.primary,
                onClick = { onPick(Role.STUDENT) },
                palette = palette,
            )
            Spacer(Modifier.height(14.dp))
            RoleCard(
                title = "Biznesmen",
                desc = "Chegirma e'lonlari qo'yish va boshqarish",
                icon = AppIcons.Store,
                accent = ModuleFood,
                onClick = { onPick(Role.BUSINESS) },
                palette = palette,
            )
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    desc: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    palette: dev.core.designsystem.theme.AppPalette,
) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(1.5.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, title, tint = accent, modifier = Modifier.size(26.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Black, color = palette.ink),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                desc,
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, color = palette.inkFaint),
            )
        }
        Icon(AppIcons.ChevronRight, null, tint = accent, modifier = Modifier.size(20.dp))
    }
}
