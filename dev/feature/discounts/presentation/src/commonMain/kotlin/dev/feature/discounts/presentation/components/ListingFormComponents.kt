package dev.feature.discounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import coil3.compose.AsyncImage
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.media.toImageBitmapOrNull
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Bo'lim yon (horizontal) padding'i — flat dizaynда sarlavha/maydonlar shu qadar ichkariда. */
val SectionHPad = 16.dp

/**
 * Forma bo'limi — FLAT (kartasiz): faqat sarlavha + izoh + tarkib. Kartachalar yo'q, hamma narsa
 * fonда to'g'ridan-to'g'ri. Yon padding shu yerda beriladi (horizontal scroll bo'limlari esa
 * o'zi edge-to-edge chiqadi).
 */
@Composable
fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    error: String? = null,
    palette: AppPalette = appPalette,
    content: @Composable () -> Unit,
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = SectionHPad),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionHeader(title, subtitle, palette)
        content()
        if (error != null) {
            Text(
                error,
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.Bold, color = ErrorColor),
            )
        }
    }
}

/** Bo'lim sarlavhasi (flat). Horizontal-scroll bo'limlarда alohida ishlatiladi. */
@Composable
fun SectionHeader(title: String, subtitle: String?, palette: AppPalette = appPalette) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            title,
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Black, color = palette.ink),
        )
        if (subtitle != null) {
            Text(
                subtitle,
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = palette.inkFaint),
            )
        }
    }
}

/** Xato matni va chegarasining rangi (palitrada xato rangi yo'q). */
val ErrorColor = Color(0xFFEF4444)

/**
 * Tanlanadigan chip — iym-native uikit2 uslubi: 36dp balandlik, yumshoq squircle-simon shakl,
 * tanlanганда SOLID brand fon (gradient emas), tanlanmaganда nozik chegara.
 */
@Composable
fun SelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    palette: AppPalette = appPalette,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .height(36.dp)
            .clip(shape)
            .background(if (selected) palette.primary else palette.fieldBg)
            .then(if (selected) Modifier else Modifier.border(1.dp, palette.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (selected) {
            Icon(AppIcons.Check, null, tint = palette.onPrimary, modifier = Modifier.size(14.dp))
        }
        Text(
            text,
            style = TextStyle(
                fontFamily = AppFontFamily,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) palette.onPrimary else palette.inkMuted,
            ),
        )
    }
}

/** Chip'lar qatori — sig'masa keyingi qatorga o'tadi. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipFlow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) { content() }
}

/** Ochiluvchi ro'yxat (viloyat, tuman, SELECT turidagi maydonlar). */
@Composable
fun FormDropdown(
    value: String?,
    placeholder: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    palette: AppPalette = appPalette,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.firstOrNull { it.first == value }?.second

    Box(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(palette.fieldBg)
                .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label ?: placeholder,
                style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 13.sp,
                    fontWeight = if (label != null) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        !enabled -> palette.inkFaint
                        label != null -> palette.ink
                        else -> palette.inkFaint
                    },
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(AppIcons.ChevronDown, null, tint = palette.chevron, modifier = Modifier.size(18.dp))
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text,
                            style = TextStyle(
                                fontFamily = AppFontFamily,
                                fontSize = 13.sp,
                                fontWeight = if (key == value) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (key == value) palette.primary else palette.ink,
                            ),
                        )
                    },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Ha/yo'q almashtirgich (BOOLEAN turidagi maydonlar). */
@Composable
fun FormSwitch(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    palette: AppPalette = appPalette,
) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = palette.inkMuted),
            modifier = Modifier.weight(1f),
        )
        Box(
            Modifier.size(width = 44.dp, height = 26.dp)
                .clip(CircleShape)
                .background(if (checked) palette.primary else palette.tabTrack),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                Modifier.padding(horizontal = 3.dp).size(20.dp).clip(CircleShape).background(Color.White),
            )
        }
    }
}

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
    Box(
        Modifier.size(84.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.primary.copy(alpha = 0.06f))
            .border(1.dp, palette.border, RoundedCornerShape(14.dp))
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            Text("...", style = TextStyle(fontFamily = AppFontFamily, fontSize = 16.sp, color = palette.inkFaint))
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(AppIcons.ImageIcon, null, tint = palette.inkFaint, modifier = Modifier.size(20.dp))
                Text("Rasm", style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = palette.inkFaint))
            }
        }
    }
}

/** Qo'shilgan rasm — burchagida o'chirish tugmasi. */
@Composable
fun ImageThumb(source: String, onRemove: () -> Unit, palette: AppPalette = appPalette) {
    Box(Modifier.size(84.dp)) {
        ListingImage(
            source,
            Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).border(1.dp, palette.border, RoundedCornerShape(14.dp)),
        )
        Box(
            Modifier.align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Close, "O'chirish", tint = Color.White, modifier = Modifier.size(11.dp))
        }
    }
}

/** Statusni ko'rsatuvchi rangli yorliq ("Faol", "Qoralama"). */
@Composable
fun StatusPill(text: String, color: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(
            text,
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, fontWeight = FontWeight.ExtraBold, color = color),
        )
    }
}

@Composable
fun IconSquareButton(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, palette: AppPalette = appPalette) {
    Box(
        Modifier.size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.glass)
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = palette.ink, modifier = Modifier.size(18.dp)) }
}

@Composable
fun FormSpacer(height: Int = 12) = Spacer(Modifier.height(height.dp))
