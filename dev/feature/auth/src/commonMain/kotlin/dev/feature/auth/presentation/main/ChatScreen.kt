package dev.feature.auth.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.Conversation
import dev.core.domain.model.Message
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.IconTile
import dev.core.uikit.component.MonogramTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_chat_clear_confirm
import dev.core.uikit.resources.auth_chat_clear_title
import dev.core.uikit.resources.auth_chat_input_placeholder
import dev.core.uikit.resources.auth_chat_message_delete_confirm
import dev.core.uikit.resources.auth_chat_message_delete_title
import dev.core.uikit.resources.auth_chat_offline
import dev.core.uikit.resources.auth_chat_online
import dev.core.uikit.resources.auth_chat_send
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_clear
import dev.core.uikit.resources.common_delete
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.main.components.ConfirmDialog
import dev.feature.auth.presentation.main.components.ConversationList
import dev.feature.auth.presentation.main.components.MessageBubble
import dev.feature.auth.presentation.main.components.OnlineDot
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatScreen(onBack: () -> Unit, vm: ChatViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()

    if (state.selected == null) {
        ConversationList(
            conversations = state.conversations,
            archivedConversations = state.archivedConversations,
            palette = palette,
            onBack = onBack,
            onOpen = vm::open,
            onDelete = { vm.deleteConversation(it.id) },
            onArchive = { vm.setArchived(it.id, true) },
            onUnarchive = { vm.setArchived(it.id, false) },
        )
    } else {
        ChatThread(
            conversation = state.selected!!,
            messages = state.messages,
            draft = state.draft,
            palette = palette,
            onBack = vm::close,
            onDraft = vm::onDraft,
            onSend = vm::send,
            onDeleteMessage = vm::deleteMessage,
            onClearMessages = vm::clearMessages,
        )
    }
}

// 1y — suhbat oynasi
@Composable
private fun ChatThread(
    conversation: Conversation,
    messages: List<Message>,
    draft: String,
    palette: AppPalette,
    onBack: () -> Unit,
    onDraft: (String) -> Unit,
    onSend: () -> Unit,
    onDeleteMessage: (String) -> Unit,
    onClearMessages: () -> Unit,
) {
    var messageToDelete by remember { mutableStateOf<Message?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    // Klaviatura balandligi — o'zgarganda pastga suramiz (Telegram kabi).
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(messages.size, imeBottom) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    // imePadding — klaviatura chiqqanda faqat pastki qism (input) tepaga suriladi,
    // header tepada qotib qoladi, xabarlar bo'limi qisqaradi (surilmaydi).
    Column(Modifier.fillMaxSize().imePadding()) {
        // Header — tepada QOTIB turadi (scroll bo'lmaydi)
        Column(Modifier.fillMaxWidth().background(palette.card)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 54.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                BackButton(onBack, contentDescription = stringResource(Res.string.common_back), iconSize = 18.dp)
                Box(contentAlignment = Alignment.Center) {
                    MonogramTile(conversation.peerInitial, size = 42.dp, fontSize = 16.sp)
                    if (conversation.online) {
                        OnlineDot(palette, ringColor = palette.card, modifier = Modifier.align(Alignment.BottomEnd))
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        conversation.peerName,
                        style = AppType.cardTitle.copy(fontWeight = AppType.button.fontWeight, color = palette.ink),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        if (conversation.online) {
                            stringResource(Res.string.auth_chat_online)
                        } else {
                            stringResource(Res.string.auth_chat_offline)
                        },
                        style = AppType.hint.copy(
                            color = if (conversation.online) palette.success else palette.inkFaint,
                        ),
                    )
                }
                if (messages.isNotEmpty()) {
                    IconTile(
                        AppIcons.Close,
                        contentDescription = stringResource(Res.string.common_clear),
                        tint = palette.danger,
                        background = palette.dangerBg,
                        size = 38.dp,
                        iconSize = 16.dp,
                        shape = AppRadius.sm,
                        onClick = { showClearConfirm = true },
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(palette.divider))
        }

        // Xabarlar — FAQAT shu qism scroll bo'ladi
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(messages, key = { it.id }) { m ->
                MessageBubble(m, palette, onLongPress = { messageToDelete = m })
            }
        }

        // Kiritish paneli — klaviatura tepasida qoladi (imePadding)
        Column(Modifier.fillMaxWidth().background(palette.card)) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(palette.divider))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                GlassTextField(
                    draft,
                    onDraft,
                    stringResource(Res.string.auth_chat_input_placeholder),
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                )
                Box(
                    Modifier.size(48.dp).clip(AppRadius.pill).background(palette.primaryBrush)
                        .clickable(onClick = onSend),
                    contentAlignment = Alignment.Center,
                ) {
                    // Gradient USTIDAGI ikonka — har ikkala rejimda oq.
                    Icon(
                        AppIcons.Send,
                        stringResource(Res.string.auth_chat_send),
                        tint = palette.onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
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
