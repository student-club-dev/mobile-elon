package dev.core.uikit.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/**
 * Masofa va o'lcham tokenlari.
 *
 * Ilgari komponentlar `horizontalPadding: Int = 22` kabi `Int` parametr olib, ichida `.dp`
 * qo'shardi — bu tur xavfsizligini yo'qotadi (`22` nima: dp, px, sp?). Endi hamma joyda
 * `Dp` ishlatiladi.
 */
@Immutable
object AppSpacing {
    /** 4.dp — ikonka va yorliq orasi. */
    val xs = 4.dp

    /** 8.dp — element ichidagi zich oraliq. */
    val sm = 8.dp

    /** 12.dp — karta ichidagi standart oraliq. */
    val md = 12.dp

    /** 16.dp — bloklar orasi. */
    val lg = 16.dp

    /** 22.dp — ekranning yon paddingi. */
    val screenHorizontal = 22.dp

    /** 26.dp — ekranning pastki paddingi. */
    val screenBottom = 26.dp

    /** 56.dp — ekranning tepa paddingi (status bar ostidan). */
    val screenTop = 56.dp

    /** 24.dp — katta bo'limlar orasi. */
    val xl = 24.dp
}

/** Komponent balandliklari — tegish maydoni 44.dp dan kichik bo'lmasligi kerak. */
@Immutable
object AppSize {
    val buttonHeight = 52.dp
    val buttonSecondaryHeight = 50.dp
    val fieldHeight = 50.dp
    val iconButton = 40.dp
    val tabHeight = 38.dp

    /** Ikonka o'lchamlari. */
    val iconSm = 15.dp
    val iconMd = 18.dp
    val iconLg = 21.dp

    /** Dekorativ blob. */
    val blob = 210.dp
}

/** Burchak radiuslari. */
@Immutable
object AppRadius {
    val sm = RoundedCornerShape(10.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(14.dp)
    val button = RoundedCornerShape(15.dp)
    val card = RoundedCornerShape(18.dp)

    /** To'liq dumaloq — avatar, nuqta, pill. */
    val pill = RoundedCornerShape(999.dp)
}
