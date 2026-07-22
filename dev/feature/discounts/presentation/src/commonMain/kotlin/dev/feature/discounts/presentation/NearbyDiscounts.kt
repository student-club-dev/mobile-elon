package dev.feature.discounts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_branch_location
import dev.core.uikit.resources.discounts_location_off
import dev.core.uikit.resources.discounts_nearby_title
import dev.core.uikit.resources.discounts_price_sum
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.rowShadow
import dev.feature.discounts.domain.model.DiscountCard
import dev.feature.discounts.domain.model.DiscountQuery
import dev.feature.discounts.domain.model.formatSum
import dev.feature.discounts.domain.usecase.GetNearbyDiscountsUseCase
import dev.core.uikit.component.AppIcons
import dev.feature.discounts.presentation.components.ListingImage
import dev.feature.discounts.presentation.icon
import dev.core.uikit.map.rememberUserLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Talabaga ko'rinadigan faol chegirmalar — `GET /discounts`.
 *
 * Joylashuv ekrandan keladi va o'zgarganда ro'yxat qayta so'raladi: masofa va saralash
 * shunga bog'liq. Ruxsat berilmasa `lat`/`lng` bo'sh ketadi — backend bunda masofasiz
 * ro'yxat qaytaradi.
 */
class NearbyDiscountsViewModel(
    private val getNearbyDiscounts: GetNearbyDiscountsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<List<DiscountCard>>(emptyList())
    val state: StateFlow<List<DiscountCard>> = _state.asStateFlow()

    fun load(lat: Double?, lng: Double?) {
        viewModelScope.launch {
            _state.value = getNearbyDiscounts(DiscountQuery(lat = lat, lng = lng))
        }
    }
}

/**
 * "Yaqin atrofdagi chegirmalar" — Chegirmalar bo'limining tepasida.
 *
 * Har bir e'lon **eng yaqin filiali** va unga bo'lgan masofa bilan ko'rsatiladi.
 * Joylashuvga ruxsat berilmasa masofa ko'rsatilmaydi, lekin ro'yxat baribir ishlaydi.
 */
@Composable
fun NearbyDiscountsSection(
    modifier: Modifier = Modifier,
    vm: NearbyDiscountsViewModel = koinViewModel(),
) {
    val palette = appPalette
    val cards by vm.state.collectAsStateWithLifecycle()
    val userLocation = rememberUserLocation()

    // Joylashuv aniqlanganда (yoki rad etilganда) ro'yxat so'raladi — tartib va masofa
    // shunga bog'liq, shuning uchun u o'zgarsa qayta yuklanadi.
    LaunchedEffect(userLocation) {
        vm.load(userLocation?.lat, userLocation?.lng)
    }

    if (cards.isEmpty()) return

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.discounts_nearby_title),
                style = AppType.body.copy(fontWeight = AppType.buttonSecondary.fontWeight, color = palette.ink),
                modifier = Modifier.weight(1f),
            )
            if (userLocation == null) {
                Text(
                    stringResource(Res.string.discounts_location_off),
                    style = AppType.caption.copy(color = palette.inkFaint),
                )
            }
        }

        cards.forEach { card ->
            NearbyDiscountCard(card, card.nearest?.branch?.display(), card.nearest?.distanceLabel(), palette)
        }
    }
}

@Composable
private fun NearbyDiscountCard(
    card: DiscountCard,
    branchLabel: String?,
    distanceLabel: String?,
    palette: AppPalette,
) {
    // Aksent biznes TURIDAN keladi — ma'lumot, palitra tokeni emas.
    val accent = Color(card.businessType.accent)
    val cardShape = AppRadius.md
    val coverShape = AppRadius.sm

    // Chegara yo'q — oq kartani yumshoq soya ajratadi.
    Row(
        Modifier.fillMaxWidth().rowShadow(cardShape).clip(cardShape).background(palette.card)
            .padding(11.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier.size(58.dp).clip(coverShape).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            val cover = card.imageUrl
            if (cover != null) {
                ListingImage(cover, Modifier.fillMaxSize().clip(coverShape))
            } else {
                Icon(card.businessType.icon, null, tint = accent, modifier = Modifier.size(24.dp))
            }
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "${card.businessName} — ${card.title}",
                style = AppType.label.copy(fontWeight = AppType.buttonSecondary.fontWeight, color = palette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(Res.string.discounts_price_sum, card.finalPrice.formatSum()),
                    style = AppType.link.copy(
                        fontWeight = AppType.screenTitle.fontWeight,
                        color = palette.success,
                    ),
                )
                Text(
                    card.originalPrice.formatSum(),
                    style = AppType.caption.copy(
                        color = palette.inkFaint,
                        textDecoration = TextDecoration.LineThrough,
                    ),
                )
            }

            // Eng yaqin filial va masofa — talabaning asosiy savoli: "qayerda va qancha uzoq?"
            val meta = listOfNotNull(
                distanceLabel?.let { stringResource(Res.string.discounts_branch_location, it) },
                branchLabel,
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = AppType.caption.copy(color = palette.inkFaint),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            Modifier.clip(AppRadius.sm).background(accent)
                .padding(horizontal = 9.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Aksent fon USTIDA — kontent rangi `onPrimary`.
            Text(
                card.discount.badge(),
                style = AppType.fieldLabel.copy(
                    fontWeight = AppType.screenTitle.fontWeight,
                    color = palette.onPrimary,
                ),
            )
        }
    }
}
