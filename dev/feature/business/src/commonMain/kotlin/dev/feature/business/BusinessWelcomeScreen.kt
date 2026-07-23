package dev.feature.business

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.ErrorText
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.HintText
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenSubtitle
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_forgot_password
import dev.core.uikit.resources.auth_no_account
import dev.core.uikit.resources.auth_password_placeholder
import dev.core.uikit.resources.auth_sign_in
import dev.core.uikit.resources.auth_sign_up
import dev.core.uikit.resources.business_welcome_email
import dev.core.uikit.resources.business_welcome_password_label
import dev.core.uikit.resources.business_welcome_phone_hint
import dev.core.uikit.resources.business_welcome_phone_label
import dev.core.uikit.resources.business_welcome_phone_prefix
import dev.core.uikit.resources.business_welcome_student_hint
import dev.core.uikit.resources.business_welcome_subtitle
import dev.core.uikit.resources.business_welcome_title
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import org.jetbrains.compose.resources.stringResource

/**
 * Biznesmen uchun kirish ekrani — **telefon + parol**.
 *
 * Backend'da SMS kod bilan kirish yo'q (kod faqat raqamni tasdiqlash va parolni tiklash uchun),
 * shuning uchun bu ekran to'g'ridan-to'g'ri `POST /auth/business/login` ni chaqiradi.
 *
 * Sof UI — holat/callbacklarni parametr sifatida oladi (auth moduliga bog'lanmaydi).
 */
@Composable
fun BusinessWelcomeScreen(
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    canSubmit: Boolean,
    isLoading: Boolean,
    error: String?,
    onSignIn: () -> Unit,
    onForgot: () -> Unit,
    onEmail: () -> Unit,
    onRegister: () -> Unit,
) {
    val palette = appPalette

    AppScreenScaffold(scroll = true, horizontalPadding = 20.dp, topPadding = 54.dp) {
        ScreenTitle(stringResource(Res.string.business_welcome_title), fontSize = 21.sp)

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
            style = AppType.fieldLabel.copy(fontSize = 13.sp, color = palette.inkMuted),
        )
        Spacer(Modifier.height(AppSpacing.sm))
        GlassTextField(
            value = phone,
            onValueChange = onPhoneChange,
            placeholder = stringResource(Res.string.business_welcome_phone_hint),
            leadingContent = { PhonePrefix(palette) },
            type = AppFieldType.UzPhone,
        )

        Spacer(Modifier.height(AppSpacing.md))
        Text(
            stringResource(Res.string.business_welcome_password_label),
            style = AppType.fieldLabel.copy(fontSize = 13.sp, color = palette.inkMuted),
        )
        Spacer(Modifier.height(AppSpacing.sm))
        GlassTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(Res.string.auth_password_placeholder),
            leading = AppIcons.Lock,
            trailing = {
                Icon(
                    if (passwordVisible) AppIcons.EyeOff else AppIcons.Eye,
                    null,
                    tint = palette.inkFaint,
                    modifier = Modifier.size(AppSize.iconMd).clip(AppRadius.sm)
                        .clickableNoRipple(onTogglePasswordVisible),
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        )

        Spacer(Modifier.height(AppSpacing.md))
        Text(
            stringResource(Res.string.auth_forgot_password),
            modifier = Modifier.fillMaxWidth().clickableNoRipple(onForgot),
            style = AppType.label.copy(fontSize = 13.sp, color = palette.primary, textAlign = TextAlign.End),
        )

        Spacer(Modifier.height(AppSpacing.lg))
        PrimaryButton(stringResource(Res.string.auth_sign_in), onSignIn, enabled = canSubmit)

        ErrorText(error)

        Spacer(Modifier.height(AppSpacing.lg))
        Text(
            stringResource(Res.string.business_welcome_email),
            modifier = Modifier.fillMaxWidth().height(22.dp).clickableNoRipple(onEmail),
            style = AppType.label.copy(fontSize = 13.sp, color = palette.primary, textAlign = TextAlign.Center),
        )

        Spacer(Modifier.height(AppSpacing.lg))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.auth_no_account),
                style = AppType.label.copy(fontSize = 13.sp, color = palette.inkMuted),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                stringResource(Res.string.auth_sign_up),
                style = AppType.label.copy(fontSize = 13.sp, color = palette.primary),
                modifier = Modifier.clickableNoRipple { if (!isLoading) onRegister() },
            )
        }

        Spacer(Modifier.height(14.dp))
        HintText(stringResource(Res.string.business_welcome_student_hint), palette = palette)
    }
}

/** Telefon maydonining "+998 |" prefiksi (auth'dagining nusxasi — modul mustaqil bo'lishi uchun). */
@Composable
private fun PhonePrefix(palette: AppPalette) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(Res.string.business_welcome_phone_prefix),
            style = AppType.bodyStrong.copy(fontWeight = AppType.fieldLabel.fontWeight, color = palette.ink),
        )
        Spacer(Modifier.width(9.dp))
        Box(Modifier.width(1.dp).height(22.dp).background(palette.divider))
    }
}

/** Mayda interaktiv elementlar uchun bosish yordamchisi. */
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
