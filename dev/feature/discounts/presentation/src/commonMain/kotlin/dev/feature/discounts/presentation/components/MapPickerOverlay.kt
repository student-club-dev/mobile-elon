package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_map_my_location
import dev.core.uikit.resources.discounts_search_empty
import dev.core.uikit.resources.discounts_searching
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.cardShadow
import dev.core.uikit.theme.rowShadow
import dev.feature.discounts.domain.repository.PlaceSuggestion
import org.jetbrains.compose.resources.stringResource

/**
 * Xarita ustidagi qidiruv natijalari ro'yxati.
 *
 * Biznes joyini va e'lon filialini tanlash ekranlari bir xil xaritani ko'rsatadi, shuning
 * uchun bu panel ham bitta joyda turadi — ilgari ikkala ekran uni alohida chizardi va
 * ular bir-biridan farq qilib qolgan edi (birida tavsif va "qidirilmoqda" bor edi,
 * ikkinchisida yo'q).
 *
 * @param searching so'rov ketyapti
 * @param searched kamida bir marta qidirildi — bo'sh ro'yxatni "topilmadi" deb ko'rsatish uchun
 * @param error geokoderga yetib borilmadi. Bu "topilmadi" DAN farq qiladi: birinchisida
 *   foydalanuvchi so'rovni o'zgartiradi, ikkinchisida esa aloqani tekshiradi.
 */
@Composable
fun BoxScope.MapSearchResults(
    results: List<PlaceSuggestion>,
    searching: Boolean,
    searched: Boolean,
    onSelect: (PlaceSuggestion) -> Unit,
    palette: AppPalette,
    error: String? = null,
) {
    val showError = error != null && !searching
    val showEmpty = searched && !searching && !showError && results.isEmpty()
    if (!searching && !showEmpty && !showError && results.isEmpty()) return

    Column(
        Modifier.align(Alignment.TopCenter)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
            .fillMaxWidth()
            // Xarita ustidagi panel — oq karta, uni chegara emas soya ajratadi.
            .cardShadow(AppRadius.lg)
            .clip(AppRadius.lg)
            .background(palette.card),
    ) {
        when {
            searching -> Text(
                stringResource(Res.string.discounts_searching),
                style = AppType.fieldLabel.copy(color = palette.inkMuted),
                modifier = Modifier.padding(AppSpacing.md),
            )

            showError -> Text(
                error.orEmpty(),
                style = AppType.fieldLabel.copy(color = palette.danger),
                modifier = Modifier.padding(AppSpacing.md),
            )

            showEmpty -> Text(
                stringResource(Res.string.discounts_search_empty),
                style = AppType.fieldLabel.copy(color = palette.inkMuted),
                modifier = Modifier.padding(AppSpacing.md),
            )

            else -> results.forEach { place ->
                Column(
                    Modifier.fillMaxWidth()
                        .clickable { onSelect(place) }
                        .padding(horizontal = AppSpacing.md, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        place.title,
                        style = AppType.label.copy(
                            fontWeight = AppType.buttonSecondary.fontWeight,
                            color = palette.ink,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // Tavsif joyni aniqlashtiradi — bir xil nomli ikki do'kon farqlanadi.
                    if (place.subtitle.isNotBlank()) {
                        Text(
                            place.subtitle,
                            style = AppType.caption.copy(color = palette.inkFaint),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/**
 * "Mening joylashuvim" tugmasi — xaritani foydalanuvchi turgan joyga qaytaradi.
 *
 * Xarita ochilishida bir marta joylashuvga markazlashadi, lekin foydalanuvchi surib
 * ketgach qaytish yo'li yo'q edi. Tugma WebView ichida emas, Compose'da — shunda ilova
 * dizayn tizimiga mos bo'ladi va qo'shimcha tarmoq so'rovi ketmaydi.
 *
 * @param bottomPadding pastdagi tasdiqlash tugmasi balandligiga qarab beriladi
 */
@Composable
fun BoxScope.MyLocationButton(
    onClick: () -> Unit,
    palette: AppPalette,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = AppSpacing.lg,
) {
    Row(
        modifier
            .align(Alignment.BottomStart)
            .padding(start = AppSpacing.md, bottom = bottomPadding)
            .rowShadow(AppRadius.md)
            .clip(AppRadius.md)
            .background(palette.card)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(AppIcons.Locate, null, tint = palette.primary, modifier = Modifier.size(16.dp))
        Text(
            stringResource(Res.string.discounts_map_my_location),
            style = AppType.fieldLabel.copy(
                fontWeight = AppType.buttonSecondary.fontWeight,
                color = palette.ink,
            ),
        )
    }
}
