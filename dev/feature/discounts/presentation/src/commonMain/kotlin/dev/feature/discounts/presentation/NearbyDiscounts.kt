package dev.feature.discounts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.formatSum
import dev.feature.discounts.domain.usecase.ObserveActiveListingsUseCase
import dev.feature.discounts.presentation.components.ListingImage
import dev.feature.discounts.presentation.map.rememberUserLocation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.compose.viewmodel.koinViewModel

/** Talabaga ko'rinadigan faol chegirmalar (biznes egalari yuklagan e'lonlar). */
class NearbyDiscountsViewModel(
    observeActive: ObserveActiveListingsUseCase,
) : ViewModel() {

    val state: StateFlow<List<Listing>> = observeActive()
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
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
    val listings by vm.state.collectAsStateWithLifecycle()
    val userLocation = rememberUserLocation()

    if (listings.isEmpty()) return

    // Eng yaqinlari yuqorida. Masofa noma'lum bo'lsa tartib o'zgarmaydi.
    val sorted = remember(listings, userLocation) {
        listings
            .map { it to it.nearestBranch(userLocation?.lat, userLocation?.lng) }
            .sortedBy { (_, nearest) -> nearest?.distanceMeters ?: Double.MAX_VALUE }
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Yaqin atrofdagi chegirmalar",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
                modifier = Modifier.weight(1f),
            )
            if (userLocation == null) {
                Text(
                    "joylashuv o'chiq",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, color = palette.inkFaint),
                )
            }
        }

        sorted.forEach { (listing, nearest) ->
            NearbyDiscountCard(listing, nearest?.branch?.display(), nearest?.distanceLabel(), palette)
        }
    }
}

@Composable
private fun NearbyDiscountCard(
    listing: Listing,
    branchLabel: String?,
    distanceLabel: String?,
    palette: AppPalette,
) {
    val accent = Color(listing.businessType.accent)

    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(palette.glass)
            .border(1.dp, palette.border, RoundedCornerShape(16.dp)).padding(11.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier.size(58.dp).clip(RoundedCornerShape(13.dp)).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            val cover = listing.images.firstOrNull()
            if (cover != null) {
                ListingImage(cover, Modifier.fillMaxSize().clip(RoundedCornerShape(13.dp)))
            } else {
                Text(listing.businessType.emoji, style = TextStyle(fontSize = 22.sp))
            }
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "${listing.businessName} — ${listing.title}",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${listing.finalPrice.formatSum()} so'm",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Black, color = palette.successDeep),
                )
                Text(
                    listing.originalPrice.formatSum(),
                    style = TextStyle(
                        fontFamily = AppFontFamily,
                        fontSize = 11.sp,
                        color = palette.inkFaint,
                        textDecoration = TextDecoration.LineThrough,
                    ),
                )
            }

            // Eng yaqin filial va masofa — talabaning asosiy savoli: "qayerda va qancha uzoq?"
            val meta = listOfNotNull(
                distanceLabel?.let { "📍 $it" },
                branchLabel,
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, color = palette.inkFaint),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            Modifier.clip(RoundedCornerShape(10.dp)).background(accent).padding(horizontal = 9.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                listing.discount.badge(),
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White),
            )
        }
    }
}
