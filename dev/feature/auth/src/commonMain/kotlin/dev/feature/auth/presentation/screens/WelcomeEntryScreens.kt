package dev.feature.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.AppScreenScaffold
import dev.core.designsystem.components.AuthTab
import dev.core.designsystem.components.BackButton
import dev.core.designsystem.components.ErrorText
import dev.core.designsystem.components.FieldLabel
import dev.core.designsystem.components.FooterLink
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.components.HintText
import dev.core.designsystem.components.LogoTile
import dev.core.designsystem.components.OrDivider
import dev.core.designsystem.components.PhoneVisualTransformation
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.components.ScreenSubtitle
import dev.core.designsystem.components.ScreenTitle
import dev.core.designsystem.components.SegmentedTabs
import dev.core.designsystem.components.SocialRow
import dev.feature.auth.presentation.flow.AuthFlowState
import dev.feature.auth.presentation.flow.AuthFlowViewModel
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette

/** Telefon maydonining "🇺🇿 +998 |" prefiksi. */
@Composable
fun PhonePrefix(palette: AppPalette) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "🇺🇿 +998",
            style = TextStyle(
                fontFamily = AppFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = palette.ink
            ),
        )
        Spacer(Modifier.width(9.dp))
        Box(Modifier.width(1.dp).height(22.dp).background(palette.border))
    }
}

// ===========================================================================
// 1d — ONBOARDING
// ===========================================================================

@Composable
fun OnboardingScreen(
    onNext: () -> Unit,
    onSkip: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(topPadding = 52) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                "O‘tkazib yuborish",
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickableNoRipple(onSkip)
                    .padding(4.dp),
                style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 12.5f.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.inkFaint
                ),
            )
        }

        // Illustration + floating chips
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            Box(
                Modifier.align(Alignment.Center).size(180.dp).background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            listOf(
                                palette.primary.copy(
                                    alpha = 0.20f
                                ), androidx.compose.ui.graphics.Color.Transparent
                            )
                        ), RoundedCornerShape(999.dp)
                    ),
            )
            Box(
                Modifier.align(Alignment.Center).size(104.dp)
                    .background(palette.primaryBrush, RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    AppIcons.GraduationCap,
                    null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }
            FloatingChip(
                "🍕",
                "Buyurtma",
                Modifier.align(Alignment.TopStart).offset(x = 4.dp, y = 40.dp),
                palette
            )
            FloatingChip(
                "📚",
                "AI Muallim",
                Modifier.align(Alignment.CenterEnd).offset(x = (-4).dp, y = 30.dp),
                palette
            )
            FloatingChip(
                "🏠", null, Modifier.align(Alignment.TopEnd).offset(x = (-30).dp, y = 4.dp), palette
            )
            FloatingChip(
                "💼",
                null,
                Modifier.align(Alignment.BottomStart).offset(x = 20.dp, y = (-8).dp),
                palette
            )
        }

        Spacer(Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Hamma xizmat bitta ilovada",
                style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    color = palette.ink,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 28.sp
                ),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "7-10 ta ilova o‘rniga bitta hisob. Ovqatdan darsgacha, ishdan turar joygacha.",
                style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 13.sp,
                    color = palette.inkMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 19.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Dot(false, palette); Dot(true, palette); Dot(false, palette)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton("Keyingi", onNext, trailingIcon = AppIcons.ArrowRight)
        }
    }
}

@Composable
private fun Dot(active: Boolean, palette: AppPalette) {
    Box(
        Modifier.height(7.dp).width(if (active) 22.dp else 7.dp).background(
                if (active) palette.primary else palette.primary.copy(alpha = 0.25f),
                RoundedCornerShape(999.dp)
            ),
    )
}

@Composable
private fun FloatingChip(emoji: String, label: String?, modifier: Modifier, palette: AppPalette) {
    Row(
        modifier.clip(RoundedCornerShape(15.dp)).background(palette.glassStrong)
            .border(1.dp, palette.border, RoundedCornerShape(15.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(emoji, style = TextStyle(fontSize = 15.sp))
        if (label != null) {
            Text(
                label, style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.ink
                )
            )
        }
    }
}

// ===========================================================================
// 1a — WELCOME (to'liq forma, tab bilan)
// ===========================================================================

@Composable
fun WelcomeScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    tab: AuthTab,
    onTab: (AuthTab) -> Unit,
    onContinue: () -> Unit,
    onSignUp: () -> Unit,
    onGoogle: () -> Unit,
    onApple: () -> Unit,
    onTelegram: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = true, topPadding = 60) {
        LogoTile()
        Spacer(Modifier.height(18.dp))
        ScreenTitle("Xush kelibsiz 👋", size = 25)
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle("Hisobingizga kiring yoki bir daqiqada ro‘yxatdan o‘ting.")
        Spacer(Modifier.height(18.dp))

        SegmentedTabs(tab, onTab)
        Spacer(Modifier.height(14.dp))

        if (tab == AuthTab.PHONE) {
            FieldLabel("Telefon raqamingiz")
            Spacer(Modifier.height(7.dp))
            GlassTextField(
                value = state.phone,
                onValueChange = vm::onPhoneChange,
                placeholder = "90 123 45 67",
                leadingContent = { PhonePrefix(palette) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = PhoneVisualTransformation(),
                textLetterSpacing = 0.5f,
            )
            Spacer(Modifier.height(8.dp))
            HintText("Ushbu raqamga tasdiqlash uchun SMS kod yuboramiz.")
        } else {
            FieldLabel("Email manzil")
            Spacer(Modifier.height(7.dp))
            GlassTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                placeholder = "aziz.karimov@edu.uz",
                leading = AppIcons.Mail,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(8.dp))
            HintText("Kirish yoki ro‘yxatdan o‘tish uchun email kiriting.")
        }

        Spacer(Modifier.height(16.dp))
        PrimaryButton("Davom etish", onContinue, trailingIcon = AppIcons.ArrowRight)

        state.error?.let {
            Spacer(Modifier.height(10.dp))
            Text(
                it, style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 12.sp,
                    color = androidx.compose.ui.graphics.Color(0xFFDC2626)
                )
            )
        }

        Spacer(Modifier.height(18.dp))
        OrDivider()
        Spacer(Modifier.height(14.dp))
        SocialRow(onGoogle, onApple, onTelegram)

        Spacer(Modifier.height(20.dp))
        FooterLink("Hisobingiz yo‘qmi?", "Ro‘yxatdan o‘tish", onSignUp)
    }
}

// ===========================================================================
// 1e — PHONE ENTRY
// ===========================================================================

@Composable
fun PhoneScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onSwitchEmail: () -> Unit,
    onGetCode: () -> Unit,
    onSignIn: () -> Unit,
    onGoogle: () -> Unit,
    onApple: () -> Unit,
    onTelegram: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = false) {
        BackButton(onBack)
        Spacer(Modifier.height(20.dp))
        ScreenTitle("Telefon raqamingiz")
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle("Kirish uchun raqamingizni kiriting — SMS orqali 6 xonali kod yuboramiz.")
        Spacer(Modifier.height(18.dp))

        SegmentedTabs(AuthTab.PHONE, { if (it == AuthTab.EMAIL) onSwitchEmail() })
        Spacer(Modifier.height(16.dp))

        GlassTextField(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            placeholder = "90 123 45 67",
            leadingContent = { PhonePrefix(palette) },
            focused = true,
            height = 56,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = PhoneVisualTransformation(),
            textLetterSpacing = 0.5f,
        )
        Spacer(Modifier.height(9.dp))
        HintText("Faqat O‘zbekiston raqamlari qabul qilinadi.")

        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            "Kod olish",
            onGetCode,
            enabled = state.phoneValid && !state.isLoading,
            trailingIcon = AppIcons.ArrowRight
        )

        ErrorText(state.error)

        Spacer(Modifier.height(18.dp))
        OrDivider()
        Spacer(Modifier.height(14.dp))
        SocialRow(onGoogle, onApple, onTelegram)

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(14.dp))
        FooterLink("Hisobingiz bormi?", "Kirish", onSignIn)
    }
}

// ===========================================================================
// 1f — EMAIL LOGIN
// ===========================================================================

@Composable
fun EmailLoginScreen(
    state: AuthFlowState,
    vm: AuthFlowViewModel,
    onBack: () -> Unit,
    onSwitchPhone: () -> Unit,
    onLogin: () -> Unit,
    onForgot: () -> Unit,
    onBiometric: () -> Unit,
    onSignUp: () -> Unit,
    palette: AppPalette = appPalette,
) {
    AppScreenScaffold(scroll = false) {
        BackButton(onBack)
        Spacer(Modifier.height(18.dp))
        ScreenTitle("Xush kelibsiz")
        Spacer(Modifier.height(6.dp))
        ScreenSubtitle("Email va parolingiz bilan hisobingizga kiring.")
        Spacer(Modifier.height(16.dp))

        SegmentedTabs(AuthTab.EMAIL, { if (it == AuthTab.PHONE) onSwitchPhone() })
        Spacer(Modifier.height(16.dp))

        FieldLabel("Email manzil")
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            placeholder = "aziz.karimov@edu.uz",
            leading = AppIcons.Mail,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(13.dp))
        FieldLabel("Parol")
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            placeholder = "••••••••",
            leading = AppIcons.Lock,
            trailing = {
                Icon(
                    if (state.passwordVisible) AppIcons.EyeOff else AppIcons.Eye,
                    null,
                    tint = palette.inkFaint,
                    modifier = Modifier.size(18.dp).clip(RoundedCornerShape(6.dp))
                        .clickableNoRipple { vm.togglePasswordVisible() },
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        )

        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickableNoRipple { vm.toggleRememberMe() }) {
                CheckBoxSmall(state.rememberMe, palette)
                Text(
                    "Meni eslab qol", style = TextStyle(
                        fontFamily = AppFontFamily,
                        fontSize = 12.5f.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.label
                    )
                )
            }
            Text(
                "Parolni unutdingizmi?",
                style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 12.5f.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary
                ),
                modifier = Modifier.clickableNoRipple(onForgot),
            )
        }

        Spacer(Modifier.height(18.dp))
        PrimaryButton("Kirish", onLogin, enabled = !state.isLoading)
        Spacer(Modifier.height(11.dp))
        dev.core.designsystem.components.OutlineButton(
            "Face ID bilan kirish", onBiometric, leadingIcon = AppIcons.ScanFace
        )

        ErrorText(state.error)

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(14.dp))
        FooterLink("Hisobingiz yo‘qmi?", "Ro‘yxatdan o‘tish", onSignUp)
    }
}

// ---- Umumiy kichik yordamchilar ----

@Composable
internal fun CheckBoxSmall(checked: Boolean, palette: AppPalette, size: Int = 20) {
    Box(
        Modifier.size(size.dp).clip(RoundedCornerShape(6.dp))
            .background(if (checked) palette.primary else androidx.compose.ui.graphics.Color.Transparent)
            .border(if (checked) 0.dp else 1.5.dp, palette.border, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(
            AppIcons.Check,
            null,
            tint = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.size((size * 0.6f).dp)
        )
    }
}

/** Mayda interaktiv elementlar uchun bosish yordamchisi. */
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
