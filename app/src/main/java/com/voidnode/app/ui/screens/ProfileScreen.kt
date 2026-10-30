package com.voidnode.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.random.Random
import com.voidnode.app.data.Profile
import com.voidnode.app.data.ProfileStorage

@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var profile by remember {
        mutableStateOf(ProfileStorage.load(context))
    }
    var name by remember(profile) {
        mutableStateOf(profile.name)
    }
    var avatarUri by remember(profile) {
        mutableStateOf(profile.avatarUri)
    }
    var saved by remember { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("voidnode_data", Context.MODE_PRIVATE) }
    val p2pId = remember {
        var id = prefs.getString("p2p_id", "")
        if (id.isNullOrBlank()) {
            id = (1..4).joinToString("-") { (1..4).map { "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"[Random.nextInt(32)] }.joinToString("") }
            prefs.edit().putString("p2p_id", id).apply()
        }
        id
    }
    var copied by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val file = File(context.filesDir, "avatar.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                avatarUri = Uri.fromFile(file).toString()
            } catch (e: Exception) {
                avatarUri = uri.toString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { photoPicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri.isNotBlank()) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "Аватар",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Фото",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Нажми на аватар, чтобы выбрать фото",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                saved = false
            },
            label = { Text("Имя") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
            )
        )

        Spacer(Modifier.height(20.dp))

        LaunchedEffect(copied) {
            if (copied) {
                delay(2000)
                copied = false
            }
        }
        LaunchedEffect(saved) {
            if (saved) {
                delay(2000)
                saved = false
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "P2P-ID: " + p2pId,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("VoidNode P2P-ID", p2pId))
                copied = true
            }) {
                Text("Копировать")
            }
        }

        if (copied) {
            Text(
                text = "P2P-ID скопирован",
                color = Color(0xFF4ADE80),
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val updated = profile.copy(
                    name = name.trim(),
                    avatarUri = avatarUri
                )
                ProfileStorage.save(context, updated)
                profile = updated
                saved = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }

        if (saved) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Профиль сохранён",
                color = Color(0xFF4ADE80),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
