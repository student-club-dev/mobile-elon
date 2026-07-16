package dev.feature.business

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.AppScreenScaffold
import dev.core.designsystem.components.BackButton
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.components.HintText
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.components.ScreenTitle
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette

/**
 * Biznesmen uchun ALOHIDA kirish ekrani. Sof UI — holat/callbacklarni parametr sifatida oladi
 * (auth moduliga bog'lanmaydi).
 */
@Composable
fun BusinessWelcomeScreen(
    phone: String,
    onPhoneChange: (String) -> Unit,
    phoneValid: Boolean,
    isLoading: Boolean,
    onBack: () -> Unit,
    onGetCode: () -> Unit,
    onGoogle: () -> Unit,
    onEmail: () -> Unit,
) {
    val palette = appPalette

    AppScreenScaffold(scroll = false, horizontalPadding = 20, topPadding = 54) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            BackButton(onBack)
            ScreenTitle("Biznes uchun kirish", size = 21)
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "Biznes egasi sifatida kiring — chegirma e'lonlaringizni joylang va boshqaring.",
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, color = palette.inkMuted),
        )

        // Premium hero — biznes belgisi
        Spacer(Modifier.height(22.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(76.dp).clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF6C47FF), Color(0xFF7C4DFF), Color(0xFF5B34D6)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.Store, null, tint = Color.White, modifier = Modifier.size(38.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Telefon raqami",
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.label),
        )
        Spacer(Modifier.height(8.dp))
        GlassTextField(
            value = phone,
            onValueChange = onPhoneChange,
            placeholder = "90 123 45 67",
            leadingContent = { PhonePrefix(palette) },
            height = 50,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )

        Spacer(Modifier.height(16.dp))
        PrimaryButton("Kod olish", onGetCode, enabled = phoneValid && !isLoading)

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth().height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(palette.glass)
                .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                .clickable(enabled = !isLoading, onClick = onGoogle),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(AppIcons.Google, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(9.dp))
            Text(
                "Google bilan kirish",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.ink),
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Email bilan kirish",
            modifier = Modifier.fillMaxWidth().height(22.dp).clickable(onClick = onEmail),
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.primary, textAlign = TextAlign.Center),
        )

        Spacer(Modifier.height(14.dp))
        HintText("Talaba sifatida kirmoqchimisiz? Orqaga qayting va \"Talaba\" ni tanlang.")
    }
}

/** Telefon maydonining "🇺🇿 +998 |" prefiksi (auth'dagining nusxasi — modul mustaqil bo'lishi uchun). */
@Composable
private fun PhonePrefix(palette: AppPalette) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "🇺🇿 +998",
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.ink),
        )
        Spacer(Modifier.width(9.dp))
        Box(Modifier.width(1.dp).height(22.dp).background(palette.border))
    }
}
