package dev.feature.auth.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.core.domain.model.Conversation
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BackButton
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_chat_archive
import dev.core.uikit.resources.auth_chat_archive_action
import dev.core.uikit.resources.auth_chat_archive_count
import dev.core.uikit.resources.auth_chat_archive_empty
import dev.core.uikit.resources.auth_chat_delete_confirm
import dev.core.uikit.resources.auth_chat_delete_title
import dev.core.uikit.resources.auth_chat_empty
import dev.core.uikit.resources.auth_chat_title
import dev.core.uikit.resources.auth_chat_unarchive
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_cancel
import dev.core.uikit.resources.common_delete
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.rowShadow
import org.jetbrains.compose.resources.stringResource

/**
 * 1x — suhbatlar ro'yxati (faol / arxiv). Uzoq bosishda arxivlash yoki o'chirish
 * dialogi ochiladi.
 */
@Composable
internal fun ConversationList(
    conversations: List<Conversation>,
    archivedConversations: List<Conversation>,
    palette: AppPalette,
    onBack: () -> Unit,
    onOpen: (Conversation) -> Unit,
    onDelete: (Conversation) -> Unit,
    onArchive: (Conversation) -> Unit,
    onUnarchive: (Conversation) -> Unit,
) {
    var showArchived by remember { mutableStateOf(false) }
    var conversationForAction by remember { mutableStateOf<Conversation?>(null) }
    var conversationToDelete by remember { mutableStateOf<Conversation?>(null) }
    val list = if (showArchived) archivedConversations else conversations

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 54.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            BackButton(
                onClick = { if (showArchived) showArchived = false else onBack() },
                contentDescription = stringResource(Res.string.common_back),
                iconSize = 18.dp,
            )
            Text(
                if (showArchived) {
                    stringResource(Res.string.auth_chat_archive)
                } else {
                    stringResource(Res.string.auth_chat_title)
                },
                style = AppType.topBarTitle.copy(color = palette.ink),
                modifier = Modifier.weight(1f),
            )
            if (!showArchived && archivedConversations.isNotEmpty()) {
                ArchiveButton(archivedConversations.size, palette) { showArchived = true }
            }
        }
        Spacer(Modifier.height(14.dp))
        if (list.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    if (showArchived) {
                        stringResource(Res.string.auth_chat_archive_empty)
                    } else {
                        stringResource(Res.string.auth_chat_empty)
                    },
                    style = AppType.body.copy(color = palette.inkMuted),
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = AppSpacing.lg, end = AppSpacing.lg, bottom = AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                items(list, key = { it.id }) { c ->
                    ConversationRow(c, palette, onClick = { onOpen(c) }, onLongPress = { conversationForAction = c })
                }
            }
        }
    }

    // Uzoq bosishda amallar tanlash (arxivlash / o'chirish)
    val action = conversationForAction
    if (action != null) {
        AlertDialog(
            onDismissRequest = { conversationForAction = null },
            title = { Text(action.peerName, style = AppType.sectionTitle.copy(color = palette.ink)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    ActionRow(
                        if (action.archived) AppIcons.ArrowRight else AppIcons.Bookmark,
                        if (action.archived) {
                            stringResource(Res.string.auth_chat_unarchive)
                        } else {
                            stringResource(Res.string.auth_chat_archive_action)
                        },
                        palette,
                    ) {
                        if (action.archived) onUnarchive(action) else onArchive(action)
                        conversationForAction = null
                    }
                    ActionRow(AppIcons.Close, stringResource(Res.string.common_delete), palette, danger = true) {
                        conversationToDelete = action
                        conversationForAction = null
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { conversationForAction = null }) {
                    Text(stringResource(Res.string.common_cancel), style = AppType.label.copy(color = palette.inkMuted))
                }
            },
        )
    }

    val target = conversationToDelete
    if (target != null) {
        ConfirmDialog(
            title = stringResource(Res.string.auth_chat_delete_title),
            message = stringResource(Res.string.auth_chat_delete_confirm, target.peerName),
            confirmLabel = stringResource(Res.string.common_delete),
            palette = palette,
            onConfirm = { onDelete(target); conversationToDelete = null },
            onDismiss = { conversationToDelete = null },
        )
    }
}

/** "Arxiv (3)" tugmasi — arxivlangan suhbatlar bo'lsa sarlavha yonida chiqadi. */
@Composable
private fun ArchiveButton(count: Int, palette: AppPalette, onClick: () -> Unit) {
    val shape = AppRadius.sm
    Row(
        Modifier.rowShadow(shape).clip(shape).background(palette.card)
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            AppIcons.Bookmark,
            stringResource(Res.string.auth_chat_archive),
            tint = palette.primary,
            modifier = Modifier.size(13.dp),
        )
        Text(
            stringResource(Res.string.auth_chat_archive_count, "$count"),
            style = AppType.hint.copy(fontWeight = AppType.label.fontWeight, color = palette.primary),
        )
    }
}

/** Xavfli amalni tasdiqlash dialogi — o'chirish/tozalash uchun bitta shakl. */
@Composable
internal fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    palette: AppPalette,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = AppType.sectionTitle.copy(color = palette.ink)) },
        text = { Text(message, style = AppType.body.copy(color = palette.inkMuted)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, style = AppType.buttonSecondary.copy(color = palette.danger))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_cancel), style = AppType.label.copy(color = palette.inkMuted))
            }
        },
    )
}
