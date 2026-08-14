package dev.feature.discounts.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import dev.core.uikit.resources.discounts_image_cover
import dev.core.uikit.resources.discounts_image_uploading
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
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

/**
 * Rasm qo'shish katakchasi (bo'sh joy) — bosilganda galereya ochiladi.
 *
 * Chegara **uzuq chiziqli**: chegarasiz katakning ochiq ko'k foni sahifa foniga qo'shilib
 * ketardi va u umuman bosiladigan joyга o'xshamasdi ("rasm yuklash borderi yo'q").
 * Uzuq chiziq esa "bu yerga qo'shing" degan universal belgi.
 */
@Composable
fun AddImageTile(onClick: () -> Unit, loading: Boolean, palette: AppPalette = appPalette) {
    val shape = AppRadius.sm
    Box(
        Modifier.size(84.dp)
            .clip(shape)
            .background(palette.accentBg)
            .dashedBorder(palette.primary.copy(alpha = 0.55f), shape)
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

/**
 * Uzuq chiziqli chegara.
 *
 * Compose'da `Modifier.border` uzuq chiziqni qo'llab-quvvatlamaydi, shuning uchun kontur
 * qo'lda chiziladi. Radius [shape] dan olinadi — chegara katakning yumaloq burchaklariga
 * aynan tushishi kerak.
 */
private fun Modifier.dashedBorder(color: Color, shape: RoundedCornerShape): Modifier =
    drawBehind {
        val radius = shape.topStart.toPx(size, this)
        val stroke = 1.5.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(
                width = stroke,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
            ),
        )
    }

/** Qo'shilgan rasm — burchagida o'chirish tugmasi. */
@Composable
fun ImageThumb(
    source: String,
    onRemove: () -> Unit,
    palette: AppPalette = appPalette,
    /** Birinchi rasm — e'lonning muqovasi (backend `images[0]` ni shunday oladi). */
    cover: Boolean = false,
) {
    val shape = AppRadius.sm
    Box(Modifier.size(84.dp)) {
        ListingImage(source, Modifier.fillMaxSize().clip(shape))
        if (cover) {
            Text(
                stringResource(Res.string.discounts_image_cover),
                style = AppType.caption.copy(color = palette.onPrimary),
                modifier = Modifier.align(Alignment.BottomStart)
                    .padding(AppSpacing.xs)
                    .clip(AppRadius.pill)
                    .background(palette.scrim)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            )
        }
        Box(
            Modifier.align(Alignment.TopEnd)
                .padding(AppSpacing.xs)
                .size(20.dp)
                .clip(AppRadius.pill)
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

