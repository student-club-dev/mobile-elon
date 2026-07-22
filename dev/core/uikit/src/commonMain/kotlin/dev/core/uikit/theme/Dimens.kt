package dev.core.uikit.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/**
 * Masofa va o'lcham tokenlari (`design_handoff_studentclub_elonuz`).
 *
 * Hamma joyda `Dp` ishlatiladi — `Int` emas, aks holda birlik noaniq qoladi.
 */
@Immutable
object AppSpacing {
    /** 4.dp — ikonka va yorliq orasi. */
    val xs = 4.dp

    /** 8.dp — element ichidagi zich oraliq. */
    val sm = 8.dp

    /** 12.dp — karta ichidagi standart oraliq. */
    val md = 12.dp

    /** 16.dp — karta ichki paddingi. */
    val lg = 16.dp

    /** 20.dp — ekranning yon paddingi. */
    val screenHorizontal = 20.dp

    /** 22.dp — bo'limlar orasi. */
    val section = 22.dp

    /** 26.dp — ekranning pastki paddingi. */
    val screenBottom = 26.dp

    /** 18.dp — topbar ostidagi bo'shliq. */
    val xl = 18.dp
}

/** Komponent o'lchamlari — tegish maydoni 44.dp dan kichik bo'lmasligi kerak. */
@Immutable
object AppSize {
    /** CTA tugma. */
    val buttonHeight = 52.dp
    val buttonSecondaryHeight = 50.dp

    /** Input maydoni. */
    val fieldHeight = 54.dp

    /** Topbar'dagi kvadrat tugma. */
    val iconButton = 40.dp

    /** Bizneslarim ekranidagi kattaroq profil tugmasi. */
    val iconButtonLarge = 54.dp

    /** Karta ichidagi ikonka nishoni. */
    val iconTile = 42.dp

    /** Biznes kartasidagi katta kategoriya nishoni. */
    val categoryTile = 56.dp

    val tabHeight = 44.dp

    /** Ikonka o'lchamlari (handoff: ro'yxatda 24, topbarda 22, FABda 26). */
    val iconSm = 17.dp
    val iconMd = 19.dp
    val iconLg = 22.dp
    val iconFab = 26.dp

    /** Toggle tugmachasi. */
    val toggleWidth = 44.dp
    val toggleHeight = 26.dp
}

/**
 * Burchak radiuslari.
 *
 * Handoff: katta karta 20–26, kichik chip/tugma 14–18, to'liq aylana pill.
 */
@Immutable
object AppRadius {
    /** 14.dp — kichik ikonka tugmasi, chip. */
    val sm = RoundedCornerShape(14.dp)

    /** 16.dp — nishon (icon tile). */
    val md = RoundedCornerShape(16.dp)

    /** 18.dp — input maydoni, segment konteyneri. */
    val lg = RoundedCornerShape(18.dp)

    /** 20.dp — ro'yxat qatori kartasi. */
    val row = RoundedCornerShape(20.dp)

    /** 24.dp — asosiy karta. */
    val card = RoundedCornerShape(24.dp)

    /** 26.dp — CTA tugma (deyarli pill). */
    val button = RoundedCornerShape(26.dp)

    /** Gradient header ostki burchaklari. */
    val headerBottom = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)

    /** Modal sheet ustki burchaklari. */
    val sheetTop = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    /** To'liq dumaloq — avatar, nuqta, toggle. */
    val pill = RoundedCornerShape(999.dp)
}
