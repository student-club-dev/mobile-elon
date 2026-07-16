package dev.feature.business

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppScreenScaffold
import dev.core.designsystem.components.BackButton
import dev.core.designsystem.components.FieldLabel
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.components.ScreenTitle
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette

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

/**
 * Biznes egasi profilini to'ldirish — talabaning universitet/kurs ekrani O'RNIGA. Biznes nomi va
 * turi so'raladi; saqlangач BusinessShell ochiladi.
 */
@Composable
fun BusinessProfileScreen(
    businessName: String,
    businessType: String,
    error: String?,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    val palette = appPalette

    AppScreenScaffold(scroll = true, horizontalPadding = 20, topPadding = 54) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            BackButton(onBack)
            ScreenTitle("Biznes profili", size = 21)
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "Biznesingiz haqida qisqacha — bu chegirma e'lonlaringizda ko'rinadi.",
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, color = palette.inkMuted),
        )

        Spacer(Modifier.height(22.dp))
        FieldLabel("Biznes nomi")
        Spacer(Modifier.height(8.dp))
        GlassTextField(
            value = businessName,
            onValueChange = onNameChange,
            placeholder = "Masalan: Kafe Aurora",
            height = 50,
        )

        Spacer(Modifier.height(18.dp))
        FieldLabel("Biznes turi")
        Spacer(Modifier.height(8.dp))
        // Horizontal scroll — turlar bir qatorда suriladi.
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(businessTypes) { type ->
                TypeChip(type, businessType == type, { onTypeChange(type) }, palette)
            }
        }

        if (error != null) {
            Spacer(Modifier.height(6.dp))
            Text(error, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, color = Color(0xFFE5484D)))
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            "Davom etish",
            onStart,
            enabled = businessName.isNotBlank() && businessType.isNotBlank() && !isLoading,
        )
    }
}

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
