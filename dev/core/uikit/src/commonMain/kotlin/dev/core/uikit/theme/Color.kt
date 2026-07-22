package dev.core.uikit.theme

import androidx.compose.ui.graphics.Color

/**
 * ElonUz — brend palitrasining XOM qiymatlari (`design_handoff_studentclub_elonuz`).
 *
 * Bu fayl faqat "bo'yoq bankasi" — ekranlar bu konstantalarga TO'G'RIDAN-TO'G'RI murojaat
 * qilmasligi kerak. Ular [AppPalette] orqali yorug'/qorong'i rejimga moslab beriladi:
 * ekranda `appPalette.danger` ishlating, `Danger` emas. Aks holda qorong'i rejim buziladi.
 *
 * YORUG' rejim qiymatlari handoff'dan bir-bir olingan. QORONG'I rejim qiymatlari handoff'da
 * yo'q edi — ular shu ko'k oiladan hosil qilingan (fon quyuq ko'k-kulrang, kartalar bir
 * pog'ona ochroq, aksent esa yorqinroq ko'k, chunki quyuq fonda #00ADEE xiralashadi).
 */

// ---- Brend ko'k (TBC) ----
val Primary = Color(0xFF00ADEE)
val PrimaryLight = Color(0xFF23C3F5)
val PrimaryDeep = Color(0xFF0091D8)

/** Qorong'ida asosiy rang ochroq — quyuq fonda kontrast yetarli bo'lsin. */
val PrimaryOnDark = Color(0xFF3FC8F7)

// ---- Matn (yorug') ----
val Ink = Color(0xFF0F2A43)
val InkMuted = Color(0xFF8A98A8)
val InkFaint = Color(0xFFB0B9C4)

/** Nofaol tab / ikonka. */
val Inactive = Color(0xFF93A2B2)

// ---- Matn (qorong'i) ----
val InkDark = Color(0xFFE8F1F8)
val InkMutedDark = Color(0xFF93A6BA)
val InkFaintDark = Color(0xFF6C7F94)

// ---- Yuzalar (yorug') ----
/** Ekran foni — kartalar shu fon ustida turadi. */
val ScreenBg = Color(0xFFEEF3F8)
val CardBg = Color(0xFFFFFFFF)

/** Ikonka nishoni / chip foni — ochiq ko'k. */
val AccentBg = Color(0xFFEAF7FD)
val Divider = Color(0xFFE4EBF2)

/** Oq panel ICHIDAGI kiritish maydoni (chat input) — kartadan bir pog'ona quyuqroq neytral. */
val FieldTrack = Color(0xFFF3F6FA)

/** Ekran foni gradienti (yuqori chapdan ochiq ko'k yorug'lik). */
val BgTopLight = Color(0xFFE7F7FD)
val BgMidLight = Color(0xFFEEF3F8)
val BgBottomLight = Color(0xFFEAF0F6)

// ---- Yuzalar (qorong'i) ----
val ScreenBgDark = Color(0xFF0F1B2A)
val CardBgDark = Color(0xFF17273C)
val AccentBgDark = Color(0xFF11304A)
val DividerDark = Color(0xFF223349)
val FieldTrackDark = Color(0xFF1E3047)

val BgTopDark = Color(0xFF10222F)
val BgMidDark = Color(0xFF0F1B2A)
val BgBottomDark = Color(0xFF0C1622)

/** Telefon bezeli / eng quyuq yuza. */
val Obsidian = Color(0xFF0B1622)

// ---- Holatlar ----
val Success = Color(0xFF16B364)
val Danger = Color(0xFFE0405F)
val DangerBg = Color(0xFFFCEAEE)
val Warning = Color(0xFFF0A62B)

/** Qorong'ida holat foni — och pastel o'rniga shaffof aksent. */
val DangerBgDark = Color(0xFF3A1F28)
val SuccessBgDark = Color(0xFF12301F)
val WarningBgDark = Color(0xFF3A2E15)

// ---- Kategoriya aksentlari (biznes turi kartalari) ----
val AccentFood = Color(0xFFE38B2C)
val AccentGame = Color(0xFF7C5CFF)
val AccentClothing = Color(0xFFEC4899)
val AccentStudy = Color(0xFF3B82F6)
val AccentCinema = Color(0xFFEF4444)
val AccentBeauty = Color(0xFFF472B6)
val AccentBarber = Color(0xFF14B8A6)

/** Kategoriya ikonkasi ortidagi yumshoq gradient (masalan kafe — pushti-sariq). */
val TileFoodStart = Color(0xFFFBD8C5)
val TileFoodEnd = Color(0xFFF7B98C)
