package dev.feature.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.ErrorText
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.HintText
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.component.SoftPill
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_first_name
import dev.core.uikit.resources.auth_last_name
import dev.core.uikit.resources.auth_otp_no_sms
import dev.core.uikit.resources.auth_otp_phone_prefix
import dev.core.uikit.resources.auth_otp_sent_suffix
import dev.core.uikit.resources.auth_otp_title
import dev.core.uikit.resources.auth_otp_via_telegram
import dev.core.uikit.resources.auth_password_short_placeholder
import dev.core.uikit.resources.auth_phone_placeholder
import dev.core.uikit.resources.auth_resend_code
import dev.core.uikit.resources.auth_resend_code_timer_prefix
import dev.core.uikit.resources.auth_signup_title
import dev.core.uikit.resources.auth_terms_agree_suffix
import dev.core.uikit.resources.auth_terms_and
import dev.core.uikit.resources.auth_terms_prefix
import dev.core.uikit.resources.auth_terms_privacy
import dev.core.uikit.resources.auth_terms_terms
import dev.core.uikit.resources.auth_university_email_hint
import dev.core.uikit.resources.auth_university_email_placeholder
import dev.core.uikit.resources.auth_verified_badge
import dev.core.uikit.resources.auth_verify
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.util.formatUzPhone
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.flow.Role
import dev.feature.auth.presentation.screens.components.AccentIconTile
import dev.feature.auth.presentation.screens.components.CheckBoxSmall
import dev.feature.auth.presentation.screens.components.CodeInput
import dev.feature.auth.presentation.screens.components.PhonePrefix
import dev.feature.auth.presentation.screens.components.ResendRow
import dev.feature.auth.presentation.screens.components.clickableNoRipple
import org.jetbrains.compose.resources.stringResource
import dev.core.uikit.component.AppFieldType

// ===========================================================================
// 1g — OTP
// ===========================================================================

@Composable
fun OtpScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onTelegram: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = false) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(AppSpacing.xl))
        AccentIconTile(AppIcons.MessageSquare, palette)
        Spacer(Modifier.height(AppSpacing.lg))
        ScreenTitle(stringResource(Res.string.auth_otp_title))
        Spacer(Modifier.height(6.dp))

        val phonePart = stringResource(
            Res.string.auth_otp_phone_prefix,
            formatUzPhone(state.phone.ifEmpty { "901234567" }),
        )
        val suffixPart = stringResource(Res.string.auth_otp_sent_suffix)
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = palette.ink, fontWeight = AppType.label.fontWeight)) {
                    append(phonePart)
                }
                withStyle(SpanStyle(color = palette.inkMuted)) { append(suffixPart) }
            },
            style = AppType.subtitle,
        )
        Spacer(Modifier.height(22.dp))

        CodeInput(state.otp, vm::onOtpChange, palette)

        Spacer(Modifier.height(22.dp))
        ResendRow(
            seconds = state.resendSeconds,
            timerPrefix = stringResource(Res.string.auth_resend_code_timer_prefix),
            resendLabel = stringResource(Res.string.auth_resend_code),
            onResend = onResend,
            palette = palette,
        )

        Spacer(Modifier.height(22.dp))
        PrimaryButton(
            stringResource(Res.string.auth_verify),
            onVerify,
            enabled = state.otpValid && !state.isLoading,
            trailingIcon = AppIcons.Check,
        )

        ErrorText(state.error)

        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                stringResource(Res.string.auth_otp_no_sms),
                style = AppType.hint.copy(color = palette.inkFaint),
            )
            Text(
                stringResource(Res.string.auth_otp_via_telegram),
                style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.primary),
                modifier = Modifier.clickableNoRipple(onTelegram),
            )
        }
    }
}

// ===========================================================================
// 1h — SIGN UP (rol bilan)
// ===========================================================================

@Composable
fun SignUpScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = false, horizontalPadding = 20.dp, topPadding = 54.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
            ScreenTitle(stringResource(Res.string.auth_signup_title), fontSize = 21.sp)
        }

        Spacer(Modifier.height(AppSpacing.lg))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            GlassTextField(
                state.firstName,
                vm::onFirstNameChange,
                stringResource(Res.string.auth_first_name),
                Modifier.weight(1f),
                height = 46.dp,
            )
            GlassTextField(
                state.lastName,
                vm::onLastNameChange,
                stringResource(Res.string.auth_last_name),
                Modifier.weight(1f),
                height = 46.dp,
            )
        }

        Spacer(Modifier.height(9.dp))
        GlassTextField(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            placeholder = stringResource(Res.string.auth_phone_placeholder),
            leadingContent = { PhonePrefix(palette) },
            height = 46.dp,
            type = AppFieldType.UzPhone,
        )
        Spacer(Modifier.height(9.dp))
        GlassTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            placeholder = stringResource(Res.string.auth_password_short_placeholder),
            leading = AppIcons.Lock,
            height = 46.dp,
            trailing = {
                Icon(
                    if (state.passwordVisible) AppIcons.EyeOff else AppIcons.Eye,
                    null,
                    tint = palette.inkFaint,
                    modifier = Modifier.size(16.dp).clickableNoRipple { vm.togglePasswordVisible() },
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        )
        Spacer(Modifier.height(9.dp))

        // Universitet emaili — verified nishoni (faqat talaba uchun; biznesmenda ko'rinmaydi).
        if (state.role != Role.BUSINESS) {
            UniversityEmailField(state.universityEmail, vm::onUniversityEmailChange, palette)
            Spacer(Modifier.height(6.dp))
            HintText(stringResource(Res.string.auth_university_email_hint))
        }

        Spacer(Modifier.height(AppSpacing.md))
        TermsRow(state.termsAccepted, palette) { vm.toggleTerms() }

        ErrorText(state.error)

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(AppSpacing.lg))
        PrimaryButton(
            stringResource(Res.string.auth_signup_title),
            onCreate,
            enabled = state.termsAccepted && !state.isLoading,
        )
    }
}

/** Universitet emaili maydoni — to'g'ri formatda "TASDIQLANGAN" nishoni chiqadi. */
@Composable
private fun UniversityEmailField(value: String, onValueChange: (String) -> Unit, palette: AppPalette) {
    val shape = RoundedCornerShape(13.dp)
    val verified = value.endsWith(".uz") && value.contains("@")
    Row(
        Modifier.fillMaxWidth().height(46.dp).clip(shape)
            .background(palette.successBg)
            .border(1.dp, palette.successDeep.copy(alpha = 0.28f), shape)
            .padding(horizontal = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(AppIcons.ShieldCheck, null, tint = palette.successDeep, modifier = Modifier.size(16.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    stringResource(Res.string.auth_university_email_placeholder),
                    style = AppType.link.copy(color = palette.inkFaint),
                )
            }
            BasicTextField(
                value,
                onValueChange,
                singleLine = true,
                textStyle = AppType.link.copy(fontWeight = AppType.bodyStrong.fontWeight, color = palette.ink),
                cursorBrush = SolidColor(palette.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (verified) {
            SoftPill(
                text = stringResource(Res.string.auth_verified_badge),
                accent = palette.successDeep,
                backgroundAlpha = 0.14f,
                shape = RoundedCornerShape(6.dp),
                textStyle = AppType.caption.copy(fontSize = 10.sp, fontWeight = AppType.button.fontWeight),
                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp),
            )
        }
    }
}

/** Foydalanish shartlari va maxfiylik siyosatiga rozilik qatori. */
@Composable
private fun TermsRow(accepted: Boolean, palette: AppPalette, onToggle: () -> Unit) {
    val prefix = stringResource(Res.string.auth_terms_prefix)
    val terms = stringResource(Res.string.auth_terms_terms)
    val and = stringResource(Res.string.auth_terms_and)
    val privacy = stringResource(Res.string.auth_terms_privacy)
    val suffix = stringResource(Res.string.auth_terms_agree_suffix)
    val plain = SpanStyle(color = palette.label)
    val accent = SpanStyle(color = palette.primary, fontWeight = AppType.label.fontWeight)
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        modifier = Modifier.clickableNoRipple(onToggle),
    ) {
        CheckBoxSmall(accepted, palette)
        Text(
            buildAnnotatedString {
                if (prefix.isNotEmpty()) withStyle(plain) { append(prefix) }
                withStyle(accent) { append(terms) }
                withStyle(plain) { append(and) }
                withStyle(accent) { append(privacy) }
                withStyle(plain) { append(suffix) }
            },
            style = AppType.hint.copy(lineHeight = 16.sp),
        )
    }
}
