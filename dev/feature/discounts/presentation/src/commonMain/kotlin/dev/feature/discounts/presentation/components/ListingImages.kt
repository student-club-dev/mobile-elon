package dev.feature.discounts.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.core.uikit.component.AppIcons
import dev.core.uikit.media.toImageBitmapOrNull
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_delete
import dev.core.uikit.resources.discounts_image
import dev.core.uikit.resources.discounts_image_uploading
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.jetbrains.compose.resources.stringResource

/**
 * E'lon rasmi. Offline rejimda rasm `data:image/...;base64,...` ko'rinishida bo'ladi
 * (backend yo'q — hech qayerga yuklanmaydi), backend yoqilganda esa oddiy CDN havolasi.
 * Shu sabab ikkala holat ham qo'llab-quvvatlanadi.
 */
@Composable
fun ListingImage(source: String, modifier: Modifier = Modifier) {
    val bitmap = remember(source) { source.decodeDataUri()?.toImageBitmapOrNull() }
    if (bitmap != null) {
        Image(bitmap, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        AsyncImage(
            model = source,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}

@OptIn(ExperimentalEncodingApi::class)
private fun String.decodeDataUri(): ByteArray? {
    if (!startsWith("data:")) return null
    val payload = substringAfter("base64,", missingDelimiterValue = "")
    if (payload.isEmpty()) return null
    return runCatching { Base64.decode(payload) }.getOrNull()
}

/** Rasm qo'shish katakchasi (bo'sh joy) — bosilganda galereya ochiladi. */
@Composable
fun AddImageTile(onClick: () -> Unit, loading: Boolean, palette: AppPalette = appPalette) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.size(84.dp)
            .clip(shape)
            .background(palette.primary.copy(alpha = 0.06f))
            .border(1.dp, palette.border, shape)
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            Text(
                stringResource(Res.string.discounts_image_uploading),
                style = AppType.cardTitle.copy(fontWeight = FontWeight.Normal, color = palette.inkFaint),
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Icon(AppIcons.ImageIcon, null, tint = palette.inkFaint, modifier = Modifier.size(20.dp))
                Text(
                    stringResource(Res.string.discounts_image),
                    style = AppType.caption.copy(fontSize = 10.sp, fontWeight = AppType.label.fontWeight, color = palette.inkFaint),
                )
            }
        }
    }
}

/** Qo'shilgan rasm — burchagida o'chirish tugmasi. */
@Composable
fun ImageThumb(source: String, onRemove: () -> Unit, palette: AppPalette = appPalette) {
    val shape = RoundedCornerShape(14.dp)
    Box(Modifier.size(84.dp)) {
        ListingImage(source, Modifier.fillMaxSize().clip(shape).border(1.dp, palette.border, shape))
        Box(
            Modifier.align(Alignment.TopEnd)
                .padding(AppSpacing.xs)
                .size(20.dp)
                .clip(CircleShape)
                // Rasm ustidagi quyuq qoplama — palitradagi `scrim` tokeni.
                .background(palette.scrim)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppIcons.Close,
                stringResource(Res.string.common_delete),
                tint = palette.onPrimary,
                modifier = Modifier.size(11.dp),
            )
        }
    }
}

