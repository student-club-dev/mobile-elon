package dev.feature.business

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.theme.AppPalette

// Biznes turlari — horizontal scroll qilinadi. Bir nechta ekran ishlatadi.
internal val businessTypes = listOf(
    "Kafe va Restoran",
    "Oziq-ovqat",
    "Kiyim-kechak",
    "Game Club",
    "O'quv markazlar",
    "Kino va ko'ngilochar",
    "Texnikalar",
    "Sartaroshxona",
    "Go'zallik saloni",
    "Sport zal",
    "Avto xizmat",
    "Apteka",
    "Gul do'koni",
    "Boshqa",
)

/** Biznes turi uchun chip — tanlash ro'yxatlarida ishlatiladi. */
@Composable
internal fun TypeChip(label: String, active: Boolean, onClick: () -> Unit, palette: AppPalette) {
    Row(
        Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (active) palette.primary.copy(alpha = 0.14f) else palette.fieldBg)
            .border(if (active) 1.5.dp else 1.dp, if (active) palette.primary else palette.border, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            label,
            style = TextStyle(
                fontFamily = AppFontFamily,
                fontSize = 12.5f.sp,
                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (active) palette.primary else palette.inkMuted,
            ),
        )
    }
}
