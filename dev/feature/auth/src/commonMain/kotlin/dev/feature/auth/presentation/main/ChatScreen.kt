package dev.feature.auth.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.main.components.ChatThread
import dev.feature.auth.presentation.main.components.ConversationList
import org.koin.compose.viewmodel.koinViewModel

/**
 * Xabarlar oqimi — suhbatlar ro'yxati (handoff, 5-ekran) va tanlangan suhbat (6-ekran).
 *
 * Qo'llab-quvvatlash suhbati bu ro'yxatda KO'RINMAYDI, u [SupportChatScreen] orqali ochiladi.
 */
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

/**
 * Qo'llab-quvvatlash chati (handoff, 7-ekran) — ro'yxatsiz, to'g'ridan-to'g'ri suhbat.
 *
 * Ekran ochilishi bilan `SUPPORT` turidagi suhbat tanlanadi; orqaga tugmasi ro'yxatga emas,
 * chaqiruvchi ekranga (Bizneslarim) qaytaradi.
 */
@Composable
fun SupportChatScreen(onBack: () -> Unit, vm: ChatViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    val support = state.support

    LaunchedEffect(support?.id) { support?.let(vm::open) }

    val selected = state.selected
    if (selected == null || selected.id != support?.id) {
        // Suhbat bazadan o'qilguncha bo'sh yuza — miltillovchi ro'yxat ko'rsatmaymiz.
        Box(Modifier.fillMaxSize())
        return
    }
    ChatThread(
        conversation = selected,
        messages = state.messages,
        draft = state.draft,
        palette = palette,
        onBack = { vm.close(); onBack() },
        onDraft = vm::onDraft,
        onSend = vm::send,
        onDeleteMessage = vm::deleteMessage,
        onClearMessages = vm::clearMessages,
        // Foydalanuvchi yozgandan keyin operator javob yozayotgani ko'rsatiladi.
        showTyping = state.messages.lastOrNull()?.outgoing == true,
    )
}
