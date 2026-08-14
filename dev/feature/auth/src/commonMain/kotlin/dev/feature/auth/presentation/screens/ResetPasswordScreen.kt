package dev.feature.auth.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import dev.core.uikit.component.ToastEffect
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenSubtitle
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_confirm_password_label
import dev.core.uikit.resources.auth_confirm_password_placeholder
import dev.core.uikit.resources.auth_continue
import dev.core.uikit.resources.auth_field_password_label
import dev.core.uikit.resources.auth_otp_phone_prefix
import dev.core.uikit.resources.auth_otp_sent_suffix
import dev.core.uikit.resources.auth_otp_title
import dev.core.uikit.resources.auth_password_min_placeholder
import dev.core.uikit.resources.auth_resend_code
import dev.core.uikit.resources.auth_resend_code_timer_prefix
import dev.core.uikit.resources.auth_reset_password_subtitle
import dev.core.uikit.resources.auth_reset_submit
import dev.core.uikit.resources.auth_reset_title
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.util.formatUzPhone
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.screens.components.AccentIconTile
import dev.feature.auth.presentation.screens.components.CodeInput
import dev.feature.auth.presentation.screens.components.ResendRow
import dev.feature.auth.presentation.screens.components.clickableNoRipple
import org.jetbrains.compose.resources.stringResource

/**
 * Parolni tiklash, 2-qadam — **faqat SMS kod**.
 *
 * Nega kod va parol alohida ekranда: ilgari ikkalasi bitta ekranда edi va foydalanuvchi kod
 * kelgach 6 raqam + ikki marta parol yozishga ulgurmasdi — kod eskirib qolardi. Endi kod
 * kelishi bilan darhol kiritiladi, parol esa keyingi ekranда shoshilmasdan tanlanadi.
 *
 * Kod bu yerда **serverga yuborilmaydi**: backend `POST /auth/business/password/reset` да
 * telefon + kod + yangi parolni **bitta so'rovда** kutadi va kodni alohida tekshiradigan
 * ochiq endpoint yo'q (`/auth/business/otp/verify` bearer token talab qiladi, tiklash paytiда
 * esa sessiya yo'q). Shuning uchun kod holatда saqlanadi va [ResetPasswordScreen] да
 * parol bilan birga jo'natiladi.
 */
@Composable
fun ResetCodeScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onResend: () -> Unit,
    palette: AppPalette = appPalette,
) {
    // Xato tugma tagidagi qizil yozuv emas, yuqori o'ng burchakdagi toast bo'lib chiqadi.
    ToastEffect(state.error, onConsumed = vm::clearError)

    AppScreenScaffold(scroll = true) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(AppSpacing.xl))
        AccentIconTile(AppIcons.MessageSquare, palette)
        Spacer(Modifier.height(AppSpacing.lg))
        ScreenTitle(stringResource(Res.string.auth_otp_title), fontSize = 23.sp)
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

        Spacer(Modifier.height(AppSpacing.section))
        CodeInput(state.otp, vm::onOtpChange, palette)

        Spacer(Modifier.height(AppSpacing.md))
        ResendRow(
            seconds = state.resendSeconds,
            timerPrefix = stringResource(Res.string.auth_resend_code_timer_prefix),
            resendLabel = stringResource(Res.string.auth_resend_code),
            onResend = onResend,
            palette = palette,
        )

        Spacer(Modifier.height(AppSpacing.xl))
        PrimaryButton(
            stringResource(Res.string.auth_continue),
            onContinue,
            enabled = state.otpValid && !state.isLoading,
        )

        Spacer(Modifier.height(AppSpacing.lg))
    }
}

/**
 * Parolni tiklash, 3-qadam — **yangi parol** (`POST /auth/business/password/reset`).
 *
 * Kod oldingi ekranда kiritilgan va holatда turibdi; so'rov shu yerда telefon + kod + parol
 * bilan birga ketadi. Kod eskirgan bo'lsa xato aynan shu ekranда ko'rinadi va foydalanuvchi
 * orqага qaytib kodni qayta so'rashi mumkin.
 */
@Composable
fun ResetPasswordScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    palette: AppPalette = appPalette,
) {
    val pwVisual = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    // Xato tugma tagidagi qizil yozuv emas, yuqori o'ng burchakdagi toast bo'lib chiqadi.
    ToastEffect(state.error, onConsumed = vm::clearError)

    AppScreenScaffold(scroll = true) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(AppSpacing.xl))
        AccentIconTile(AppIcons.Lock, palette)
        Spacer(Modifier.height(AppSpacing.lg))
        ScreenTitle(stringResource(Res.string.auth_reset_title), fontSize = 23.sp)
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle(stringResource(Res.string.auth_reset_password_subtitle))

        Spacer(Modifier.height(AppSpacing.section))
        FieldLabel(stringResource(Res.string.auth_field_password_label))
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            placeholder = stringResource(Res.string.auth_password_min_placeholder),
            leading = AppIcons.Lock,
            trailing = {
                Icon(
                    if (state.passwordVisible) AppIcons.EyeOff else AppIcons.Eye,
                    null,
                    tint = palette.inkFaint,
                    modifier = Modifier.size(AppSize.iconMd).clip(AppRadius.sm)
                        .clickableNoRipple { vm.togglePasswordVisible() },
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = pwVisual,
        )

        Spacer(Modifier.height(13.dp))
        FieldLabel(stringResource(Res.string.auth_confirm_password_label))
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.confirmPassword,
            onValueChange = vm::onConfirmPasswordChange,
            placeholder = stringResource(Res.string.auth_confirm_password_placeholder),
            leading = AppIcons.Lock,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = pwVisual,
        )

        Spacer(Modifier.height(AppSpacing.xl))
        PrimaryButton(
            stringResource(Res.string.auth_reset_submit),
            onSubmit,
            enabled = state.resetReady,
            trailingIcon = AppIcons.Check,
        )

        Spacer(Modifier.height(AppSpacing.lg))
    }
}
