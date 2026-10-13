package com.voidnode.app.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import org.json.JSONObject
import org.json.JSONArray
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidnode.app.ui.theme.VoidBackground
import com.voidnode.app.ui.theme.VoidPrimary
import com.voidnode.app.ui.theme.VoidText

private data class Chat(
    val id: Int,
    val title: String,
    val preview: String,
    val archived: Boolean = false,
    val hidden: Boolean = false,
    val category: String = "",
    val unread: Int = 0
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatsScreen(
    onOpenChat: (String) -> Unit
) {
    val chats = remember {
        mutableStateListOf(
            Chat(
                id = 1,
                title = "Тестовый контакт",
                preview = "Добро пожаловать в VoidNode"
            ),
            Chat(
                id = 2,
                title = "Архивный пример",
                preview = "Этот чат можно вернуть из архива",
                archived = true
            )
        )
    }


    var selectedTab by remember { mutableStateOf(0) }
    var categories by remember { mutableStateOf(listOf("Работа", "Семья")) }
    val tabTitles = listOf("Чаты", "Контакты") + categories
    var showArchive by remember { mutableStateOf(false) }
    val currentCategory = if (selectedTab >= 2 && selectedTab < tabTitles.lastIndex) tabTitles[selectedTab] else null
    val contacts = remember {
        mutableStateListOf(
            "Анна Петрова",
            "Иван Смирнов",
            "Мария Козлова",
            "Дмитрий Волков",
            "Ольга Новак"
        )
    }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("voidnode_data", Context.MODE_PRIVATE) }

    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val saved = prefs.getString("chats", null)
        if (saved != null) {
            try {
                val arr = JSONArray(saved)
                chats.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    chats.add(
                        Chat(
                            id = o.getInt("id"),
                            title = o.getString("title"),
                            preview = o.getString("preview"),
                            archived = o.getBoolean("archived"),
                            hidden = o.getBoolean("hidden"),
                            category = o.optString("category", ""),
                            unread = o.optInt("unread", 0)
                        )
                    )
                }
            } catch (e: Exception) {
            }
        }
        val savedCats = prefs.getString("categories", null)
        if (savedCats != null) {
            try {
                val catArr = JSONArray(savedCats)
                val catList = mutableListOf<String>()
                for (i in 0 until catArr.length()) catList.add(catArr.getString(i))
                categories = catList
            } catch (e: Exception) {
            }
        }
        val savedContacts = prefs.getString("contacts", null)
        if (savedContacts != null) {
            try {
                val conArr = JSONArray(savedContacts)
                contacts.clear()
                for (i in 0 until conArr.length()) contacts.add(conArr.getString(i))
            } catch (e: Exception) {
            }
        }
        loaded = true
    }

    LaunchedEffect(loaded, chats.toList(), categories, contacts.toList()) {
        if (loaded) {
            val arr = JSONArray()
            chats.forEach { c ->
                arr.put(
                    JSONObject()
                        .put("id", c.id)
                        .put("title", c.title)
                        .put("preview", c.preview)
                        .put("archived", c.archived)
                        .put("hidden", c.hidden)
                        .put("category", c.category)
                        .put("unread", c.unread)
                )
            }
            prefs.edit().putString("chats", arr.toString()).apply()
            val catArr = JSONArray()
            categories.forEach { catArr.put(it) }
            prefs.edit().putString("categories", catArr.toString()).apply()
            val conArr = JSONArray()
            contacts.forEach { conArr.put(it) }
            prefs.edit().putString("contacts", conArr.toString()).apply()
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var selectedChat by remember { mutableStateOf<Chat?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var showAddContact by remember { mutableStateOf(false) }
    var contactToDelete by remember { mutableStateOf<String?>(null) }
    var newContactName by remember { mutableStateOf("") }

    val visibleChats = chats
        .filter { !it.hidden }
        .filter {
            when {
                showArchive -> it.archived
                currentCategory != null -> !it.archived && it.category == currentCategory
                else -> !it.archived
            }
        }
        .filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.preview.contains(searchQuery, ignoreCase = true)
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabTitles.forEachIndexed { index, title ->
                Column(
                    modifier = Modifier.combinedClickable(
                        onClick = { selectedTab = index },
                        onLongClick = {
                            if (index >= 2) {
                                categories = categories - title
                                selectedTab = 0
                            }
                        }
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (title == "Архив") {
                            ArchiveIcon(
                                color = if (selectedTab == index) VoidText else VoidText.copy(alpha = 0.5f)
                            )
                        } else {
                            Text(
                            text = title,
                            color = if (selectedTab == index) VoidText else VoidText.copy(alpha = 0.5f),
                            fontSize = 15.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                        }
                        val unreadCount = when {
                            index == 0 -> chats.count { !it.archived && !it.hidden && it.unread > 0 }
                            
                            index >= 2 -> chats.count { !it.archived && !it.hidden && it.category == title && it.unread > 0 }
                            else -> 0
                        }
                        if (unreadCount > 0) {
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(VoidPrimary, CircleShape)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(3.dp)
                            .background(
                                if (selectedTab == index) VoidPrimary else Color.Transparent,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clickable { showAddCategory = true }
                    .padding(8.dp)
            ) {
                Text("+", color = VoidText, fontSize = 20.sp)
            }
        }
        Spacer(Modifier.height(12.dp))

        

        if (showSearch) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    placeholder = { Text("Поиск", fontSize = 14.sp) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "✕",
                                color = VoidText,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .clickable { searchQuery = "" }
                                    .padding(6.dp)
                            )
                        }
                    },
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
                        .size(44.dp)
                        .background(VoidPrimary, CircleShape)
                        .clickable {
                            showSearch = false
                            searchQuery = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = VoidText, fontSize = 18.sp)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "VoidNode",
                        color = VoidText,
                        fontSize = 30.sp
                    )

                    Text(
                        text = if (showArchive) "Архив чатов" else "Чаты",
                        color = VoidText,
                        fontSize = 18.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showArchive) {
                        TextButton(
                            onClick = {
                                showArchive = false
                                selectedTab = 0
                            }
                        ) {
                            Text("Выйти из архива", color = VoidPrimary)
                        }
                    }
                    IconButton(onClick = { showSearch = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = VoidText
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Text(
                                text = "⋮",
                                color = VoidText,
                                fontSize = 28.sp
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Настройки")
                                },
                                onClick = {
                                    showMenu = false
                                    showSettings = true
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = {
                    Text("Настройки")
                },
                text = {
                    Column {
                        Text("VoidNode", color = VoidText, fontSize = 18.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Версия 0.0.3", color = VoidText, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Архив доступен на главном экране во вкладке «Архив».", color = VoidText, fontSize = 14.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettings = false }) {
                        Text("Закрыть")
                    }
                }
            )
        }

        if (showSearch && searchQuery.isBlank()) {
            chats.filter { !it.archived && !it.hidden }.forEach { chat ->
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).combinedClickable(onClick = { onOpenChat(chat.title) }, onLongClick = { val k = chats.indexOfFirst { it.id == chat.id }; if (k >= 0) chats[k] = chats[k].copy(hidden = true) }).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChatAvatar(chat.title)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(chat.title, color = VoidText, fontSize = 16.sp)
                        Text(chat.preview, color = VoidText, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 1) {
            Button(
                onClick = { showAddContact = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VoidPrimary)
            ) {
                Text("＋ Добавить контакт")
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = contacts,
                    key = { it }
                ) { name ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { contactToDelete = name }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChatAvatar(name)
                        Spacer(Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = name,
                                color = VoidText,
                                fontSize = 16.sp
                            )
                        }
                        TextButton(
                            onClick = {
                                if (chats.none { it.title == name }) {
                                    chats.add(
                                        Chat(
                                            id = (chats.maxOfOrNull { it.id } ?: 0) + 1,
                                            title = name,
                                            preview = "Новый чат",
                                            unread = 1
                                        )
                                    )
                                }
                                selectedTab = 0
                            }
                        ) {
                            Text("Написать", color = VoidPrimary)
                        }
                    }
                }
            }
        contactToDelete?.let { name ->
            AlertDialog(
                onDismissRequest = { contactToDelete = null },
                title = {
                    Text("Удалить контакт?")
                },
                text = {
                    Text("Контакт \"$name\" будет удалён из списка.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            contacts.remove(name)
                            contactToDelete = null
                        }
                    ) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { contactToDelete = null }) {
                        Text("Отмена")
                    }
                }
            )
        }

        if (showArchive && selectedTab == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { showArchive = false }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArchiveIcon(color = VoidText)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Архив",
                    color = VoidText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = {
                        showArchive = false
                        selectedTab = 0
                    }
                ) {
                    Text("Выйти", color = VoidPrimary)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        } else if (visibleChats.isEmpty()) {
            Text(
                text = if (searchQuery.isNotBlank()) {
                    "Ничего не найдено"
                } else if (showArchive) {
                    "Архив пуст"
                } else {
                    "Нет активных чатов"
                },
                color = VoidText,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            val chatListState = rememberLazyListState()
            var pullAccum by remember { mutableStateOf(0f) }
            LazyColumn(
                state = chatListState,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var pulled = 0f
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                val dy = change.position.y - down.position.y
                                if (dy > pulled) pulled = dy
                                if (pulled > 100f &&
                                    chatListState.firstVisibleItemIndex == 0 &&
                                    chatListState.firstVisibleItemScrollOffset == 0
                                ) {
                                    showArchive = true
                                    selectedTab = 0
                                    break
                                }
                                if (event.changes.none { it.pressed }) break
                            }
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = visibleChats,
                    key = { it.id }
                ) { chat ->
                    ChatRow(
                        chat = chat,
                        onOpenChat = {
                            val idx = chats.indexOfFirst { it.id == chat.id }
                            if (idx >= 0) {
                                chats[idx] = chats[idx].copy(unread = 0)
                                val arr = JSONArray()
                                chats.forEach { c ->
                                    arr.put(
                                        JSONObject()
                                            .put("id", c.id)
                                            .put("title", c.title)
                                            .put("preview", c.preview)
                                            .put("archived", c.archived)
                                            .put("hidden", c.hidden)
                                            .put("category", c.category)
                                            .put("unread", c.unread)
                                    )
                                }
                                prefs.edit().putString("chats", arr.toString()).apply()
                            }
                            onOpenChat(chat.title)
                        },
                        onLongPress = { selectedChat = chat }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (chats.none { it.id == 3 }) {
                    chats.add(
                        Chat(
                            id = 3,
                            title = "Новый тестовый чат",
                            preview = "Чат добавлен локально",
                            unread = 1
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = VoidPrimary
            )
        ) {
            Text("Открыть тестовый чат")
        }
    
        
}

    if (showAddContact) {
        AlertDialog(
            onDismissRequest = { showAddContact = false },
            title = {
                Text("Новый контакт")
            },
            text = {
                OutlinedTextField(
                    value = newContactName,
                    onValueChange = { newContactName = it },
                    singleLine = true,
                    placeholder = { Text("Имя контакта") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newContactName.trim()
                        if (name.isNotEmpty() && name !in contacts) {
                            contacts.add(name)
                        }
                        newContactName = ""
                        showAddContact = false
                    }
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContact = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = {
                Text("Новая категория")
            },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    singleLine = true,
                    placeholder = { Text("Например: Работа") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newCategoryName.trim()
                        if (name.isNotEmpty() && name !in categories) {
                            categories = categories + name
                        }
                        newCategoryName = ""
                        showAddCategory = false
                    }
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategory = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    selectedChat?.let { chat ->
        AlertDialog(
            onDismissRequest = { selectedChat = null },
            title = {
                Text(
                    if (chat.archived) "Вернуть чат?"
                    else "Архивировать чат?"
                )
            },
            text = {
                Text(chat.title)
            },
            confirmButton = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(
                            onClick = {
                                val index = chats.indexOfFirst { it.id == chat.id }
                                if (index >= 0) {
                                    chats[index] = chats[index].copy(category = "")
                                }
                                selectedChat = null
                            }
                        ) {
                            Text("Без категории", color = VoidText)
                        }
                        categories.forEach { cat ->
                            TextButton(
                                onClick = {
                                    val index = chats.indexOfFirst { it.id == chat.id }
                                    if (index >= 0) {
                                        chats[index] = chats[index].copy(category = cat)
                                    }
                                    selectedChat = null
                                }
                            ) {
                                Text(
                                    if (chat.category == cat) "• " + cat else cat,
                                    color = VoidPrimary
                                )
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            val index = chats.indexOfFirst { it.id == chat.id }
                            if (index >= 0) {
                                chats[index] = chats[index].copy(
                                    archived = !chats[index].archived
                                )
                            }
                            selectedChat = null
                        }
                    ) {
                        Text(
                            if (chat.archived) "Вернуть"
                            else "Архивировать"
                        )
                    }


                    TextButton(
                        onClick = {
                            chats.removeAll { it.id == chat.id }
                            selectedChat = null
                        }
                    ) {
                        Text("Удалить", color = Color(0xFFD24B4B))
                    }

                }
            },
            dismissButton = {
                TextButton(onClick = { selectedChat = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatRow(
    chat: Chat,
    onOpenChat: () -> Unit,
    onLongPress: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = onOpenChat,
                onLongClick = onLongPress
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChatAvatar(chat.title)

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = chat.title,
                color = VoidText,
                fontSize = 18.sp
            )

            Text(
                text = chat.preview,
                color = VoidText,
                fontSize = 14.sp
            )
        }
    }
}


@Composable
private fun ChatAvatar(name: String) {
    val colors = listOf(
        Color(0xFF2F6FED),
        Color(0xFF23A55A),
        Color(0xFFE5A50A),
        Color(0xFFD24B4B),
        Color(0xFF8E44AD)
    )
    val color = colors[kotlin.math.abs(name.hashCode()) % colors.size]
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ArchiveIcon(color: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.09f
        // Крышка
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.08f, h * 0.10f),
            size = Size(w * 0.84f, h * 0.22f),
            cornerRadius = CornerRadius(w * 0.06f)
        )
        // Корпус
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.16f, h * 0.40f),
            size = Size(w * 0.68f, h * 0.50f),
            cornerRadius = CornerRadius(w * 0.05f)
        )
        // Ручка
        drawRoundRect(
            color = VoidBackground,
            topLeft = Offset(w * 0.38f, h * 0.55f),
            size = Size(w * 0.24f, h * 0.09f),
            cornerRadius = CornerRadius(w * 0.04f)
        )
    }
}
