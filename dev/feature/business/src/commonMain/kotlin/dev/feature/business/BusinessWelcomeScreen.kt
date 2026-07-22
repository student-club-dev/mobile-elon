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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.HintText
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenSubtitle
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.business_welcome_dev_login
import dev.core.uikit.resources.business_welcome_email
import dev.core.uikit.resources.business_welcome_get_code
import dev.core.uikit.resources.business_welcome_google
import dev.core.uikit.resources.business_welcome_phone_hint
import dev.core.uikit.resources.business_welcome_phone_label
import dev.core.uikit.resources.business_welcome_phone_prefix
import dev.core.uikit.resources.business_welcome_student_hint
import dev.core.uikit.resources.business_welcome_subtitle
import dev.core.uikit.resources.business_welcome_title
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import org.jetbrains.compose.resources.stringResource
import dev.core.uikit.component.AppFieldType

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
    // Faqat local test rejimida (USE_LOCAL_DATA) beriladi — Firebase'siz darrov kirish.
    onDevLogin: (() -> Unit)? = null,
) {
    val palette = appPalette

    AppScreenScaffold(scroll = false, horizontalPadding = 20.dp, topPadding = 54.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
            ScreenTitle(stringResource(Res.string.business_welcome_title), fontSize = 21.sp)
        }

        Spacer(Modifier.height(10.dp))
        ScreenSubtitle(stringResource(Res.string.business_welcome_subtitle), palette = palette)

        // Premium hero — biznes belgisi. Gradient sarlavha bilan bir xil brush ishlatiladi.
        Spacer(Modifier.height(22.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(76.dp).clip(RoundedCornerShape(24.dp)).background(palette.headerBrush),
                contentAlignment = Alignment.Center,
            ) {
                // Gradient USTIDAGI ikonka — har ikki rejimda ham oq bo'lib qoladi.
                Icon(AppIcons.Store, null, tint = Color.White, modifier = Modifier.size(38.dp))
            }
        }

        Spacer(Modifier.height(AppSpacing.xl))
        Text(
            stringResource(Res.string.business_welcome_phone_label),
            style = AppType.fieldLabel.copy(fontSize = 13.sp, color = palette.label),
        )
        Spacer(Modifier.height(AppSpacing.sm))
        GlassTextField(
            value = phone,
            onValueChange = onPhoneChange,
            placeholder = stringResource(Res.string.business_welcome_phone_hint),
            leadingContent = { PhonePrefix(palette) },
            type = AppFieldType.UzPhone,
        )

        Spacer(Modifier.height(AppSpacing.lg))
        PrimaryButton(stringResource(Res.string.business_welcome_get_code), onGetCode, enabled = phoneValid && !isLoading)

        Spacer(Modifier.height(AppSpacing.md))
        Row(
            Modifier.fillMaxWidth().height(AppSize.buttonSecondaryHeight)
                .clip(AppRadius.lg)
                .background(palette.glass)
                .border(1.dp, palette.border, AppRadius.lg)
                .clickable(enabled = !isLoading, onClick = onGoogle),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(AppIcons.Google, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(9.dp))
            Text(
                stringResource(Res.string.business_welcome_google),
                style = AppType.label.copy(fontSize = 13.sp, color = palette.ink),
            )
        }

        Spacer(Modifier.height(AppSpacing.lg))
        Text(
            stringResource(Res.string.business_welcome_email),
            modifier = Modifier.fillMaxWidth().height(22.dp).clickable(onClick = onEmail),
            style = AppType.label.copy(fontSize = 13.sp, color = palette.primary, textAlign = TextAlign.Center),
        )

        Spacer(Modifier.height(14.dp))
        HintText(stringResource(Res.string.business_welcome_student_hint), palette = palette)

        // Local test rejimi — Firebase/SMSsiz darrov kirish (backend tayyor bo'lganda yo'qoladi).
        onDevLogin?.let { devLogin ->
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth().height(48.dp)
                    .clip(AppRadius.lg)
                    .background(palette.primary.copy(alpha = 0.12f))
                    .border(1.dp, palette.primary.copy(alpha = 0.4f), AppRadius.lg)
                    .clickable(enabled = !isLoading, onClick = devLogin),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(Res.string.business_welcome_dev_login),
                    style = AppType.label.copy(fontSize = 13.sp, color = palette.primary),
                )
            }
        }
    }
}

/** Telefon maydonining "🇺🇿 +998 |" prefiksi (auth'dagining nusxasi — modul mustaqil bo'lishi uchun). */
@Composable
private fun PhonePrefix(palette: AppPalette) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(Res.string.business_welcome_phone_prefix),
            style = AppType.bodyStrong.copy(fontWeight = AppType.fieldLabel.fontWeight, color = palette.ink),
        )
        Spacer(Modifier.width(9.dp))
        Box(Modifier.width(1.dp).height(22.dp).background(palette.border))
    }
}
