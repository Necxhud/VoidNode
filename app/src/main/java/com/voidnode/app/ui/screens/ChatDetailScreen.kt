package com.voidnode.app.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidnode.app.ui.theme.VoidBackground
import com.voidnode.app.ui.theme.VoidPrimary
import com.voidnode.app.ui.theme.VoidText

data class Message(
    val text: String,
    val time: String,
    val isMe: Boolean = true,
    val status: String = "read"
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
    chatTitle: String,
    messages: List<Message>,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit,
    onDeleteMessage: (Int) -> Unit,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var selectedMessageIndex by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .animateContentSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(VoidPrimary, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = VoidText
                        )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(VoidPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chatTitle.take(1).uppercase(),
                        color = VoidText,
                        fontSize = 18.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF31C48D), CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(chatTitle, color = VoidText, fontSize = 20.sp)

                Text(
                    text = if (isTyping) "печатает…" else "в сети",
                    color = if (isTyping) VoidPrimary else Color(0xFF31C48D),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Сообщений пока нет. Напиши первое сообщение.",
                    color = VoidText,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    count = messages.size,
                    key = { index ->
                        messages[index].time + messages[index].text + messages[index].isMe
                    }
                ) { index ->
                    val message = messages[index]
                    val bubbleColor = if (message.isMe) VoidPrimary else Color(0xFF2A2A2A)
                    val bubbleShape = if (message.isMe) {
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 4.dp
                        )
                    } else {
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 16.dp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 270.dp)
                                .background(bubbleColor, bubbleShape)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { selectedMessageIndex = index }
                                )
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = message.text,
                                color = VoidText,
                                fontSize = 15.sp,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = message.time,
                                    color = VoidText,
                                    fontSize = 10.sp
                                )

                                if (message.isMe) {
                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = when (message.status) {
                                            "sending" -> "🕐"
                                            "sent" -> "✓"
                                            else -> "✓✓"
                                        },
                                        color = if (message.status == "read") {
                                            Color(0xFF4FC3F7)
                                        } else {
                                            VoidText
                                        },
                                        fontSize = 10.sp
                                )
                            }
                         }
                        }
                    }
                }

                if (isTyping) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "печатает…",
                                color = VoidText,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFF2A2A2A),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                placeholder = { Text("Сообщение") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VoidText,
                    unfocusedTextColor = VoidText,
                    focusedBorderColor = VoidPrimary,
                    unfocusedBorderColor = VoidPrimary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(VoidPrimary, CircleShape)
                    .clickable {
                        onSendMessage(messageText)
                        messageText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Отправить",
                    tint = VoidText
                )
            }
        }
    }

    selectedMessageIndex?.let { index ->
        AlertDialog(
            onDismissRequest = { selectedMessageIndex = null },
            title = { Text("Удалить сообщение?") },
            text = {
                Text(messages[index].text)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMessage(index)
                        selectedMessageIndex = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMessageIndex = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}
