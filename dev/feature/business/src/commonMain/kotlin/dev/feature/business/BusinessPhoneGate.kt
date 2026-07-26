package dev.feature.business

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.AppBottomSheet
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.InlineErrorText
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.business_phone_gate_continue
import dev.core.uikit.resources.business_phone_gate_subtitle
import dev.core.uikit.resources.business_phone_gate_title
import dev.core.uikit.resources.business_welcome_phone_hint
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.util.fullUzPhoneOrNull
import dev.core.uikit.util.nationalPhoneDigits
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.presentation.PhoneVerificationSheet
import dev.feature.profile.presentation.ProfileViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Biznes qo'shish uchun avval telefon raqamingizni tasdiqlang" oqimi.
 *
 * Ikki bosqich, ikkalasi ham oyna: **raqam** → `PUT /profile/me` → **SMS kod**
 * (`/auth/business/otp/request` + `/verify`). Tasdiqlangach [onVerified] chaqiriladi va
 * chaqiruvchi (biznes formasi) saqlashni o'zi qayta uriniadi — foydalanuvchi to'ldirgan
 * formadan chiqib ketmaydi.
 *
 * Nega biznes modulida: raqam profil qatlamiga (`ProfileViewModel`) tegishli, biznes
 * formasi esa `discounts:presentation` да — u modulда profil bog'liqligi yo'q. Shu modul
 * ikkalasini ham ko'radi, shuning uchun ulash shu yerda.
 */
@Composable
fun BusinessPhoneGate(
    onVerified: () -> Unit,
    onCancel: () -> Unit,
    vm: ProfileViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    val profile = state.profile

    // Profilда raqam bor (lekin tasdiqlanmagan) bo'lsa — maydonni shundan to'ldiramiz.
    var digits by remember(profile) { mutableStateOf(nationalPhoneDigits(profile?.phoneNumber)) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Raqam saqlangach shu yerga yoziladi va kod oynasi ochiladi.
    var verifying by remember { mutableStateOf<String?>(null) }

    verifying?.let { phone ->
        PhoneVerificationSheet(
            phone = phone,
            vm = vm,
            palette = palette,
            onVerified = { verifying = null; onVerified() },
            onDismiss = { verifying = null; onCancel() },
        )
        return
    }

    AppBottomSheet(
        visible = true,
        onDismiss = onCancel,
        title = stringResource(Res.string.business_phone_gate_title),
        palette = palette,
    ) {
        Text(
            stringResource(Res.string.business_phone_gate_subtitle),
            style = AppType.subtitle.copy(color = palette.inkMuted),
        )
        Spacer(Modifier.height(AppSpacing.lg))

        GlassTextField(
            value = digits,
            onValueChange = { digits = it.filter(Char::isDigit).take(9); error = null },
            placeholder = stringResource(Res.string.business_welcome_phone_hint),
            type = AppFieldType.UzPhone,
        )

        error?.let {
            Spacer(Modifier.height(AppSpacing.sm))
            InlineErrorText(it, palette = palette)
        }

        Spacer(Modifier.height(AppSpacing.lg))
        PrimaryButton(
            text = stringResource(Res.string.business_phone_gate_continue),
            enabled = digits.length == 9 && !saving,
            onClick = {
                val phone = fullUzPhoneOrNull(digits) ?: return@PrimaryButton
                saving = true
                error = null
                // Raqamni profilga yozamiz — kod aynan profildagi raqamga yuboriladi.
                vm.saveProfile((profile ?: UserProfile()).copy(phoneNumber = phone)) { err ->
                    saving = false
                    // Maydon xatosi bo'lsa (masalan "raqam band") — aynan shu matn ko'rsatiladi.
                    if (err == null) verifying = phone else error = err.fieldMessage() ?: err.message
                }
            },
        )
        Spacer(Modifier.height(AppSpacing.md))
    }
}

/** Backend `phoneNumber` maydoni uchun xato qaytargan bo'lsa — o'shani ko'rsatamiz. */
private fun dev.core.common.error.FormError.fieldMessage(): String? = fields["phoneNumber"]
