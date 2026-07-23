package dev.feature.auth.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.AuthTab
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.ErrorText
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.FooterLink
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.HintText
import dev.core.uikit.component.LogoTile
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenSubtitle
import dev.core.uikit.component.ScreenTitle
import dev.core.uikit.component.SegmentedTabs
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_confirm_password_label
import dev.core.uikit.resources.auth_confirm_password_placeholder
import dev.core.uikit.resources.auth_email_placeholder
import dev.core.uikit.resources.auth_field_email_label
import dev.core.uikit.resources.auth_field_password_label
import dev.core.uikit.resources.auth_field_phone_label
import dev.core.uikit.resources.auth_have_account
import dev.core.uikit.resources.auth_password_min_placeholder
import dev.core.uikit.resources.auth_phone_placeholder
import dev.core.uikit.resources.auth_register_hint
import dev.core.uikit.resources.auth_register_subtitle
import dev.core.uikit.resources.auth_sign_in
import dev.core.uikit.resources.auth_signup_title
import dev.core.uikit.resources.auth_terms_agree_suffix
import dev.core.uikit.resources.auth_terms_and
import dev.core.uikit.resources.auth_terms_prefix
import dev.core.uikit.resources.auth_terms_privacy
import dev.core.uikit.resources.auth_terms_terms
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.screens.components.CheckBoxSmall
import dev.feature.auth.presentation.screens.components.PhonePrefix
import dev.feature.auth.presentation.screens.components.clickableNoRipple
import org.jetbrains.compose.resources.stringResource

/**
 * Yangi biznes hisobi (`POST /auth/business/register`) — telefon **yoki** email + parol.
 *
 * Telefon bilan ro'yxatdan o'tish tavsiya etiladi: parolni tiklash faqat SMS orqali ishlaydi
 * (`/auth/business/password/forgot`). Hisob darhol ochiladi, raqam esa keyingi ekranda
 * SMS kod bilan tasdiqlanadi.
 */
@Composable
fun RegisterScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onSignIn: () -> Unit,
    palette: AppPalette = appPalette,
) {
    val pwVisual = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    AppScreenScaffold(scroll = true) {
        BackButton(onBack, contentDescription = stringResource(Res.string.common_back))
        Spacer(Modifier.height(AppSpacing.xl))
        LogoTile(size = 48.dp, radius = 15.dp, iconSize = 26.dp)
        Spacer(Modifier.height(AppSpacing.lg))
        ScreenTitle(stringResource(Res.string.auth_signup_title))
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle(stringResource(Res.string.auth_register_subtitle))
        Spacer(Modifier.height(AppSpacing.xl))

        SegmentedTabs(
            selected = if (state.registerWithEmail) AuthTab.EMAIL else AuthTab.PHONE,
            onSelect = { vm.onRegisterMethodChange(it == AuthTab.EMAIL) },
        )
        Spacer(Modifier.height(AppSpacing.lg))

        if (state.registerWithEmail) {
            FieldLabel(stringResource(Res.string.auth_field_email_label))
            Spacer(Modifier.height(7.dp))
            GlassTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                placeholder = stringResource(Res.string.auth_email_placeholder),
                leading = AppIcons.Mail,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
        } else {
            FieldLabel(stringResource(Res.string.auth_field_phone_label))
            Spacer(Modifier.height(7.dp))
            GlassTextField(
                value = state.phone,
                onValueChange = vm::onPhoneChange,
                placeholder = stringResource(Res.string.auth_phone_placeholder),
                leadingContent = { PhonePrefix(palette) },
                type = AppFieldType.UzPhone,
            )
        }
        Spacer(Modifier.height(13.dp))

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
        Spacer(Modifier.height(AppSpacing.sm))
        HintText(stringResource(Res.string.auth_register_hint))

        Spacer(Modifier.height(AppSpacing.md))
        TermsRow(state.termsAccepted, palette) { vm.toggleTerms() }

        Spacer(Modifier.height(AppSpacing.xl))
        PrimaryButton(
            stringResource(Res.string.auth_signup_title),
            onCreate,
            enabled = state.registerReady,
            trailingIcon = AppIcons.ArrowRight,
        )

        ErrorText(state.error)

        Spacer(Modifier.height(20.dp))
        FooterLink(
            stringResource(Res.string.auth_have_account),
            stringResource(Res.string.auth_sign_in),
            onSignIn,
        )
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
    val plain = SpanStyle(color = palette.inkMuted)
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
