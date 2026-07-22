package dev.feature.business

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.business_type_auto
import dev.core.uikit.resources.business_type_barber
import dev.core.uikit.resources.business_type_beauty
import dev.core.uikit.resources.business_type_cafe
import dev.core.uikit.resources.business_type_cinema
import dev.core.uikit.resources.business_type_clothing
import dev.core.uikit.resources.business_type_education
import dev.core.uikit.resources.business_type_electronics
import dev.core.uikit.resources.business_type_flowers
import dev.core.uikit.resources.business_type_game_club
import dev.core.uikit.resources.business_type_grocery
import dev.core.uikit.resources.business_type_gym
import dev.core.uikit.resources.business_type_other
import dev.core.uikit.resources.business_type_pharmacy
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Biznes turlari — horizontal scroll qilinadi. Bir nechta ekran ishlatadi.
 *
 * [id] — bazaga/backendga YOZILADIGAN barqaror qiymat, u tarjima qilinmaydi (aks holda
 * tilni almashtirgan foydalanuvchining saqlangan turi yo'qolardi). [label] esa ekranda
 * ko'rsatiladigan tarjima qilinadigan matn.
 */
internal enum class BusinessType(val id: String, val label: StringResource) {
    CAFE("Kafe va Restoran", Res.string.business_type_cafe),
    GROCERY("Oziq-ovqat", Res.string.business_type_grocery),
    CLOTHING("Kiyim-kechak", Res.string.business_type_clothing),
    GAME_CLUB("Game Club", Res.string.business_type_game_club),
    EDUCATION("O'quv markazlar", Res.string.business_type_education),
    CINEMA("Kino va ko'ngilochar", Res.string.business_type_cinema),
    ELECTRONICS("Texnikalar", Res.string.business_type_electronics),
    BARBER("Sartaroshxona", Res.string.business_type_barber),
    BEAUTY("Go'zallik saloni", Res.string.business_type_beauty),
    GYM("Sport zal", Res.string.business_type_gym),
    AUTO("Avto xizmat", Res.string.business_type_auto),
    PHARMACY("Apteka", Res.string.business_type_pharmacy),
    FLOWERS("Gul do'koni", Res.string.business_type_flowers),
    OTHER("Boshqa", Res.string.business_type_other),
}

/** Tanlash ro'yxati uchun turlar — ko'rsatilish tartibi enum tartibiga teng. */
internal val businessTypes: List<BusinessType> = BusinessType.entries

/**
 * Saqlangan tur qiymatini ekranda ko'rsatiladigan matnga aylantiradi.
 * Ro'yxatda yo'q qiymat (masalan backend'dan kelgan yangi tur) o'zgarishsiz qaytariladi.
 */
@Composable
internal fun businessTypeLabel(id: String): String =
    businessTypes.firstOrNull { it.id == id }?.let { stringResource(it.label) } ?: id

/** Biznes turi uchun chip — tanlash ro'yxatlarida ishlatiladi. */
@Composable
internal fun TypeChip(type: BusinessType, active: Boolean, onClick: () -> Unit, palette: AppPalette) {
    val shape = RoundedCornerShape(13.dp)
    Row(
        Modifier
            .height(44.dp)
            .clip(shape)
            .background(if (active) palette.accentBg else palette.card)
            .then(if (active) Modifier.border(1.5.dp, palette.primary, shape) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(type.label),
            style = AppType.label.copy(
                fontSize = 12.5f.sp,
                // Tanlangan chip qalinroq: ExtraBold (tugma) / SemiBold (kuchaytirilgan tana matni).
                fontWeight = if (active) AppType.button.fontWeight else AppType.bodyStrong.fontWeight,
                color = if (active) palette.primary else palette.inkMuted,
            ),
        )
    }
}
