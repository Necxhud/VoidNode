package com.voidnode.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalContext
import com.voidnode.app.data.ChatStorage
import com.voidnode.app.ui.screens.ChatDetailScreen
import com.voidnode.app.ui.screens.ChatsScreen
import com.voidnode.app.ui.screens.Message
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VoidNodeApp() {
    var selectedChatTitle by remember { mutableStateOf<String?>(null) }
    var isTyping by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val title = selectedChatTitle

    if (title == null) {
        ChatsScreen(
            onOpenChat = { selectedChatTitle = it }
        )
    } else {
        val messages = remember(title) {
            ChatStorage.loadMessages(context, title).toMutableStateList()
        }

        ChatDetailScreen(
            chatTitle = title,
            messages = messages,
            isTyping = isTyping,
            onSendMessage = { text ->
                if (text.isNotBlank()) {
                    val userMessage = Message(
                        text = text.trim(),
                        time = currentTime(),
                        isMe = true,
                        status = "sending"
                    )

                    messages.add(userMessage)
                    ChatStorage.saveMessages(context, title, messages)

                    scope.launch {
                        delay(600)

                        val sentIndex = messages.indexOfFirst { it.text == userMessage.text && it.time == userMessage.time }
                        if (sentIndex >= 0) {
                            messages[sentIndex] = messages[sentIndex].copy(status = "sent")
                            ChatStorage.saveMessages(context, title, messages)
                        }

                        delay(600)

                        val readIndex = messages.indexOfFirst { it.text == userMessage.text && it.time == userMessage.time }
                        if (readIndex >= 0) {
                            messages[readIndex] = messages[readIndex].copy(status = "read")
                            ChatStorage.saveMessages(context, title, messages)
                        }

                        delay(700)
                        isTyping = true

                        delay(1000)
                        isTyping = false

                        val reply = Message(
                            text = "Принял: " + userMessage.text,
                            time = currentTime(),
                            isMe = false
                        )

                        messages.add(reply)
                        ChatStorage.saveMessages(context, title, messages)
                    }
                }
            },
            onDeleteMessage = { index ->
                if (index in messages.indices) {
                    messages.removeAt(index)
                    ChatStorage.saveMessages(context, title, messages)
                }
            },
            onBack = { selectedChatTitle = null }
        )
    }
}

private fun currentTime(): String {
    return LocalTime.now().format(
        DateTimeFormatter.ofPattern("HH:mm")
    )
}
