package dev.feature.auth.presentation.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.Conversation
import dev.core.domain.model.Message
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
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
        ChatThread(state.selected!!, state.messages, state.draft, palette, onBack = vm::close, onDraft = vm::onDraft, onSend = vm::send, onDeleteMessage = vm::deleteMessage, onClearMessages = vm::clearMessages)
    }
}

// 1x — suhbatlar ro'yxati
@Composable
private fun ConversationList(
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
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            IconBox(AppIcons.ArrowLeft, palette) { if (showArchived) showArchived = false else onBack() }
            Text(if (showArchived) "Arxiv" else "Xabarlar", style = TextStyle(fontFamily = AppFontFamily, fontSize = 22.sp, fontWeight = FontWeight.Black, color = palette.ink), modifier = Modifier.weight(1f))
            if (!showArchived && archivedConversations.isNotEmpty()) {
                Row(
                    Modifier.clip(RoundedCornerShape(10.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(10.dp)).clickable { showArchived = true }.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(AppIcons.Bookmark, "Arxiv", tint = palette.primary, modifier = Modifier.size(13.dp))
                    Text("Arxiv (${archivedConversations.size})", style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.Bold, color = palette.primary))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        if (list.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(if (showArchived) "Arxiv bo'sh" else "Suhbatlar yo'q", style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, color = palette.inkMuted))
            }
        } else {
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
            title = { Text(action.peerName, style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Black)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ActionRow(
                        if (action.archived) AppIcons.ArrowRight else AppIcons.Bookmark,
                        if (action.archived) "Arxivdan chiqarish" else "Arxivlash",
                        palette,
                    ) {
                        if (action.archived) onUnarchive(action) else onArchive(action)
                        conversationForAction = null
                    }
                    ActionRow(AppIcons.Close, "O'chirish", palette, danger = true) {
                        conversationToDelete = action
                        conversationForAction = null
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { conversationForAction = null }) {
                    Text("Bekor", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = palette.inkMuted))
                }
            },
        )
    }

    val target = conversationToDelete
    if (target != null) {
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            title = { Text("Suhbatni o'chirish", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Black)) },
            text = { Text("\"${target.peerName}\" bilan suhbat va barcha xabarlar o'chiriladi. Davom etasizmi?", style = TextStyle(fontFamily = AppFontFamily)) },
            confirmButton = {
                TextButton(onClick = { onDelete(target); conversationToDelete = null }) {
                    Text("O'chirish", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626)))
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToDelete = null }) {
                    Text("Bekor", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = palette.inkMuted))
                }
            },
        )
    }
}

@Composable
private fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, palette: AppPalette, danger: Boolean = false, onClick: () -> Unit) {
    val tint = if (danger) Color(0xFFDC2626) else palette.primary
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (danger) tint else palette.ink))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationRow(c: Conversation, palette: AppPalette, onClick: () -> Unit, onLongPress: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(16.dp)).combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(999.dp)).background(palette.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                Text(c.peerInitial, style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = palette.primary))
            }
            if (c.online) {
                Box(Modifier.align(Alignment.BottomEnd).size(12.dp).clip(RoundedCornerShape(999.dp)).background(Color.White).padding(2.dp)) {
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(999.dp)).background(palette.successDeep))
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(c.peerName, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(c.lastMessage, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = palette.inkFaint), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(c.lastTime, style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, color = palette.inkFaint))
            if (c.unreadCount > 0) {
                Box(Modifier.size(18.dp).clip(RoundedCornerShape(999.dp)).background(palette.primary), contentAlignment = Alignment.Center) {
                    Text("${c.unreadCount}", style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White))
                }
            }
        }
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

    // imePadding — klaviatura chiqqanда faqat pastki qism (input) tepaga suriladi,
    // header tepada qotib qoladi, xabarlar bo'limi qisqaradi (surilmaydi).
    Column(Modifier.fillMaxSize().imePadding()) {
        // Header — tepada QOTIB turadi (scroll bo'lmaydi)
        Column(Modifier.fillMaxWidth().background(palette.glass)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                IconBox(AppIcons.ArrowLeft, palette, onBack)
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(999.dp)).background(palette.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                        Text(conversation.peerInitial, style = TextStyle(fontFamily = AppFontFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = palette.primary))
                    }
                    if (conversation.online) {
                        Box(Modifier.align(Alignment.BottomEnd).size(12.dp).clip(RoundedCornerShape(999.dp)).background(palette.glass).padding(2.dp)) {
                            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(999.dp)).background(palette.successDeep))
                        }
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(conversation.peerName, style = TextStyle(fontFamily = AppFontFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(if (conversation.online) "online" else "oflayn", style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = if (conversation.online) palette.successDeep else palette.inkFaint))
                }
                if (messages.isNotEmpty()) {
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFFDC2626).copy(alpha = 0.10f)).clickable { showClearConfirm = true },
                        contentAlignment = Alignment.Center,
                    ) { Icon(AppIcons.Close, "Tozalash", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp)) }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(palette.border))
        }

        // Xabarlar — FAQAT shu qism scroll bo'ladi
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(messages, key = { it.id }) { m -> MessageBubble(m, palette, onLongPress = { messageToDelete = m }) }
        }

        // Kiritish paneli — klaviatura tepasida qoladi (imePadding)
        Column(Modifier.fillMaxWidth().background(palette.glass)) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(palette.border))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GlassTextField(draft, onDraft, "Xabar yozing...", modifier = Modifier.weight(1f), height = 48)
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(999.dp)).background(palette.primaryBrush).clickable(onClick = onSend),
                    contentAlignment = Alignment.Center,
                ) { Icon(AppIcons.Send, "Yuborish", tint = Color.White, modifier = Modifier.size(20.dp)) }
            }
        }
    }

    val target = messageToDelete
    if (target != null) {
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("Xabarni o'chirish", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Black)) },
            text = { Text("Bu xabarni o'chirmoqchimisiz?", style = TextStyle(fontFamily = AppFontFamily)) },
            confirmButton = {
                TextButton(onClick = { onDeleteMessage(target.id); messageToDelete = null }) {
                    Text("O'chirish", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626)))
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("Bekor", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = palette.inkMuted))
                }
            },
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Suhbatni tozalash", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Black)) },
            text = { Text("Bu suhbatdagi barcha xabarlar o'chiriladi. Davom etasizmi?", style = TextStyle(fontFamily = AppFontFamily)) },
            confirmButton = {
                TextButton(onClick = { onClearMessages(); showClearConfirm = false }) {
                    Text("Tozalash", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626)))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Bekor", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = palette.inkMuted))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(message: Message, palette: AppPalette, onLongPress: () -> Unit) {
    val align = if (message.outgoing) Alignment.CenterEnd else Alignment.CenterStart
    Box(Modifier.fillMaxWidth(), contentAlignment = align) {
        val shape = if (message.outgoing) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
        } else {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
        }
        Column(
            Modifier.widthIn(max = 280.dp).clip(shape)
                .background(if (message.outgoing) palette.primary else palette.glassStrong)
                .then(if (message.outgoing) Modifier else Modifier.border(1.dp, palette.border, shape))
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(horizontal = 13.dp, vertical = 9.dp),
        ) {
            Text(message.text, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, color = if (message.outgoing) Color.White else palette.ink))
            Text(
                message.time,
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 9.sp, color = if (message.outgoing) Color.White.copy(alpha = 0.7f) else palette.inkFaint),
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@Composable
private fun IconBox(icon: androidx.compose.ui.graphics.vector.ImageVector, palette: AppPalette, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, "Orqaga", tint = palette.ink, modifier = Modifier.size(18.dp)) }
}
