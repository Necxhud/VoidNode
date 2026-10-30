package com.voidnode.app.ui.screens
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.Canvas
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
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
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.voidnode.app.ui.AppState

private data class Chat(
    val id: Int,
    val title: String,
    val preview: String,
    val archived: Boolean = false,
    val hidden: Boolean = false,
    val category: String = "",
    val unread: Int = 0,
    val pinned: Boolean = false,
    val muted: Boolean = false,
    val lastMessageTime: Long = 0L,
    val messageStatus: Int = 0
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatsScreen(
    onOpenChat: (String) -> Unit
) {
    val chats = remember {
        mutableStateListOf(
            Chat(
                id = 2,
                title = "Архивный пример",
                preview = "Этот чат можно вернуть из архива",
                archived = true
            )
        )
    }


    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabTitles = listOf("Чаты", "Контакты") + AppState.categories
    val archivedContacts = remember { mutableStateListOf<String>() }
var showArchive by rememberSaveable { mutableStateOf(false) }
    var lastArchiveToggle by remember { mutableStateOf(0L) }
    val currentCategory = if (selectedTab >= 2 && selectedTab < tabTitles.lastIndex) tabTitles[selectedTab] else null
    val contacts = remember {
        mutableStateListOf(
            "Анна Петрова",
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
                AppState.categories = catList
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

    run {
            val savedArchived = prefs.getString("archivedContacts", null)
            if (savedArchived != null) {
                val arr = JSONArray(savedArchived)
                archivedContacts.clear()
                for (i in 0 until arr.length()) archivedContacts.add(arr.getString(i))
            }
        }
        LaunchedEffect(loaded, chats.toList(), AppState.categories, contacts.toList()) {
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
            AppState.categories.forEach { catArr.put(it) }
            prefs.edit().putString("categories", catArr.toString()).apply()
            val conArr = JSONArray()
            contacts.forEach { conArr.put(it) }
            prefs.edit().putString("contacts", conArr.toString()).apply()
            val archArr = JSONArray()
            archivedContacts.forEach { archArr.put(it) }
            prefs.edit().putString("archivedContacts", archArr.toString()).apply()
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var selectedChat by remember { mutableStateOf<Chat?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    val pinnedContacts = remember { mutableStateOf(prefs.getStringSet("pinned_contacts", emptySet()) ?: emptySet()) }
val mutedContacts = remember { mutableStateOf(prefs.getStringSet("muted_contacts", emptySet()) ?: emptySet()) }
var showAddContact by remember { mutableStateOf(false) }
    var newContactId by remember { mutableStateOf("") }
    var contactToDelete by remember { mutableStateOf<String?>(null) }
    var newContactName by remember { mutableStateOf("") }
    val quarantineContext = LocalContext.current
    var blockedContacts by remember { mutableStateOf(QuarantineStore.load(quarantineContext)) }
    var showQuarantine by remember { mutableStateOf(false) }


    val visibleChats = chats
        .filter { !it.hidden }
        .filter { it.title !in blockedContacts }
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

    BackHandler(enabled = showSearch) {
        if (searchFocused) {
            keyboardController?.hide()
            focusManager.clearFocus()
        } else {
            showSearch = false
            searchQuery = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
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
                        .height(54.dp)
                        .onFocusChanged { searchFocused = it.isFocused },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    placeholder = { Text("Поиск", fontSize = 14.sp) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "✕",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .clickable { searchQuery = "" }
                                    .padding(6.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            showSearch = false
                            searchQuery = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
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
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 30.sp
                    )

                    Text(
                        text = if (showArchive) "Архив чатов" else "",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showSearch = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                            Text(
                                text = "⋮",
                                color = MaterialTheme.colorScheme.onBackground,
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


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabTitles.forEachIndexed { index, title ->
                Column(
                    modifier = Modifier.combinedClickable(
                        onClick = { if (!showArchive || index == 0) selectedTab = index },
                        onLongClick = {
                            if (index >= 2) {
                                AppState.categories = AppState.categories - title
                                selectedTab = 0
                            }
                        }
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (title == "Архив") {
                            ArchiveIcon(
                                color = if (selectedTab == index) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        } else {
                            Text(
                            text = title,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
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
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
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
                                if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        

if (showSettings) {
            SettingsScreen(onBack = { showSettings = false })
        }

        if (showSearch && searchQuery.isBlank()) {
            chats.filter { !it.archived && !it.hidden }.forEach { chat ->
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Transparent).combinedClickable(onClick = { onOpenChat(chat.title) }, onLongClick = { val k = chats.indexOfFirst { it.id == chat.id }; if (k >= 0) chats[k] = chats[k].copy(hidden = true) }).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChatAvatar(chat.title)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(chat.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                        Text(chat.preview, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("＋ Добавить контакт")
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = contacts.filter { it !in archivedContacts }.sortedWith(compareByDescending { it in pinnedContacts.value }),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                            if (name in mutedContacts.value) {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Filled.NotificationsOff, contentDescription = "Без звука", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                            }
                        }
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
                            Text("Написать", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        contactToDelete?.let { name ->
            Dialog(onDismissRequest = { contactToDelete = null }) {
                Card(
                    modifier = Modifier.width(250.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(name, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            TextButton(
                                onClick = {
                                    val cur = pinnedContacts.value.toMutableSet()
                                    if (name in cur) cur.remove(name) else cur.add(name)
                                    pinnedContacts.value = cur
                                    prefs.edit().putStringSet("pinned_contacts", cur).apply()
                                    contactToDelete = null
                                },
                                modifier = Modifier.size(48.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (name in pinnedContacts.value) "📌" else "📍", fontSize = 20.sp)
                            }
                            TextButton(
                                onClick = {
                                    val cur = mutedContacts.value.toMutableSet()
                                    if (name in cur) cur.remove(name) else cur.add(name)
                                    mutedContacts.value = cur
                                    prefs.edit().putStringSet("muted_contacts", cur).apply()
                                    contactToDelete = null
                                },
                                modifier = Modifier.size(48.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (name in mutedContacts.value) "🔕" else "🔔", fontSize = 20.sp)
                            }
                            TextButton(
                                onClick = {
                                    contacts.remove(name)
                                    archivedContacts.remove(name)
                                    contactToDelete = null
                                },
                                modifier = Modifier.size(48.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("🗑", fontSize = 20.sp, color = Color(0xFFD24B4B))
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showArchive && selectedTab == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showArchive = false }) {
                    Text("←", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)
                }

                Text(
                    "Архив · ${chats.count { it.archived }}",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Поиск в архиве") }
            )
            Spacer(Modifier.height(8.dp))
        }

        } else if (visibleChats.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(showArchive) {
                        var total = 0f
                        detectVerticalDragGestures(
                            onDragStart = { total = 0f },
                            onVerticalDrag = { _, dy -> total += dy },
                            onDragEnd = {
                                if (showArchive && total > 200f) showArchive = false
                            }
                        )
                    }
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) {
                        "Ничего не найдено"
                    } else if (showArchive) {
                        "Архив пуст"
                    } else {
                        "Нет активных чатов"
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                )
                if (searchQuery.isBlank()) {
                TextButton(onClick = { showArchive = !showArchive }) {
                    Text(if (showArchive) "Вернуться к чатам" else "Открыть архив")
                }
                }
            }
        } else {
            val chatListState = rememberLazyListState()
            var pullAccum by remember { mutableStateOf(0f) }
            var pullReady by remember { mutableStateOf(false) }
                                    if (pullReady) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(Color(0xFF2F6FED), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        if (showArchive) "Отпустите, чтобы вернуться" else "Отпустите для архива",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            LazyColumn(
                state = chatListState,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var pulled = 0f
                            var movedSideways = false
                            pullReady = false
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                val dy = change.position.y - down.position.y
                                val dx = change.position.x - down.position.x
                                if (event.changes.any { it.isConsumed }) {
                                    if (event.changes.none { it.pressed }) {
                                        pullReady = false
                                        break
                                    }
                                    continue
                                }
                                if (kotlin.math.abs(dx) > kotlin.math.abs(dy) && kotlin.math.abs(dx) > 40f) movedSideways = true
                                if (dy > pulled) pulled = dy
                                val atTop = chatListState.firstVisibleItemIndex == 0 &&
                                    chatListState.firstVisibleItemScrollOffset == 0
                                pullReady = atTop && pulled > 60f && !movedSideways
                                if (event.changes.none { it.pressed }) {
                                    if (!movedSideways && pullReady && kotlin.math.abs(dx) < 60f) {
                                        val now = System.currentTimeMillis()
                                        if (now - lastArchiveToggle >= 600L) {
                                            lastArchiveToggle = now
                                            showArchive = !showArchive
                                            if (showArchive) selectedTab = 0
                                        }
                                    } else if (movedSideways && kotlin.math.abs(dx) > 100f) {
                                        if (dx < 0 && selectedTab < tabTitles.lastIndex) selectedTab += 1
                                        if (dx > 0 && selectedTab > 0) selectedTab -= 1
                                    }
                                    pullReady = false
                                    break
                                }
                            }
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = visibleChats.sortedWith(compareByDescending<Chat> { it.pinned }),
                    key = { it.id }
                ) { chat ->
                    ChatRow(
                        modifier = Modifier.animateItem(),
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
                        onLongPress = { selectedChat = chat },
                        isArchived = showArchive,
                        onSwipeArchive = {
                            val index = chats.indexOfFirst { it.id == chat.id }
                            if (index >= 0) {
                                chats[index] = chats[index].copy(archived = !showArchive)
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
                        }
                    )
                }
            }
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
                OutlinedTextField(
                    value = newContactId,
                    onValueChange = { newContactId = it },
                    singleLine = true,
                    placeholder = { Text("P2P-ID контакта") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newContactName.trim()
                        if (name.isNotEmpty() && name !in contacts) {
                            contacts.add(name)
                            if (newContactId.isNotBlank()) {
                                prefs.edit().putString("contact_p2p_" + name, newContactId.trim()).apply()
                            }
                        }
                        newContactId = ""
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
                        if (name.isNotEmpty() && name !in AppState.categories) {
                            AppState.categories = AppState.categories + name
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

    if (showQuarantine) {
        AlertDialog(
            onDismissRequest = { showQuarantine = false },
            title = { Text("Карантин") },
            text = {
                Column {
                    if (blockedContacts.isEmpty()) {
                        Text("Нет заблокированных контактов")
                    } else {
                        blockedContacts.forEach { name ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(name, modifier = Modifier.weight(1f))

                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showQuarantine = false }) { Text("Закрыть") } }
        )
    }

    selectedChat?.let { chat ->

        Dialog(onDismissRequest = { selectedChat = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(chat.title, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppState.categories.forEach { cat ->
                            TextButton(
                                onClick = {
                                    val index = chats.indexOfFirst { it.id == chat.id }
                                    if (index >= 0) chats[index] = chats[index].copy(category = cat)
                                    selectedChat = null
                                }
                            ) {
                                Text(
                                    if (chat.category == cat) "• " + cat else cat,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val index = chats.indexOfFirst { it.id == chat.id }
                                if (index >= 0) chats[index] = chats[index].copy(pinned = !chat.pinned)
                                selectedChat = null
                            }
                        ) { Text(if (chat.pinned) "Открепить" else "Закрепить", color = MaterialTheme.colorScheme.primary) }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val index = chats.indexOfFirst { it.id == chat.id }
                                if (index >= 0) chats[index] = chats[index].copy(muted = !chat.muted)
                                selectedChat = null
                            }
                        ) { Text(if (chat.muted) "Со звуком" else "Без звука", color = MaterialTheme.colorScheme.primary) }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                blockedContacts = blockedContacts + chat.title
                                QuarantineStore.save(quarantineContext, blockedContacts)
                                selectedChat = null
                            }
                        ) { Text("Заблокировать", color = Color(0xFFD24B4B)) }

                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                chats.removeAll { it.id == chat.id }
                                val arr = JSONArray()
                                chats.forEach { c ->
                                    arr.put(JSONObject().apply {
                                        put("id", c.id)
                                        put("title", c.title)
                                        put("preview", c.preview)
                                        put("archived", c.archived)
                                        put("hidden", c.hidden)
                                        put("category", c.category)
                                        put("unread", c.unread)
                                    })
                                }
                                prefs.edit().putString("chats", arr.toString()).apply()
                                selectedChat = null
                            }
                        ) { Text("Удалить", color = Color(0xFFD24B4B)) }
                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = { selectedChat = null }
                        ) { Text("Отмена", color = MaterialTheme.colorScheme.onBackground) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatRow(
    chat: Chat,
    onOpenChat: () -> Unit,
    onLongPress: () -> Unit,
    onSwipeArchive: () -> Unit,
    isArchived: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeArchive()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {
            Box(modifier = Modifier.fillMaxWidth())
        },
        enableDismissFromStartToEnd = false
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onOpenChat,
                onLongClick = onLongPress
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChatAvatar(chat.title)

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chat.pinned) {
                    PinIcon()
                    Spacer(Modifier.width(5.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(chat.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
                                if (chat.muted) {
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Filled.NotificationsOff, contentDescription = "Без звука", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                                }
                            }
            }

            Text(
                text = chat.preview,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (chat.lastMessageTime > 0L || chat.messageStatus > 0) {
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (chat.lastMessageTime > 0L) {
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = chat.lastMessageTime
                    val now = java.util.Calendar.getInstance()
                    val timeText = if (
                        cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                        cal.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)
                    ) {
                        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(cal.time)
                    } else {
                        java.text.SimpleDateFormat("dd.MM", java.util.Locale.getDefault()).format(cal.time)
                    }
                    Text(timeText, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                if (chat.messageStatus == 1) {
                    Text("✓", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                } else if (chat.messageStatus == 2) {
                    Text("✓✓", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }
        }
    }
    }
}



@Composable
private fun PinIcon(color: Color = MaterialTheme.colorScheme.primary, size: Dp = 14.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.16f, cap = StrokeCap.Round)
        rotate(45f, pivot = Offset(w * 0.5f, h * 0.5f)) {
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.28f, h * 0.06f),
                size = Size(w * 0.44f, h * 0.42f),
                cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                style = stroke
            )
            drawLine(
                color = color,
                start = Offset(w * 0.5f, h * 0.48f),
                end = Offset(w * 0.5f, h * 0.94f),
                strokeWidth = w * 0.16f,
                cap = StrokeCap.Round
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
    val bgColor = MaterialTheme.colorScheme.background
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
            color = bgColor,
            topLeft = Offset(w * 0.38f, h * 0.55f),
            size = Size(w * 0.24f, h * 0.09f),
            cornerRadius = CornerRadius(w * 0.04f)
        )
    }
}
