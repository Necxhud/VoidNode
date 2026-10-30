package com.voidnode.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.voidnode.app.ui.AppState
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.voidnode.app.ui.theme.ThemeState

private val settingsSections = listOf(
    "Аккаунт",
    "Настройки чатов",
    "Конфиденциальность",
    "Уведомления",
    "Внешний вид",
    "Данные и память",
    "Папки с чатами",
    "Устройство",
    "Энергосбережение",
    "Язык",
    "О функциях",
    "О приложении",
    "Отправить подарок",
    "Задать вопрос",
    "Политика конфиденциальности",
    "Вопросы о VoidNode"
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var selected by remember { mutableStateOf<String?>(null) }

BackHandler(enabled = true) {
    if (selected != null) selected = null else onBack()
}
    val settingsScrollState = rememberScrollState()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("voidnode_data", Context.MODE_PRIVATE) }
    var darkTheme by remember { mutableStateOf(ThemeState.dark) }
    var dynamicColor by remember { mutableStateOf(prefs.getBoolean("dynamic_color", false)) }
    var newCategory by remember { mutableStateOf("") }
    var exportResult by remember { mutableStateOf("") }
    var importResult by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var blockedContacts by remember { mutableStateOf(QuarantineStore.load(context)) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var showQuarantineDialog by remember { mutableStateOf(false) }
    val backupPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                val root = JSONObject(text)
                val chats = root.getJSONObject("chats")
                val chatsPrefs = context.getSharedPreferences("voidnode_chats", Context.MODE_PRIVATE)
                val editor = chatsPrefs.edit()
                editor.clear()
                chats.keys().forEach { key ->
                    editor.putString(key, chats.getString(key))
                }
                editor.apply()

                if (root.has("categories")) {
                    val dataPrefs = context.getSharedPreferences("voidnode_data", Context.MODE_PRIVATE)
                    dataPrefs.edit().putString("categories", root.getJSONArray("categories").toString()).apply()
                }

                importResult = "Импортировано чатов: " + chats.length()
            } catch (e: Exception) {
                importResult = "Ошибка импорта: " + e.message
            }
        }
    }

    Dialog(
        onDismissRequest = { if (selected != null) selected = null else onBack() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selected == null) onBack() else selected = null
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = if (selected == null) "Настройки" else selected ?: "",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (selected == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(settingsScrollState)
                    ) {
                        settingsSections.forEach { title ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selected = title }
                                    .padding(vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = title,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "›",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "VoidNode 0.0.4",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    }
                } else if (selected == "Аккаунт") {
                    ProfileScreen(
                        onBack = { selected = null }
                    )
                } else if (selected == "Настройки чатов") {
                    Column {
                        Text(
    "Категории чатов",
    color = MaterialTheme.colorScheme.onBackground,
    fontSize = 17.sp,
    fontWeight = FontWeight.Bold
)
Spacer(Modifier.height(12.dp))
if (AppState.categories.isEmpty()) {
    Text(
        "Пока нет категорий",
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        fontSize = 14.sp
    )
} else {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AppState.categories.forEachIndexed { index, cat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(cat, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Text(
                    text = "Удалить",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        AppState.categories = AppState.categories - cat
                        prefs.edit().putString("categories", JSONArray(AppState.categories).toString()).apply()
                    }
                )
            }
            if (index < AppState.categories.lastIndex) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                )
            }
        }
    }
}
Spacer(Modifier.height(20.dp))
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newCategory,
                                onValueChange = { newCategory = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("Новая категория", fontSize = 14.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val name = newCategory.trim()
                                    if (name.isNotEmpty() && name !in AppState.categories) {
                                        AppState.categories = AppState.categories + name
                                        prefs.edit().putString("categories", JSONArray(AppState.categories).toString()).apply()
                                    }
                                    newCategory = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Добавить")
                            }
                        }
                    }
                } else if (selected == "Внешний вид") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Тёмная тема", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp)
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = {
                                darkTheme = it
                                ThemeState.dark = it
                                prefs.edit().putBoolean("dark_theme", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Использовать цвета обоев", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp)
                        Switch(
                            checked = dynamicColor,
                            onCheckedChange = {
                                dynamicColor = it
                                prefs.edit().putBoolean("dynamic_color", it).apply()
                                ThemeState.dynamicVersion++
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                } else if (selected == "Конфиденциальность") {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPinDialog = true }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PIN-код", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (prefs.getString("pin_code", null) != null) "Включён" else "Выключен",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }

                        if (showPinDialog) {
                            AlertDialog(
                                onDismissRequest = { showPinDialog = false },
                                title = { Text("PIN-код") },
                                text = {
                                    Column {
                                        Text("Введите PIN (4-6 цифр)", fontSize = 14.sp)
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = pinInput,
                                            onValueChange = { pinInput = it.filter { c -> c.isDigit() }.take(6) },
                                            singleLine = true,
                                            label = { Text("Новый PIN") }
                                        )
                                    }
                                },
                                confirmButton = {
                                    Row {
                                        TextButton(onClick = {
                                            if (pinInput.length >= 4) {
                                                prefs.edit().putString("pin_code", pinInput).apply()
                                                pinInput = ""
                                                showPinDialog = false
                                                exportResult = "PIN сохранён"
                                            } else {
                                                exportResult = "PIN должен быть от 4 цифр"
                                            }
                                        }) { Text("Сохранить") }
                                        TextButton(onClick = {
                                            prefs.edit().remove("pin_code").apply()
                                            pinInput = ""
                                            showPinDialog = false
                                            exportResult = "PIN удалён"
                                        }) { Text("Удалить") }
                                    }
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showQuarantineDialog = true }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Карантин", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (blockedContacts.isEmpty()) "Пусто" else "Заблокировано: " + blockedContacts.size,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }

                        if (showQuarantineDialog) {
                            AlertDialog(
                                onDismissRequest = { showQuarantineDialog = false },
                                title = { Text("Карантин") },
                                text = {
                                    Column {
                                        if (blockedContacts.isEmpty()) {
                                            Text("Нет заблокированных контактов")
                                        } else {
                                            blockedContacts.forEach { name ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(name, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                                                    Text(
                                                        "Разблокировать",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 14.sp,
                                                        modifier = Modifier.clickable {
                                                            blockedContacts = blockedContacts - name
                                                            QuarantineStore.save(context, blockedContacts)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showQuarantineDialog = false }) { Text("Закрыть") } }
                            )
                        }
                    }

                } else if (selected == "Данные и память") {
                    Column {
                        Text("Резервная копия", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                try {
                                    val chatsPrefs = context.getSharedPreferences("voidnode_chats", Context.MODE_PRIVATE)
                                    val dataPrefs = context.getSharedPreferences("voidnode_data", Context.MODE_PRIVATE)

                                    val root = JSONObject()
                                    val chats = JSONObject()

                                    chatsPrefs.all.forEach { (title, value) ->
                                        chats.put(title, value.toString())
                                    }

                                    root.put("chats", chats)
                                    root.put("categories", JSONArray(dataPrefs.getString("categories", "[]")))
                                    root.put("exportedAt", System.currentTimeMillis())

                                    val fileName = "VoidNode-backup-" +
                                        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date()) +
                                        ".json"

                                    val values = ContentValues().apply {
                                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                                        put(MediaStore.Downloads.MIME_TYPE, "application/json")
                                    }

                                    val uri: Uri? = context.contentResolver.insert(
                                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                        values
                                    )

                                    uri?.let {
                                        context.contentResolver.openOutputStream(it)?.use { output ->
                                            output.write(root.toString().toByteArray())
                                        }
                                        exportResult = "Сохранено в Downloads: " + fileName
                                    } ?: run {
                                        exportResult = "Не удалось создать файл"
                                    }
                                } catch (e: Exception) {
                                    exportResult = "Ошибка: " + e.message
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Экспортировать все чаты")
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { backupPicker.launch("application/json") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Импортировать чаты")
                        }

                        if (importResult.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = importResult,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }

                        if (exportResult.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = exportResult,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }

                } else {
                    Text(
                        text = "Этот раздел пока в разработке.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Здесь появятся настройки раздела «" + (selected ?: "") + "».",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
