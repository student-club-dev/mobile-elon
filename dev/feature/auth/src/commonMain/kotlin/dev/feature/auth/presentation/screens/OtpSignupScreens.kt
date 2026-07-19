package dev.feature.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.AppScreenScaffold
import dev.core.designsystem.components.BackButton
import dev.core.designsystem.components.ErrorText
import dev.core.designsystem.components.FieldLabel
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.components.HintText
import dev.core.designsystem.components.PhoneVisualTransformation
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.components.ScreenSubtitle
import dev.core.designsystem.components.ScreenTitle
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.feature.auth.presentation.flow.Role
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette

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
        BackButton(onBack)
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.size(60.dp)
                .background(Brush.linearGradient(listOf(palette.primary.copy(alpha = 0.14f), palette.primary.copy(alpha = 0.14f))), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.MessageSquare, null, tint = palette.primary, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(16.dp))
        ScreenTitle("Tasdiqlash kodi")
        Spacer(Modifier.height(6.dp))
        Text(
            buildAnnotatedString {
                withStyle(androidx.compose.ui.text.SpanStyle(color = palette.ink, fontWeight = FontWeight.Bold)) {
                    append("+998 ${dev.core.designsystem.components.formatUzPhone(state.phone.ifEmpty { "901234567" })} ")
                }
                withStyle(androidx.compose.ui.text.SpanStyle(color = palette.inkMuted)) {
                    append("raqamiga yuborilgan 6 xonali kodni kiriting.")
                }
            },
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, lineHeight = 19.sp),
        )
        Spacer(Modifier.height(22.dp))

        OtpInput(state.otp, vm::onOtpChange, palette)

        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(AppIcons.Clock, null, tint = palette.inkFaint, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            if (state.resendSeconds > 0) {
                Text(
                    "Kodni qayta yuborish · ",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, color = palette.inkFaint),
                )
                Text(
                    formatTimer(state.resendSeconds),
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = palette.primary),
                )
            } else {
                Text(
                    "Kodni qayta yuborish",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = palette.primary),
                    modifier = Modifier.clickableNoRipple(onResend),
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        PrimaryButton("Tasdiqlash", onVerify, enabled = state.otpValid && !state.isLoading, trailingIcon = AppIcons.Check)

        ErrorText(state.error)

        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Kod SMS orqali kelmadimi? ", style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = palette.inkFaint))
            Text("Telegram orqali oling", style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.Bold, color = palette.primary), modifier = Modifier.clickableNoRipple(onTelegram))
        }
    }
}

private fun formatTimer(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "0$m:${s.toString().padStart(2, '0')}"
}

@Composable
private fun OtpInput(value: String, onValueChange: (String) -> Unit, palette: AppPalette) {
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(6) { i ->
                    val ch = value.getOrNull(i)
                    val focused = i == value.length
                    OtpCell(ch, focused, palette, Modifier.weight(1f))
                }
            }
        },
    )
}

@Composable
private fun OtpCell(ch: Char?, focused: Boolean, palette: AppPalette, modifier: Modifier) {
    val shape = RoundedCornerShape(13.dp)
    val bg = when {
        ch != null -> palette.fieldBg
        focused -> palette.fieldBg
        else -> if (palette.dark) Color.White.copy(alpha = 0.04f) else Color(0xFFF4F2FC)
    }
    val border = when {
        focused -> palette.primary
        ch != null -> palette.primary.copy(alpha = 0.20f)
        else -> palette.border
    }
    Box(
        modifier
            .height(52.dp)
            .clip(shape)
            .background(bg)
            .border(if (focused || ch != null) 1.5.dp else 1.dp, border, shape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            ch != null -> Text(ch.toString(), style = TextStyle(fontFamily = AppFontFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink))
            focused -> Box(Modifier.width(2.dp).height(24.dp).background(palette.primary))
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
    AppScreenScaffold(scroll = false, horizontalPadding = 20, topPadding = 54) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            BackButton(onBack)
            ScreenTitle("Hisob yaratish", size = 21)
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            GlassTextField(state.firstName, vm::onFirstNameChange, "Ism", Modifier.weight(1f), height = 46)
            GlassTextField(state.lastName, vm::onLastNameChange, "Familya", Modifier.weight(1f), height = 46)
        }

        Spacer(Modifier.height(9.dp))
        GlassTextField(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            placeholder = "90 123 45 67",
            leadingContent = { PhonePrefix(palette) },
            height = 46,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = PhoneVisualTransformation(),
        )
        Spacer(Modifier.height(9.dp))
        GlassTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            placeholder = "••••••",
            leading = AppIcons.Lock,
            height = 46,
            trailing = {
                Icon(
                    if (state.passwordVisible) AppIcons.EyeOff else AppIcons.Eye,
                    null, tint = palette.inkFaint,
                    modifier = Modifier.size(16.dp).clickableNoRipple { vm.togglePasswordVisible() },
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        )
        Spacer(Modifier.height(9.dp))

        // Universitet emaili — verified badge (faqat talaba uchun; biznesmenда ko'rinmaydi).
        if (state.role != Role.BUSINESS) {
            val verified = state.universityEmail.endsWith(".uz") && state.universityEmail.contains("@")
            Row(
                Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(13.dp))
                    .background(palette.successBg)
                    .border(1.dp, palette.successDeep.copy(alpha = 0.28f), RoundedCornerShape(13.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(AppIcons.ShieldCheck, null, tint = palette.successDeep, modifier = Modifier.size(16.dp))
                Box(Modifier.weight(1f)) {
                    if (state.universityEmail.isEmpty()) {
                        Text("aziz@tuit.uz", style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, color = palette.inkFaint))
                    }
                    BasicTextField(
                        state.universityEmail, vm::onUniversityEmailChange, singleLine = true,
                        textStyle = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.SemiBold, color = palette.ink),
                        cursorBrush = SolidColor(palette.primary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (verified) {
                    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(palette.successDeep.copy(alpha = 0.14f)).padding(horizontal = 7.dp, vertical = 3.dp)) {
                        Text("TASDIQLANGAN", style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = palette.successDeep))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            HintText("Universitet emaili (ixtiyoriy) — verified talaba nishoni beradi.")
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.clickableNoRipple { vm.toggleTerms() }) {
            CheckBoxSmall(state.termsAccepted, palette)
            Text(
                buildAnnotatedString {
                    withStyle(androidx.compose.ui.text.SpanStyle(color = palette.primary, fontWeight = FontWeight.Bold)) { append("Foydalanish shartlari") }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = palette.label)) { append(" va ") }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = palette.primary, fontWeight = FontWeight.Bold)) { append("Maxfiylik siyosati") }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = palette.label)) { append("ga roziman.") }
                },
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, lineHeight = 16.sp),
            )
        }

        ErrorText(state.error)

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Hisob yaratish", onCreate, enabled = state.termsAccepted && !state.isLoading)
    }
}

// ===========================================================================
// 1i — FORGOT PASSWORD
// ===========================================================================

@Composable
fun ForgotPasswordScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onSend: () -> Unit,
    onBackToLogin: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = false) {
        BackButton(onBack)
        Spacer(Modifier.height(40.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(96.dp)
                    .background(Brush.linearGradient(listOf(palette.primary.copy(alpha = 0.14f), palette.primary.copy(alpha = 0.14f))), RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(64.dp).background(palette.primaryBrush, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.Lock, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.height(22.dp))
            ScreenTitle("Parolni tiklash", size = 23)
            Spacer(Modifier.height(8.dp))
            Text(
                "Email manzilingizni kiriting — parolni tiklash havolasini yuboramiz.",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, color = palette.inkMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 19.sp),
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }

        Spacer(Modifier.height(26.dp))
        FieldLabel("Email manzil")
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            placeholder = "aziz.karimov@edu.uz",
            leading = AppIcons.Mail,
            focused = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(18.dp))
        PrimaryButton("Tiklash havolasini yuborish", onSend, enabled = !state.isLoading)

        state.info?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.SemiBold, color = palette.successDeep, lineHeight = 17.sp))
        }
        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, color = Color(0xFFDC2626), lineHeight = 17.sp))
        }

        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth().clickableNoRipple(onBackToLogin), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(AppIcons.ArrowLeft, null, tint = palette.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text("Kirishga qaytish", style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.primary))
        }
    }
}
