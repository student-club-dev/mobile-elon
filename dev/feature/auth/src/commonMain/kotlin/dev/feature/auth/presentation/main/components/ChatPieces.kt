package dev.feature.auth.presentation.main.components

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
import dev.core.uikit.component.MonogramTile
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.rowShadow

/**
 * Suhbat qatori — avatar, oxirgi xabar va o'qilmaganlar hisoblagichi.
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
    val shape = AppRadius.md
    Row(
        // Chegara emas — oq karta va yumshoq soya (yangi dizayn tili).
        Modifier.fillMaxWidth().rowShadow(shape).clip(shape).background(palette.card)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box {
            MonogramTile(c.peerInitial, size = 46.dp)
            // Onlayn nuqtasi atrofidagi halqa — ekran yuzasi rangida (qorong'ida quyuq).
            if (c.online) OnlineDot(palette, ringColor = palette.card, modifier = Modifier.align(Alignment.BottomEnd))
        }
        Column(Modifier.weight(1f)) {
            Text(
                c.peerName,
                style = AppType.subtitle.copy(fontWeight = AppType.button.fontWeight, color = palette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                c.lastMessage,
                style = AppType.hint.copy(color = palette.inkFaint),
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

/** Avatar burchagidagi "onlayn" nuqtasi — yuza rangidagi halqa ichida yashil doira. */
@Composable
internal fun OnlineDot(palette: AppPalette, ringColor: Color, modifier: Modifier = Modifier) {
    Box(modifier.size(12.dp).clip(AppRadius.pill).background(ringColor).padding(2.dp)) {
        Box(Modifier.fillMaxSize().clip(AppRadius.pill).background(palette.success))
    }
}

/** Xabar pufagi — chiquvchi xabar brend rangida, kiruvchi shisha yuzada. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MessageBubble(message: Message, palette: AppPalette, onLongPress: () -> Unit) {
    val align = if (message.outgoing) Alignment.CenterEnd else Alignment.CenterStart
    Box(Modifier.fillMaxWidth(), contentAlignment = align) {
        val corner = 16.dp
        val shape = if (message.outgoing) {
            RoundedCornerShape(corner, corner, AppSpacing.xs, corner)
        } else {
            RoundedCornerShape(corner, corner, corner, AppSpacing.xs)
        }
        // Kiruvchi pufak oq karta — uni chegara emas, soya ajratadi.
        val surface = if (message.outgoing) Modifier else Modifier.rowShadow(shape)
        Column(
            Modifier.widthIn(max = 280.dp).then(surface).clip(shape)
                .background(if (message.outgoing) palette.primary else palette.card)
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(horizontal = 13.dp, vertical = 9.dp),
        ) {
            Text(
                message.text,
                style = AppType.subtitle.copy(color = if (message.outgoing) palette.onPrimary else palette.ink),
            )
            Text(
                message.time,
                style = AppType.caption.copy(
                    fontSize = 9.sp,
                    color = if (message.outgoing) palette.onPrimary.copy(alpha = 0.7f) else palette.inkFaint,
                ),
                modifier = Modifier.align(Alignment.End),
            )
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
