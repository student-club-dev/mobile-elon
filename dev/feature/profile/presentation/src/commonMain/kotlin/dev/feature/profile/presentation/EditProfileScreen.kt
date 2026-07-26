package dev.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.common.error.FormError
import dev.core.domain.model.University
import dev.core.uikit.component.AppBottomSheet
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.FieldLabel
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.InlineErrorText
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.media.rememberImagePicker
import dev.core.uikit.media.toImageBitmapOrNull
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_resend_code
import dev.core.uikit.resources.auth_resend_code_timer_prefix
import dev.core.uikit.resources.auth_verify
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_save
import dev.core.uikit.resources.profile_avatar_change
import dev.core.uikit.resources.profile_avatar_uploading
import dev.core.uikit.resources.profile_course_1
import dev.core.uikit.resources.profile_course_2
import dev.core.uikit.resources.profile_course_3
import dev.core.uikit.resources.profile_course_4
import dev.core.uikit.resources.profile_course_master
import dev.core.uikit.resources.profile_field_course
import dev.core.uikit.resources.profile_field_first_name
import dev.core.uikit.resources.profile_field_gender
import dev.core.uikit.resources.profile_field_last_name
import dev.core.uikit.resources.profile_field_phone
import dev.core.uikit.resources.profile_field_phone_placeholder
import dev.core.uikit.resources.profile_field_university
import dev.core.uikit.resources.profile_field_university_email
import dev.core.uikit.resources.profile_field_university_email_placeholder
import dev.core.uikit.resources.profile_gender_female
import dev.core.uikit.resources.profile_gender_male
import dev.core.uikit.resources.profile_phone_verify_code_placeholder
import dev.core.uikit.resources.profile_phone_verify_later
import dev.core.uikit.resources.profile_phone_verify_subtitle
import dev.core.uikit.resources.profile_phone_verify_title
import dev.core.uikit.resources.profile_saving
import dev.core.uikit.resources.profile_university_select
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.cardShadow
import dev.core.uikit.theme.rowShadow
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.presentation.components.ProfileAvatar
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.util.UZ_DIALING_CODE
import dev.core.uikit.util.nationalPhoneDigits
import dev.core.uikit.util.fullUzPhoneOrNull

/** Chip tanlovi (kurs, jins) — qiymat serverga ketadi, yorlig'i resursdan olinadi. */
private data class ChipOption(val value: String, val label: StringResource)

private val courseOptions = listOf(
    ChipOption("1", Res.string.profile_course_1),
    ChipOption("2", Res.string.profile_course_2),
    ChipOption("3", Res.string.profile_course_3),
    ChipOption("4", Res.string.profile_course_4),
    ChipOption("MASTER", Res.string.profile_course_master),
)

/** Qiymatlar spec'dagi `GenderDto` bilan bir xil ("MALE" | "FEMALE"). */
private val genderOptions = listOf(
    ChipOption("MALE", Res.string.profile_gender_male),
    ChipOption("FEMALE", Res.string.profile_gender_female),
)

/**
 * Backend 422 da qaytaradigan maydon nomlari — `UpdateProfileDto` bilan bir xil
 * (qarang: `ProfileMappers.toUpdateRequest`). Xatolarni to'g'ri maydon ostiga qo'yish uchun
 * kalitlar aynan shu yerda, bitta joyda saqlanadi.
 */
private object ProfileField {
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"
    const val PHONE = "phoneNumber"
    const val GENDER = "gender"
    const val UNIVERSITY = "universityId"
    const val UNIVERSITY_EMAIL = "universityEmail"
    const val COURSE = "courseYear"

    /** Forma ko'rsata oladigan kalitlar — qolganlari umumiy xabar bilan birga chiqadi. */
    val known = setOf(FIRST_NAME, LAST_NAME, PHONE, GENDER, UNIVERSITY, UNIVERSITY_EMAIL, COURSE)
}

/**
 * Profilni tahrirlash ekrani (A2/C2). Local keshdagi profilni prefill qiladi,
 * o'zgarishlarni [ProfileViewModel.saveProfile] orqali backend + local keshga yozadi.
 */
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    /**
     * Universitet va kurs maydonlari faqat TALABA oqimida ko'rsatiladi. Biznes egasi
     * talaba emas — unga bu maydonlar begona, shuning uchun biznes oqimi `false` uzatadi.
     */
    showStudentFields: Boolean = true,
    vm: ProfileViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    val profile = state.profile

    var firstName by remember(profile) { mutableStateOf(profile?.firstName.orEmpty()) }
    var lastName by remember(profile) { mutableStateOf(profile?.lastName.orEmpty()) }
    var phone by remember(profile) { mutableStateOf(nationalPhoneDigits(profile?.phoneNumber)) }
    var gender by remember(profile) { mutableStateOf(profile?.gender) }
    var universityId by remember(profile) { mutableStateOf(profile?.universityId) }
    // Universitet emaili `PUT /profile/me` tanasida bor va 422 shu kalit bilan qaytadi —
    // maydonsiz foydalanuvchi xatoni tuzata olmasdi.
    var universityEmail by remember(profile) { mutableStateOf(profile?.universityEmail.orEmpty()) }
    var courseYear by remember(profile) { mutableStateOf(profile?.courseYear) }

    var uniExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    // Raqam almashtirilganда backend uning tasdig'ini bekor qiladi, tasdiqlanmagan raqam bilan
    // esa biznes yaratib bo'lmaydi (403 PHONE_NOT_VERIFIED) — shu sababli saqlangandan keyin
    // darhol SMS kod so'raymiz. `null` — tasdiqlash kerak emas (raqam o'zgarmagan).
    var verifyingPhone by remember { mutableStateOf<String?>(null) }
    // Backend 422 qaytarsa xato maydonlar bo'yicha taqsimlanadi (`FormError.fields`),
    // maydonga bog'lanmagani esa forma tagida ko'rinadi.
    var error by remember { mutableStateOf<FormError?>(null) }

    // Foydalanuvchi maydonni tuzatishga kirishishi bilan o'sha maydonning xatosi yo'qoladi;
    // qolganlari joyida turadi — bitta saqlashда bir nechta maydon xato bo'lishi mumkin.
    val clearFieldError: (String) -> Unit = { key ->
        val current = error
        if (current != null && key in current.fields) {
            error = current.copy(fields = current.fields - key)
        }
    }

    // Avatar: tanlangan rasm darrov ko'rinadi, ayni paytda fon rejimida serverga yuklanadi.
    var avatarPreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var avatarUploading by remember { mutableStateOf(false) }
    var avatarError by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberImagePicker { picked ->
        if (picked == null) return@rememberImagePicker // bekor qilindi
        avatarError = null
        avatarPreview = picked.bytes.toImageBitmapOrNull()
        avatarUploading = true
        vm.uploadAvatar(picked.bytes, picked.fileName) { err ->
            avatarUploading = false
            if (err != null) {
                avatarError = err
                avatarPreview = null // yuklanmadi — eski rasmga qaytamiz
            }
        }
    }

    // Tasdiqlash oynasi kontent USTIDA chizilishi kerak — shuning uchun ildiz `Box`.
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 54.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                BackButton(
                    onClick = onBack,
                    contentDescription = stringResource(Res.string.common_back),
                    palette = palette,
                )
                Text(
                    // Topbarda foydalanuvchining ISMI turadi — "Profilni tahrirlash" degan
                    // umumiy sarlavha emas. Nima tahrirlanayotgani maydonlardan aniq.
                    state.name,
                    style = AppType.topBarTitle.copy(color = palette.ink),
                    maxLines = 1,
                )
            }
            Spacer(Modifier.height(AppSpacing.xl))

            // Avatar — bosilganda galereya ochiladi
            val changePhotoLabel = stringResource(Res.string.profile_avatar_change)
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfileAvatar(
                        name = state.name,
                        size = 96.dp,
                        fontSize = 36.sp,
                        palette = palette,
                        avatarUrl = profile?.avatarUrl,
                        localPreview = avatarPreview,
                        modifier = Modifier.clickable(enabled = !avatarUploading) { imagePicker.pick() },
                    )
                    // Kamera nishoni — brend rangli doira ustida, shuning uchun ikonka doim oq.
                    Box(
                        Modifier.size(30.dp).clip(AppRadius.pill).background(palette.primary)
                            .clickable(enabled = !avatarUploading) { imagePicker.pick() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(AppIcons.Camera, changePhotoLabel, tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }

                when {
                    avatarUploading -> Text(
                        stringResource(Res.string.profile_avatar_uploading),
                        style = AppType.fieldLabel.copy(fontWeight = AppType.bodyStrong.fontWeight, color = palette.inkMuted),
                    )
                    avatarError != null -> InlineErrorText(avatarError!!, palette = palette)
                    else -> Text(
                        changePhotoLabel,
                        style = AppType.fieldLabel.copy(color = palette.primary),
                        modifier = Modifier.clickable { imagePicker.pick() },
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            Column(
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Maydon xatolari kaliti — `PUT /profile/me` tanasidagi maydon nomi bilan bir xil.
                val fieldErrors = error?.fields.orEmpty()

                val firstNameLabel = stringResource(Res.string.profile_field_first_name)
                FieldLabel(firstNameLabel, palette = palette)
                GlassTextField(
                    firstName,
                    { firstName = it; clearFieldError(ProfileField.FIRST_NAME) },
                    firstNameLabel,
                    leading = AppIcons.Pencil,
                    type = AppFieldType.LatinText,
                )
                FieldError(fieldErrors[ProfileField.FIRST_NAME], palette)

                val lastNameLabel = stringResource(Res.string.profile_field_last_name)
                FieldLabel(lastNameLabel, palette = palette)
                GlassTextField(
                    lastName,
                    { lastName = it; clearFieldError(ProfileField.LAST_NAME) },
                    lastNameLabel,
                    leading = AppIcons.Pencil,
                    type = AppFieldType.LatinText,
                )
                FieldError(fieldErrors[ProfileField.LAST_NAME], palette)

                FieldLabel(stringResource(Res.string.profile_field_phone), palette = palette)
                GlassTextField(
                    phone,
                    { phone = it; clearFieldError(ProfileField.PHONE) },
                    stringResource(Res.string.profile_field_phone_placeholder),
                    leadingContent = { Text(UZ_DIALING_CODE, style = AppType.bodyStrong.copy(color = palette.ink)) },
                    type = AppFieldType.UzPhone,
                )
                FieldError(fieldErrors[ProfileField.PHONE], palette)

                // Jins — talaba ham, biznes egasi ham to'ldiradi: u biznes turlari va
                // kategoriyalar ro'yxatini filtrlaydi (`GET /business/types?gender=`).
                FieldLabel(stringResource(Res.string.profile_field_gender), palette = palette)
                ChipRow(genderOptions, selected = gender, palette = palette) {
                    gender = it
                    clearFieldError(ProfileField.GENDER)
                }
                FieldError(fieldErrors[ProfileField.GENDER], palette)

                if (showStudentFields) {
                    // Universitet tanlash
                    FieldLabel(stringResource(Res.string.profile_field_university), palette = palette)
                    val selectedUni = state.universities.firstOrNull { it.id == universityId }
                    Row(
                        // Maydon — oq yuza + yumshoq soya, chegara yo'q.
                        Modifier.fillMaxWidth().rowShadow(AppRadius.lg).clip(AppRadius.lg)
                            .background(palette.fieldBg)
                            .clickable { uniExpanded = !uniExpanded }
                            .padding(horizontal = AppSpacing.md, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(AppIcons.GraduationCap, null, tint = palette.inkFaint, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(9.dp))
                        Text(
                            selectedUni?.name ?: stringResource(Res.string.profile_university_select),
                            style = AppType.bodyStrong.copy(
                                color = if (selectedUni != null) palette.ink else palette.inkFaint,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        Icon(AppIcons.ChevronDown, null, tint = palette.inkFaint, modifier = Modifier.size(18.dp))
                    }
                    if (uniExpanded) {
                        Column(
                            Modifier.fillMaxWidth().cardShadow(AppRadius.lg).clip(AppRadius.lg)
                                .background(palette.card),
                        ) {
                            state.universities.forEach { uni ->
                                UniversityRow(uni, selected = uni.id == universityId, palette = palette) {
                                    universityId = uni.id
                                    uniExpanded = false
                                    clearFieldError(ProfileField.UNIVERSITY)
                                }
                            }
                        }
                    }
                    FieldError(fieldErrors[ProfileField.UNIVERSITY], palette)

                    FieldLabel(stringResource(Res.string.profile_field_university_email), palette = palette)
                    GlassTextField(
                        universityEmail,
                        { universityEmail = it; clearFieldError(ProfileField.UNIVERSITY_EMAIL) },
                        stringResource(Res.string.profile_field_university_email_placeholder),
                        leading = AppIcons.GraduationCap,
                        type = AppFieldType.Email,
                    )
                    FieldError(fieldErrors[ProfileField.UNIVERSITY_EMAIL], palette)

                    // Kurs tanlash
                    FieldLabel(stringResource(Res.string.profile_field_course), palette = palette)
                    ChipRow(courseOptions, selected = courseYear, palette = palette) {
                        courseYear = it
                        clearFieldError(ProfileField.COURSE)
                    }
                    FieldError(fieldErrors[ProfileField.COURSE], palette)
                }

                // Forma ko'rsata oladigan kalitlar — biznes oqimida talaba maydonlari yo'q.
                val shownKeys = if (showStudentFields) {
                    ProfileField.known
                } else {
                    ProfileField.known - ProfileField.UNIVERSITY -
                        ProfileField.UNIVERSITY_EMAIL - ProfileField.COURSE
                }
                // Umumiy xabar — maydon ostida ko'rinadigan xato bo'lmaganda (takrorlamaslik uchun).
                if (fieldErrors.keys.none { it in shownKeys }) {
                    error?.let { InlineErrorText(it.message, palette = palette) }
                }
                // Formaga tegishsiz (noma'lum yoki yashirilgan) maydon xatolari jim yo'qolmasin.
                fieldErrors.filterKeys { it !in shownKeys }.forEach { (key, text) ->
                    InlineErrorText("$key: $text", palette = palette)
                }

                Spacer(Modifier.height(AppSpacing.xs))
                PrimaryButton(
                    text = if (saving) stringResource(Res.string.profile_saving) else stringResource(Res.string.common_save),
                    enabled = !saving,
                    onClick = {
                        error = null
                        saving = true
                        val newPhone = fullUzPhoneOrNull(phone)
                        val phoneChanged = newPhone != null && newPhone != profile?.phoneNumber
                        val updated = (profile ?: UserProfile()).copy(
                            firstName = firstName.trim().ifBlank { null },
                            lastName = lastName.trim().ifBlank { null },
                            phoneNumber = newPhone,
                            gender = gender,
                            universityId = universityId,
                            universityEmail = universityEmail.trim().ifBlank { null },
                            courseYear = courseYear,
                        )
                        vm.saveProfile(updated) { err ->
                            saving = false
                            when {
                                err != null -> error = err
                                // Raqam almashgan — tasdiqlamasdan chiqib ketmaymiz.
                                phoneChanged -> verifyingPhone = newPhone
                                else -> onBack()
                            }
                        }
                    },
                )
                Spacer(Modifier.height(28.dp))
            }
        }

        // Raqam almashtirilgan bo'lsa — tasdiqlash oynasi (saqlash muvaffaqiyatli o'tgach ochiladi).
        verifyingPhone?.let { pending ->
            PhoneVerificationSheet(
                phone = pending,
                vm = vm,
                palette = palette,
                onVerified = { verifyingPhone = null; onBack() },
                onDismiss = { verifyingPhone = null; onBack() },
            )
        }
    }
}

/**
 * Yangi telefon raqamini SMS kod bilan tasdiqlash oynasi.
 *
 * Nega kerak: backend raqam almashtirilganда uning tasdig'ini bekor qiladi
 * (`PUT /profile/me` — *"changing the phone number resets its verification"*), tasdiqlanmagan
 * raqam bilan esa biznes yaratib/tahrirlab bo'lmaydi (`403 PHONE_NOT_VERIFIED`). Shuning uchun
 * foydalanuvchini shu yerдаyoq tasdiqlashga taklif qilamiz.
 *
 * "Keyinroq" tanlansa ham profil saqlangan bo'ladi — faqat raqam tasdiqlanmagan qoladi.
 *
 * Ochiq (public), chunki biznes qo'shish oqimi ham shu oynani ishlatadi: u yerда backend
 * tasdiqlanmagan raqam bilan biznes yaratishga ruxsat bermaydi.
 *
 * [onVerified] — kod to'g'ri kelди; [onDismiss] — foydalanuvchi "keyinroq" dedi yoki oynani
 * yopdi. Ikkisi alohida, chunki chaqiruvchi ko'pincha faqat muvaffaqiyatda ish davom
 * ettiradi (masalan biznesni qayta saqlaydi).
 */
@Composable
fun PhoneVerificationSheet(
    phone: String,
    vm: ProfileViewModel,
    palette: AppPalette,
    onVerified: () -> Unit,
    onDismiss: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(true) }
    var verifying by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var cooldown by remember { mutableStateOf(0) }

    val sendCode: () -> Unit = {
        sending = true
        message = null
        vm.requestPhoneOtp(phone) { cooldownSeconds, error ->
            sending = false
            message = error
            cooldown = cooldownSeconds ?: 0
        }
    }

    // Oyna ochilishi bilan kod ketadi — foydalanuvchi qo'shimcha tugma bosmaydi.
    LaunchedEffect(phone) { sendCode() }

    // Qayta yuborish taymeri (backend bergan `resendCooldownSeconds` bo'yicha).
    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000)
            cooldown -= 1
        }
    }

    AppBottomSheet(
        visible = true,
        onDismiss = onDismiss,
        title = stringResource(Res.string.profile_phone_verify_title),
        palette = palette,
    ) {
        Text(
            stringResource(Res.string.profile_phone_verify_subtitle, phone),
            style = AppType.subtitle.copy(color = palette.inkMuted),
        )
        Spacer(Modifier.height(AppSpacing.lg))

        GlassTextField(
            value = code,
            onValueChange = { code = it.filter(Char::isDigit).take(OTP_CODE_LENGTH); message = null },
            placeholder = stringResource(Res.string.profile_phone_verify_code_placeholder),
            type = AppFieldType.Number,
        )

        message?.let {
            Spacer(Modifier.height(AppSpacing.sm))
            InlineErrorText(it, palette = palette)
        }

        Spacer(Modifier.height(AppSpacing.md))
        Text(
            text = if (cooldown > 0) {
                stringResource(Res.string.auth_resend_code_timer_prefix) + " $cooldown"
            } else {
                stringResource(Res.string.auth_resend_code)
            },
            style = AppType.link.copy(
                color = if (cooldown > 0) palette.inkFaint else palette.primary,
            ),
            modifier = Modifier.clickable(enabled = cooldown == 0 && !sending) { sendCode() },
        )

        Spacer(Modifier.height(AppSpacing.lg))
        PrimaryButton(
            text = stringResource(Res.string.auth_verify),
            enabled = code.length == OTP_CODE_LENGTH && !verifying && !sending,
            onClick = {
                verifying = true
                vm.verifyPhoneOtp(phone, code) { error ->
                    verifying = false
                    if (error == null) onVerified() else message = error
                }
            },
        )

        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            stringResource(Res.string.profile_phone_verify_later),
            style = AppType.link.copy(color = palette.inkMuted),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onDismiss),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(AppSpacing.md))
    }
}

/** Backend 6 xonali kod yuboradi. */
private const val OTP_CODE_LENGTH = 6

/** Maydon ostidagi xato — backend shu maydon uchun xato qaytarmagan bo'lsa hech narsa chizmaydi. */
@Composable
private fun FieldError(message: String?, palette: AppPalette) {
    if (message != null) InlineErrorText(message, palette = palette)
}

/** Bir qatorli tanlov (kurs, jins) — variantlar teng kenglikda taqsimlanadi. */
@Composable
private fun ChipRow(
    options: List<ChipOption>,
    selected: String?,
    palette: AppPalette,
    onSelect: (String) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        options.forEach { opt ->
            val active = opt.value == selected
            Box(
                // Tanlangani ochiq ko'k aksent fonda; chegara o'rniga soya.
                Modifier.weight(1f).height(42.dp).rowShadow(AppRadius.md).clip(AppRadius.md)
                    .background(if (active) palette.accentBg else palette.card)
                    .clickable { onSelect(opt.value) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(opt.label),
                    style = AppType.hint.copy(
                        fontWeight = AppType.fieldLabel.fontWeight,
                        color = if (active) palette.primary else palette.inkMuted,
                    ),
                )
            }
        }
    }
}

@Composable
private fun UniversityRow(uni: University, selected: Boolean, palette: AppPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            uni.monogram,
            style = AppType.fieldLabel.copy(fontWeight = AppType.screenTitle.fontWeight, color = palette.primary),
            modifier = Modifier.width(48.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(uni.name, style = AppType.label.copy(color = palette.ink))
            Text(uni.city, style = AppType.caption.copy(color = palette.inkFaint))
        }
        if (selected) Icon(AppIcons.ShieldCheck, null, tint = palette.primary, modifier = Modifier.size(17.dp))
    }
}
