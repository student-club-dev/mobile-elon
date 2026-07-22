package dev.core.uikit.theme

import androidx.compose.ui.graphics.Color

/**
 * ElonUz — brend palitrasining XOM qiymatlari.
 *
 * Bu fayl faqat "bo'yoq bankasi" — ekranlar bu konstantalarga TO'G'RIDAN-TO'G'RI murojaat
 * qilmasligi kerak. Ular [AppPalette] orqali yorug'/qorong'i rejimga moslab beriladi:
 * ekranda `appPalette.danger` ishlating, `Danger` emas. Aks holda qorong'i rejim buziladi.
 */

// ---- Brand violet ----
val Primary = Color(0xFF6C47FF)
val PrimaryGradientEnd = Color(0xFF8E6BFF)
val PrimaryAccent = Color(0xFFA78BFA)
val PrimaryDark = Color(0xFF8B6DFF)
val LinkDark = Color(0xFFB9A6FF)

// ---- Header gradient (profil / talabalar / biznes sarlavhalari) ----
val HeaderStart = Color(0xFF6C47FF)
val HeaderMid = Color(0xFF7C4DFF)
val HeaderEnd = Color(0xFF5B34D6)
val HeaderStartDark = Color(0xFF4B32B8)
val HeaderMidDark = Color(0xFF5A38C9)
val HeaderEndDark = Color(0xFF3E2496)

// ---- Ink / text (light) ----
val Ink = Color(0xFF1E1B4B)
val InkMuted = Color(0xFF6E698C)
val InkFaint = Color(0xFF8B86A6)
val LabelInk = Color(0xFF565278)

// ---- Text (dark) ----
val TextDark = Color(0xFFF0EEFF)
val TextMutedDark = Color(0xFFA29DB8)
val TextFaintDark = Color(0xFF8781A0)
val LabelDark = Color(0xFFB4AECD)

// ---- Surfaces ----
val SurfaceLight = Color(0xFFFAFAFB)
val SurfaceDark = Color(0xFF0F0F1A)

/** Pastki navigatsiya paneli / ko'tarilgan yuzalar. */
val BarSurfaceLight = Color.White
val BarSurfaceDark = Color(0xFF1A1630)

/** Tanlanmagan chip / bosqich indikatori foni. */
val ChipTrackLight = Color(0xFFF4F2FC)

// ---- Background gradient stops (light) ----
val BgTopLight = Color(0xFFF3F0FF)
val BgMidLight = Color(0xFFFBFAFE)
val BgBottomLight = Color(0xFFEBF6FF)

// ---- Background gradient stops (dark) ----
val BgTopDark = Color(0xFF171331)
val BgMidDark = Color(0xFF0F0F1A)
val BgBottomDark = Color(0xFF150F26)

// ---- Feedback ----
val Success = Color(0xFF10B981)
val SuccessDeep = Color(0xFF059669)

/** Xato / o'chirish / chiqish. Qorong'ida ochroq variant — qora fonda kontrast yetarli bo'lsin. */
val Danger = Color(0xFFDC2626)
val DangerDark = Color(0xFFF87171)

/** Ogohlantirish (moderatsiya kutilmoqda, muddati tugayapti). */
val Warning = Color(0xFFB45309)
val WarningBgLight = Color(0xFFFEF3C7)
val WarningDark = Color(0xFFFBBF24)

/** Reyting yulduzi. */
val Amber = Color(0xFFF59E0B)

/** O'qilmagan bildirishnoma nuqtasi. */
val BadgeDot = Color(0xFFFF5A5A)

/** Yorug' rejimda shisha yuza ustidagi matn (outline tugma). */
val OnGlassLight = Color(0xFF4A3F86)

/** Rasm ustidagi qoplama asosi — shaffofligi [AppPalette.scrim] da beriladi. */
val ScrimBlack = Color(0xFF000000)

// ---- Dekorativ ----
val Cyan = Color(0xFF22D3EE)

// ---- Module accents (rol / success kartalar / bildirishnoma turlari) ----
val ModuleFood = Color(0xFFF97316)
val ModuleStudy = Color(0xFF2563EB)
val ModuleEmployer = Color(0xFFD97706)
val ModuleHousing = Color(0xFF059669)
val ModuleMedical = Color(0xFFBE185D)
