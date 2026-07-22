package dev.feature.auth.presentation.main.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.domain.model.Conversation
import dev.core.domain.model.Message
import dev.core.uikit.component.AppIcons
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.ctaShadow
import dev.core.uikit.theme.rowShadow

/** Pufak burchagi — "dumcha" tomonda kichik, qolganida 16 (handoff, 6-ekran). */
private val BubbleCorner = 16.dp
private val BubbleTail = 4.dp
private val BubbleMaxWidth = 280.dp

/** Sana chipi va "yozmoqda" pufagi — ro'yxat kartalaridan kichikroq radius. */
private val DateChipShape = RoundedCornerShape(10.dp)
private val TypingShape = RoundedCornerShape(BubbleCorner)

/** Nishon o'lchamlari (handoff: 50dp nishon, radius 16). */
private val AvatarTileSize = 50.dp

/**
 * Suhbat qatori — rangli bosh harf nishoni, oxirgi xabar va o'qilmaganlar hisoblagichi.
 * Uzoq bosishda arxivlash/o'chirish menyusi ochiladi.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ConversationRow(
    c: Conversation,
    palette: AppPalette,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val shape = AppRadius.row
    val accent = conversationAccent(c.id, palette)
    Row(
        // Chegara emas — oq karta va yumshoq soya (yangi dizayn tili).
        Modifier.fillMaxWidth().rowShadow(shape).clip(shape).background(palette.card)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box {
            // Nishon yumaloq kvadrat (radius 16) — handoff'da suhbatdosh rasmi o'rnida
            // ismning bosh harfi rangli fonda turadi.
            Box(
                Modifier.size(AvatarTileSize).clip(AppRadius.md).background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(c.peerInitial, style = AppType.button.copy(fontSize = 19.sp, color = accent))
            }
            // Onlayn nuqtasi atrofidagi halqa — karta rangida (qorong'ida quyuq).
            if (c.online) OnlineDot(palette, ringColor = palette.card, modifier = Modifier.align(Alignment.BottomEnd))
        }
        Column(Modifier.weight(1f)) {
            Text(
                c.peerName,
                style = AppType.rowTitle.copy(fontWeight = AppType.button.fontWeight, color = palette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                c.lastMessage,
                style = AppType.subtitle.copy(fontSize = 13.sp, color = palette.inkMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(c.lastTime, style = AppType.caption.copy(color = palette.inkFaint))
            if (c.unreadCount > 0) {
                Box(
                    Modifier.size(18.dp).clip(AppRadius.pill).background(palette.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${c.unreadCount}",
                        style = AppType.caption.copy(
                            fontSize = 10.sp,
                            fontWeight = AppType.screenTitle.fontWeight,
                            color = palette.onPrimary,
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Suhbat nishonining rangi — handoff'da har bir qator boshqa aksentda (binafsha, sariq...).
 * Rang suhbat id'sidan barqaror hosil qilinadi: ro'yxat qayta yuklanganda ham o'zgarmaydi.
 */
private fun conversationAccent(id: String, palette: AppPalette): Color {
    val accents = listOf(
        palette.accentGame,
        palette.accentFood,
        palette.accentStudy,
        palette.accentClothing,
        palette.accentBarber,
        palette.accentCinema,
        palette.accentBeauty,
    )
    val index = ((id.hashCode() % accents.size) + accents.size) % accents.size
    return accents[index]
}

/** Avatar burchagidagi "onlayn" nuqtasi — yuza rangidagi halqa ichida yashil doira. */
@Composable
internal fun OnlineDot(palette: AppPalette, ringColor: Color, modifier: Modifier = Modifier) {
    Box(modifier.size(12.dp).clip(AppRadius.pill).background(ringColor).padding(2.dp)) {
        Box(Modifier.fillMaxSize().clip(AppRadius.pill).background(palette.success))
    }
}

/** Xabarlar oqimi ustidagi sana chipi — "Bugun". */
@Composable
internal fun DateChip(label: String, palette: AppPalette) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            label,
            style = AppType.caption.copy(fontWeight = AppType.label.fontWeight, color = palette.inkMuted),
            modifier = Modifier.clip(DateChipShape).background(palette.divider)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
        )
    }
}

/**
 * Xabar pufagi — chiquvchi brend gradientida (ko'k soya bilan), kiruvchi oq kartada.
 * Chiquvchi pufakda yuborilganlik belgisi (ikki galochka) ham chiziladi.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MessageBubble(message: Message, palette: AppPalette, onLongPress: () -> Unit) {
    val align = if (message.outgoing) Alignment.CenterEnd else Alignment.CenterStart
    Box(Modifier.fillMaxWidth(), contentAlignment = align) {
        val shape = if (message.outgoing) {
            RoundedCornerShape(BubbleCorner, BubbleCorner, BubbleTail, BubbleCorner)
        } else {
            RoundedCornerShape(BubbleCorner, BubbleCorner, BubbleCorner, BubbleTail)
        }
        // Chiquvchi pufakni ko'k "yorug'lik", kiruvchini esa neytral yumshoq soya ajratadi.
        val surface = if (message.outgoing) Modifier.ctaShadow(shape) else Modifier.rowShadow(shape)
        val body = Modifier.widthIn(max = BubbleMaxWidth).then(surface).clip(shape)
        Column(
            (if (message.outgoing) body.background(palette.primaryBrush) else body.background(palette.card))
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(horizontal = 14.dp, vertical = 11.dp),
        ) {
            Text(
                message.text,
                style = AppType.subtitle.copy(
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp,
                    // Gradient USTIDAGI matn — har ikkala rejimda oq.
                    color = if (message.outgoing) Color.White else palette.ink,
                ),
            )
            Spacer(Modifier.height(5.dp))
            Row(
                Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    message.time,
                    style = AppType.caption.copy(
                        fontSize = 11.sp,
                        color = if (message.outgoing) Color.White.copy(alpha = 0.85f) else palette.inkFaint,
                    ),
                )
                if (message.outgoing) {
                    Icon(
                        AppIcons.CheckDouble,
                        null,
                        // Gradient USTIDAGI ikonka — oq.
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

/**
 * "Yozmoqda" indikatori — oq pufak ichida ketma-ket pulsatsiyalanuvchi uchta nuqta.
 * Har bir nuqta o'z kechikishi bilan yonadi, shuning uchun to'lqin chapdan o'ngga yuguradi.
 */
@Composable
internal fun TypingIndicator(palette: AppPalette) {
    val transition = rememberInfiniteTransition(label = "typing")
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Row(
            Modifier.rowShadow(TypingShape).clip(TypingShape).background(palette.card)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            repeat(3) { index ->
                val alpha by transition.animateFloat(
                    initialValue = 0.30f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 520, delayMillis = index * 180, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "typing_dot_$index",
                )
                Box(Modifier.size(6.dp).clip(AppRadius.pill).background(palette.inkFaint.copy(alpha = alpha)))
            }
        }
    }
}

/** Dialog ichidagi amal qatori — "Arxivlash", "O'chirish". */
@Composable
internal fun ActionRow(
    icon: ImageVector,
    label: String,
    palette: AppPalette,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    val tint = if (danger) palette.danger else palette.primary
    Row(
        Modifier.fillMaxWidth().clip(AppRadius.md).clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Text(
            label,
            style = AppType.body.copy(
                fontWeight = AppType.label.fontWeight,
                color = if (danger) tint else palette.ink,
            ),
        )
    }
}
