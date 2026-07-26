package dev.feature.business

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import dev.core.common.Resource
import dev.core.domain.usecase.ForgotPasswordUseCase
import dev.core.domain.usecase.LoginUseCase
import dev.core.domain.usecase.ResetPasswordUseCase
import dev.core.uikit.component.AppBottomSheet
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.InlineErrorText
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_forgot_password
import dev.core.uikit.resources.auth_password_placeholder
import dev.core.uikit.resources.auth_reset_submit
import dev.core.uikit.resources.auth_reset_subtitle
import dev.core.uikit.resources.auth_reset_title
import dev.core.uikit.resources.auth_sign_in
import dev.core.uikit.resources.business_account_gate_subtitle
import dev.core.uikit.resources.business_account_gate_title
import dev.core.uikit.resources.business_welcome_password_label
import dev.core.uikit.resources.business_welcome_phone_hint
import dev.core.uikit.resources.business_welcome_phone_label
import dev.core.uikit.resources.profile_phone_verify_code_placeholder
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.util.fullUzPhoneOrNull
import dev.feature.profile.domain.usecase.RefreshProfileUseCase
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Kod uzunligi — backend 6 xonali yuboradi. */
private const val OTP_CODE_LENGTH = 6

/** Backend talab qiladigan eng qisqa parol. */
private const val MIN_PASSWORD_LENGTH = 8

/**
 * "Biznes +" bosilганда profilда telefon raqami bo'lmasa ochiladigan oyna.
 *
 * Nega kerak: Google bilan kirilgan hisobda raqam bo'lmaydi, biznes esa tasdiqlangan raqamsiz
 * yaratilmaydi. Foydalanuvchining haqiqiy hisobi odatda **allaqachon bor** — u telefon bilan
 * ro'yxatdan o'tgan. Shuning uchun yangi hisob yasashga urinmaymiz, balki o'shanga kiramiz.
 *
 * Ikki yo'l:
 * - **Parol esda** — raqam + parol → `POST /auth/business/login` → sessiya o'sha hisobga o'tadi
 * - **Parol esda emas** — "Parolni unutdingizmi?" → `forgot` (SMS kod) → kod + yangi parol →
 *   `reset` → darhol o'sha parol bilan kiriladi (foydalanuvchi ikkinchi marta terib o'tirmaydi)
 *
 * [onLoggedIn] — sessiya almashdi, chaqiruvchi biznes formasini ochishi mumkin.
 */
@Composable
fun BusinessAccountGate(
    onLoggedIn: () -> Unit,
    onCancel: () -> Unit,
    login: LoginUseCase = koinInject(),
    forgotPassword: ForgotPasswordUseCase = koinInject(),
    resetPassword: ResetPasswordUseCase = koinInject(),
    refreshProfile: RefreshProfileUseCase = koinInject(),
) {
    val palette = appPalette
    val scope = rememberCoroutineScope()

    var digits by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Parol tiklash bosqichi: `false` — raqam+parol, `true` — SMS kod + yangi parol.
    var resetting by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val phone = fullUzPhoneOrNull(digits)

    /** Kirish muvaffaqiyatli — profilni serverdan tortib, chaqiruvchini xabardor qilamiz. */
    fun finish() = scope.launch {
        refreshProfile()
        onLoggedIn()
    }

    AppBottomSheet(
        visible = true,
        onDismiss = onCancel,
        title = stringResource(
            if (resetting) Res.string.auth_reset_title else Res.string.business_account_gate_title,
        ),
        palette = palette,
    ) {
        Text(
            stringResource(
                if (resetting) Res.string.auth_reset_subtitle else Res.string.business_account_gate_subtitle,
            ),
            style = AppType.subtitle.copy(color = palette.inkMuted),
        )
        Spacer(Modifier.height(AppSpacing.lg))

        FieldLabel(stringResource(Res.string.business_welcome_phone_label), palette = palette)
        Spacer(Modifier.height(AppSpacing.xs))
        GlassTextField(
            value = digits,
            // Kod yuborilgandan keyin raqam o'zgarmaydi — kod aynan o'sha raqamga ketgan.
            onValueChange = { if (!resetting) { digits = it.filter(Char::isDigit).take(9); error = null } },
            placeholder = stringResource(Res.string.business_welcome_phone_hint),
            type = AppFieldType.UzPhone,
        )

        if (resetting) {
            Spacer(Modifier.height(AppSpacing.md))
            GlassTextField(
                value = code,
                onValueChange = { code = it.filter(Char::isDigit).take(OTP_CODE_LENGTH); error = null },
                placeholder = stringResource(Res.string.profile_phone_verify_code_placeholder),
                type = AppFieldType.Number,
            )
        }

        Spacer(Modifier.height(AppSpacing.md))
        FieldLabel(stringResource(Res.string.business_welcome_password_label), palette = palette)
        Spacer(Modifier.height(AppSpacing.xs))
        GlassTextField(
            value = password,
            onValueChange = { password = it; error = null },
            placeholder = stringResource(Res.string.auth_password_placeholder),
            leading = AppIcons.Lock,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )

        error?.let {
            Spacer(Modifier.height(AppSpacing.sm))
            InlineErrorText(it, palette = palette)
        }

        Spacer(Modifier.height(AppSpacing.lg))
        PrimaryButton(
            text = stringResource(if (resetting) Res.string.auth_reset_submit else Res.string.auth_sign_in),
            enabled = !busy && phone != null &&
                password.length >= (if (resetting) MIN_PASSWORD_LENGTH else 1) &&
                (!resetting || code.length == OTP_CODE_LENGTH),
            onClick = {
                val target = phone ?: return@PrimaryButton
                busy = true
                error = null
                scope.launch {
                    if (resetting) {
                        // Avval parolni yangilaymiz, so'ng o'sha parol bilan kiramiz.
                        when (val res = resetPassword(target, code, password)) {
                            is Resource.Error -> { busy = false; error = res.message }
                            else -> when (val logged = login(target, password)) {
                                is Resource.Error -> { busy = false; error = logged.message }
                                else -> { busy = false; finish() }
                            }
                        }
                    } else {
                        when (val res = login(target, password)) {
                            is Resource.Error -> { busy = false; error = res.message }
                            else -> { busy = false; finish() }
                        }
                    }
                }
            },
        )

        if (!resetting) {
            Spacer(Modifier.height(AppSpacing.md))
            Text(
                stringResource(Res.string.auth_forgot_password),
                style = AppType.link.copy(color = palette.primary),
                textAlign = TextAlign.Center,
                // Raqamsiz kod yuborib bo'lmaydi — raqam to'liq bo'lmaguncha o'chiq.
                modifier = Modifier.fillMaxWidth().clickable(enabled = !busy && phone != null) {
                    val target = phone ?: return@clickable
                    busy = true
                    error = null
                    scope.launch {
                        when (val res = forgotPassword(target)) {
                            is Resource.Error -> { busy = false; error = res.message }
                            else -> { busy = false; password = ""; resetting = true }
                        }
                    }
                },
            )
        }
        Spacer(Modifier.height(AppSpacing.md))
    }
}
