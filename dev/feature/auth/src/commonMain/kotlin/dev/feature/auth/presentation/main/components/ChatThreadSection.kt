package dev.feature.auth.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.domain.model.Conversation
import dev.core.domain.model.ConversationType
import dev.core.domain.model.Message
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GradientHeader
import dev.core.uikit.component.HeaderIconButton
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_chat_call
import dev.core.uikit.resources.auth_chat_clear_confirm
import dev.core.uikit.resources.auth_chat_clear_title
import dev.core.uikit.resources.auth_chat_message_delete_confirm
import dev.core.uikit.resources.auth_chat_message_delete_title
import dev.core.uikit.resources.auth_chat_offline
import dev.core.uikit.resources.auth_chat_online
import dev.core.uikit.resources.auth_chat_support
import dev.core.uikit.resources.auth_chat_today
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_clear
import dev.core.uikit.resources.common_delete
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import org.jetbrains.compose.resources.stringResource

/** Gradient header ostki burchagi (handoff, 6-ekran: 26px). */
private val HeaderCorner = 26.dp
private val HeaderButtonSize = 36.dp
private val HeaderAvatarSize = 42.dp

/**
 * 1y — suhbat oynasi (handoff'ning 6- va 7-ekrani).
 *
 * Tuzilishi: ko'k gradient header (qotib turadi) → xabarlar oqimi (faqat shu qism suriladi) →
 * oq kiritish paneli. [showTyping] `true` bo'lganda oxirida "yozmoqda" pufagi chiqadi.
 */
@Composable
internal fun ChatThread(
    conversation: Conversation,
    messages: List<Message>,
    draft: String,
    palette: AppPalette,
    onBack: () -> Unit,
    onDraft: (String) -> Unit,
    onSend: () -> Unit,
    onDeleteMessage: (String) -> Unit,
    onClearMessages: () -> Unit,
    showTyping: Boolean = false,
) {
    var messageToDelete by remember { mutableStateOf<Message?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    // Ro'yxatdagi elementlar soni: sana chipi + xabarlar + "yozmoqda" pufagi.
    val itemCount = (if (messages.isEmpty()) 0 else 1) + messages.size + (if (showTyping) 1 else 0)
    // Klaviatura balandligi — o'zgarganda pastga suramiz (Telegram kabi).
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(itemCount, imeBottom) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    // imePadding — klaviatura chiqqanda faqat pastki qism (input) tepaga suriladi,
    // header tepada qotib qoladi, xabarlar bo'limi qisqaradi (surilmaydi).
    Column(Modifier.fillMaxSize().imePadding()) {
        ChatThreadHeader(
            conversation = conversation,
            palette = palette,
            showClear = messages.isNotEmpty(),
            onBack = onBack,
            onClear = { showClearConfirm = true },
        )

        // Xabarlar — FAQAT shu qism scroll bo'ladi
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Handoff'da oqim boshida bitta sana chipi turadi.
            if (messages.isNotEmpty()) {
                item("chat_date") { DateChip(stringResource(Res.string.auth_chat_today), palette) }
            }
            items(messages, key = { it.id }) { m ->
                MessageBubble(m, palette, onLongPress = { messageToDelete = m })
            }
            if (showTyping) item("chat_typing") { TypingIndicator(palette) }
        }

        ChatInputBar(draft = draft, palette = palette, onDraft = onDraft, onSend = onSend)
    }

    val target = messageToDelete
    if (target != null) {
        ConfirmDialog(
            title = stringResource(Res.string.auth_chat_message_delete_title),
            message = stringResource(Res.string.auth_chat_message_delete_confirm),
            confirmLabel = stringResource(Res.string.common_delete),
            palette = palette,
            onConfirm = { onDeleteMessage(target.id); messageToDelete = null },
            onDismiss = { messageToDelete = null },
        )
    }

    if (showClearConfirm) {
        ConfirmDialog(
            title = stringResource(Res.string.auth_chat_clear_title),
            message = stringResource(Res.string.auth_chat_clear_confirm),
            confirmLabel = stringResource(Res.string.common_clear),
            palette = palette,
            onConfirm = { onClearMessages(); showClearConfirm = false },
            onDismiss = { showClearConfirm = false },
        )
    }
}

/**
 * Suhbat sarlavhasi — ko'k gradient blok.
 *
 * Ichidagi kontent ranglari palitradan OLINMAYDI: hammasi gradient USTIDA turadi va
 * yorug'/qorong'i rejimda bir xil oq bo'lishi kerak (`GradientHeader` qoidasi).
 */
@Composable
private fun ChatThreadHeader(
    conversation: Conversation,
    palette: AppPalette,
    showClear: Boolean,
    onBack: () -> Unit,
    onClear: () -> Unit,
) {
    val support = conversation.type == ConversationType.SUPPORT
    val title = if (support) stringResource(Res.string.auth_chat_support) else conversation.peerName
    GradientHeader(cornerRadius = HeaderCorner, palette = palette) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xl).padding(top = 54.dp, bottom = AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            HeaderIconButton(
                AppIcons.ArrowLeft,
                onClick = onBack,
                contentDescription = stringResource(Res.string.common_back),
                size = HeaderButtonSize,
                iconSize = 17.dp,
            )
            HeaderAvatar(conversation.peerInitial, support)
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = AppType.rowTitle.copy(fontWeight = AppType.button.fontWeight, color = Color.White),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    if (conversation.online) {
                        Box(Modifier.size(6.dp).clip(AppRadius.pill).background(palette.success))
                    }
                    Text(
                        if (conversation.online) {
                            stringResource(Res.string.auth_chat_online)
                        } else {
                            stringResource(Res.string.auth_chat_offline)
                        },
                        style = AppType.caption.copy(color = Color.White.copy(alpha = 0.85f)),
                    )
                }
            }
            // Suhbatni tozalash — handoff'da yo'q, lekin mavjud funksiya yo'qolmasin.
            // Ikonka `More`: qo'llanma bo'yicha `✕` FAQAT modal/dialog yopish uchun, sarlavhada
            // u yopish tugmasiga o'xshab chalg'itardi. Bosilganda tasdiqlash oynasi ochiladi.
            if (showClear) {
                HeaderIconButton(
                    AppIcons.More,
                    onClick = onClear,
                    contentDescription = stringResource(Res.string.common_clear),
                    size = HeaderButtonSize,
                    iconSize = 17.dp,
                )
            }
            HeaderIconButton(
                AppIcons.Phone,
                // Qo'ng'iroq hali telefoniyaga ulanmagan — tugma handoff shakli uchun turadi.
                onClick = {},
                contentDescription = stringResource(Res.string.auth_chat_call),
                size = HeaderButtonSize,
                iconSize = 17.dp,
            )
        }
    }
}

/** Header ichidagi avatar — oq shaffof kvadrat, oq chegara bilan. */
@Composable
private fun HeaderAvatar(initial: String, support: Boolean) {
    Box(
        Modifier.size(HeaderAvatarSize).clip(AppRadius.md)
            // Gradient USTIDAGI yuza — palitra emas, oq shaffoflik.
            .background(Color.White.copy(alpha = 0.22f))
            .border(1.5.dp, Color.White.copy(alpha = 0.40f), AppRadius.md),
        contentAlignment = Alignment.Center,
    ) {
        if (support) {
            // Qo'llab-quvvatlash — bosh harf emas, xizmat ikonkasi (handoff, 7-ekran).
            Icon(AppIcons.Support, null, tint = Color.White, modifier = Modifier.size(20.dp))
        } else {
            Text(initial, style = AppType.button.copy(fontSize = 18.sp, color = Color.White))
        }
    }
}
