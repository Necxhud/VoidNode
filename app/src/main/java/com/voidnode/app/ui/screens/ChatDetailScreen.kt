package com.voidnode.app.ui.screens

import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import android.net.Uri
import android.content.Context
import android.content.Intent
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import android.graphics.Bitmap
import android.app.Activity
import android.media.MediaPlayer
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import android.media.MediaRecorder
import java.io.File
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import java.net.InetAddress
import java.net.NetworkInterface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.voidnode.app.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidnode.app.network.VoidNet
import com.voidnode.app.network.VoidNetService
import com.voidnode.app.data.ProfileStorage

data class Message(
    val text: String,
    val time: String,
    val isMe: Boolean = true,
    val status: String = "read",
    val senderName: String = "",
    val isVoice: Boolean = false,
    val audioPath: String? = null,
    val durationSec: Int = 0,
    val isVideo: Boolean = false,
    val videoPath: String? = null,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
    chatTitle: String?,
    messages: List<Message>,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit,
    onDeleteMessage: (Int) -> Unit,
    onBack: () -> Unit,
    onReceiveMessage: (String, String) -> Unit = { _, _ -> },
    onVoice: (String, String, String, Int) -> Unit = { _, _, _, _ -> },
    onVoiceSent: (String, Int) -> Unit = { _, _ -> },
    onVideoSent: (String) -> Unit = {}
) {
    val chatTitleSafe = chatTitle ?: ""

    var messageText by remember { mutableStateOf("") }
    var selectedMessageIndex by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()
    var showP2P by remember { mutableStateOf(false) }
    var myIp by remember { mutableStateOf("") }
    var p2pIp by remember { mutableStateOf("") }
    var showQr by remember { mutableStateOf(false) }
    var qrText by remember { mutableStateOf("") }
    var peerTyping by remember { mutableStateOf(false) }
    var lastTypingSent by remember { mutableStateOf(0L) }
    val qrScanner = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val scan = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
        val contents = scan.contents

        if (contents != null && contents.startsWith("VOIDNET:")) {
            val ip = contents.removePrefix("VOIDNET:").substringBefore(":")
            p2pIp = ip
            VoidNet.connect(ip)
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }
    val context = LocalContext.current
    val myProfile = remember { ProfileStorage.load(context) }
    val prefs = remember { context.getSharedPreferences("voidnode_data", Context.MODE_PRIVATE) }
    var encKey by remember { mutableStateOf(prefs.getString("encryption_key", "") ?: "") }
    var isRecording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingPath by remember { mutableStateOf("") }
    var recordStart by remember { mutableStateOf(0L) }
    var recordPermissionGranted by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingPath by remember { mutableStateOf("") }
    var playProgress by remember { mutableStateOf(0f) }
    var playTick by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var recordElapsed by remember { mutableStateOf(0) }
    var playSpeed by remember { mutableStateOf(1f) }

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "voice_" + System.currentTimeMillis() + ".m4a")
            val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else MediaRecorder()
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioSamplingRate(44100)
            r.setAudioEncodingBitRate(128000)
            r.setAudioChannels(1)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            recordingPath = file.absolutePath
            recordStart = System.currentTimeMillis()
            isRecording = true
        } catch (e: Exception) {
            isRecording = false
            Toast.makeText(context, "Ошибка записи: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun stopAndSend() {
        val r = recorder
        recorder = null
        isRecording = false
        try {
            r?.stop()
        } catch (e: Exception) {
            File(recordingPath).delete()
            Toast.makeText(context, "Слишком короткое голосовое", Toast.LENGTH_SHORT).show()
            return
        } finally {
            try { r?.release() } catch (e: Exception) {}
        }

        val duration = ((System.currentTimeMillis() - recordStart) / 1000).toInt().coerceAtLeast(1)
        val file = File(recordingPath)
        if (file.exists()) {
            val voicesDir = File(context.filesDir, "voices").apply { mkdirs() }
            val saved = File(voicesDir, "voice_sent_" + System.currentTimeMillis() + ".m4a")
            file.copyTo(saved, overwrite = true)

            onVoiceSent(saved.absolutePath, duration)

            VoidNet.sendVoice(
                chatTitleSafe,
                file,
                myProfile.name.ifBlank { "Я" },
                prefs.getString("p2p_id", "") ?: "",
                duration
            )
            file.delete()
        }
    }
    LaunchedEffect(playingPath, playTick) {
        while (playingPath.isNotBlank() && player?.isPlaying == true) {
            player?.let {
                if (it.duration > 0) {
                    playProgress = it.currentPosition.toFloat() / it.duration
                }
            }
            delay(100)
        }
    }

    LaunchedEffect(isRecording) {
        recordElapsed = 0
        while (isRecording) {
            delay(1000)
            recordElapsed++
        }
    }

    LaunchedEffect(chatTitleSafe) {
        messageText = prefs.getString("draft_" + chatTitleSafe, "") ?: ""
        VoidNet.encryptionKey = prefs.getString("encryption_key", "")?.ifBlank { null }
    }

    LaunchedEffect(peerTyping) {
        if (peerTyping) {
            delay(3000)
            peerTyping = false
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        VoidNet.appContext = context
        VoidNet.onTyping = { chat ->
            if (chat == chatTitleSafe) {
                peerTyping = true
            }
        }

        VoidNet.uiListener = { _, text, senderName, _ ->
            onReceiveMessage(text, senderName)
        }

        context.startForegroundService(
            Intent(context, VoidNetService::class.java)
        )

        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(chatTitleSafe) {
        VoidNet.currentOpenChat = chatTitleSafe
        onDispose {
            VoidNet.currentOpenChat = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onBackground
                        )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (chatTitleSafe ?: "").take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onBackground,
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

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .clickable { showP2P = true }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text("P2P", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(chatTitleSafe, color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)

                Text(
                    text = if (isTyping) "печатает…" else "в сети",
                    color = if (isTyping) MaterialTheme.colorScheme.primary else Color(0xFF31C48D),
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
                    color = MaterialTheme.colorScheme.onBackground,
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
                        messages[index].time + "_" + index + "_" + messages[index].isMe
                    }
                ) { index ->
                    val message = messages[index]
                    val bubbleColor = if (message.isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
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
                            if (message.isVideo && message.videoPath != null) {
                                VideoCircle(path = message.videoPath!!)
                            } else if (message.isVoice && message.audioPath != null) {
                                val isThis = playingPath == message.audioPath
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .shadow(4.dp, CircleShape)
                                                .background(
                                                    when {
                                                        isThis && isPaused -> Color(0xFFFFA000)
                                                        isThis -> Color.White
                                                        else -> MaterialTheme.colorScheme.primary
                                                    },
                                                    CircleShape
                                                )
                                                .clickable {
                                                    if (isThis && !isPaused) {
                                                        player?.pause()
                                                        isPaused = true
                                                    } else {
                                                        isPaused = false
                                                        player?.release()
                                                        player = MediaPlayer().apply {
                                                            setDataSource(message.audioPath)
                                                            prepare()
                                                            if (isThis) {
                                                                seekTo((playProgress * message.durationSec * 1000).toInt())
                                                            }
                                                            setOnCompletionListener {
                                                                playProgress = 0f
                                                                isPaused = false
                                                                playingPath = ""
                                                                playTick++
                                                            }
                                                            start()
                                                            playbackParams = playbackParams.setSpeed(playSpeed)
                                                        }
                                                        playingPath = message.audioPath
                                                    }
                                                    playTick++
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isThis && !isPaused) {
                                                Text(
                                                    text = "❚❚",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 13.sp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = message.durationSec.toString() + "″",
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontSize = 12.sp
                                        )
                                        
                                    }
                                    Slider(
                                        value = if (isThis) playProgress else 0f,
                                        onValueChange = {
                                            playProgress = it
                                            if (isThis) {
                                                player?.let { pl ->
                                                    pl.seekTo((it * pl.duration).toInt())
                                                }
                                            }
                                        },
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.onBackground,
                                            activeTrackColor = MaterialTheme.colorScheme.onBackground,
                                            inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier
                                            .width(150.dp)
                                            .height(22.dp)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = if (playSpeed == 1f) "1x" else if (playSpeed == 1.5f) "1.5x" else "2x",
                                            color = Color(0xFF4CAF50),
                                            fontSize = 16.sp,
                                            modifier = Modifier.clickable {
                                                playSpeed = when (playSpeed) {
                                                    1f -> 1.5f
                                                    1.5f -> 2f
                                                    else -> 1f
                                                }
                                                player?.let { pl ->
                                                    pl.playbackParams = pl.playbackParams.setSpeed(playSpeed)
                                                }
                                            }
                                        )
                                }
                            } else {
                                Text(
                                    text = message.text,
                                    color = if (message.isMe) Color.White else MaterialTheme.colorScheme.onBackground,
                                    fontSize = 15.sp,
                                    lineHeight = 19.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = message.time,
                                    color = if (message.isMe) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
                                            if (message.isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground
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
                                color = MaterialTheme.colorScheme.onBackground,
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

    val audioPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        recordPermissionGranted = granted
        if (granted) startRecording()
    }


        var videoUri by remember { mutableStateOf<Uri?>(null) }
        var videoMode by remember { mutableStateOf(false) }
        val videoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CaptureVideo()
        ) { ok ->
            val u = videoUri
            if (ok && u != null) {
                val path = u.path
                if (path != null) onVideoSent(path)
            }
            videoUri = null
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
                OutlinedTextField(
                value = messageText,
                onValueChange = {
                            messageText = it
                            prefs.edit().putString("draft_" + chatTitleSafe, it).apply()

                            val now = System.currentTimeMillis()
                            if (now - lastTypingSent > 2000) {
                                VoidNet.sendTyping(chatTitleSafe)
                                lastTypingSent = now
                            }
                        },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                placeholder = { Text("Сообщение") },
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
                    .size(50.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = { videoMode = !videoMode })
                    }
                    .scale(if (isRecording) 1.1f else 1f)
                    .background(
                        if (isRecording) Color(0xFFE53935) else MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                    .then(
                        if (messageText.isBlank()) {
                            Modifier.clickable {
                                                if (!isRecording) {
                                    if (videoMode) {
                            val vf = File(context.cacheDir, "video_" + System.currentTimeMillis() + ".mp4")
                            val uri = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".fileprovider", vf)
                            videoUri = uri
                            videoLauncher.launch(uri)
                        } else if (recordPermissionGranted) {
                                        startRecording()
                                    } else {
                                        audioPermission.launch(android.Manifest.permission.RECORD_AUDIO)
                                    }
                                } else {
                                    stopAndSend()
                                }
                            }
                        } else {
                            Modifier.clickable {
                                onSendMessage(messageText)
                                VoidNet.sendMessage(chatTitleSafe, messageText, myProfile.name.ifBlank { "Я" }, prefs.getString("p2p_id", "") ?: "", prefs.getString("contact_p2p_" + chatTitleSafe, "") ?: "")
                                prefs.edit().remove("draft_" + chatTitleSafe).apply()
                                messageText = ""
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!isRecording) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .clickable { videoMode = !videoMode }
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (videoMode) Icons.Filled.Videocam else Icons.Filled.Mic,
                            contentDescription = "Переключить ГС/кружок",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
if (messageText.isBlank()) {
                    Icon(
                        imageVector = if (isRecording) Icons.Filled.Stop else if (videoMode) Icons.Filled.Videocam else Icons.Filled.Mic,
                        contentDescription = if (isRecording) "Остановить запись" else "Записать голосовое",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Отправить",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

    if (showP2P) {
        AlertDialog(
            onDismissRequest = { showP2P = false },
            title = { Text("P2P подключение") },
            text = {
                Column {
                    if (myIp.isNotBlank()) {
                        Text("Мой IP: " + myIp, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                    }
                    Text("Введи IP второго телефона в той же Wi-Fi-сети.", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = encKey,
                        onValueChange = {
                            encKey = it
                            prefs.edit().putString("encryption_key", it).apply()
                            VoidNet.encryptionKey = it.ifBlank { null }
                        },
                        singleLine = true,
                        label = { Text("Ключ шифрования") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    Row {
                        TextButton(onClick = {
                            qrText = "VOIDNET:" + (VoidNet.localIpAddress() ?: "") + ":5050"
                            showQr = true
                        }) { Text("Показать QR") }

                        Spacer(Modifier.width(8.dp))

                        TextButton(onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                val integrator = IntentIntegrator(activity)
                                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                                integrator.setPrompt("Отсканируй QR VoidNode")
                                integrator.setBeepEnabled(false)
                                qrScanner.launch(integrator.createScanIntent())
                            }
                        }) { Text("Сканировать") }
                    }

                    if (showQr) {
                        val qrBitmap = remember(qrText) {
                            val matrix: BitMatrix = QRCodeWriter().encode(
                                qrText,
                                BarcodeFormat.QR_CODE,
                                512,
                                512
                            )
                            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                            for (x in 0 until 512) {
                                for (y in 0 until 512) {
                                    bitmap.setPixel(
                                        x,
                                        y,
                                        if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                                    )
                                }
                            }
                            bitmap
                        }

                        Spacer(Modifier.height(12.dp))

                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR VoidNode",
                            modifier = Modifier.size(180.dp)
                        )


                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = p2pIp,
                        onValueChange = { p2pIp = it },
                        singleLine = true,
                        placeholder = { Text("Например: 192.168.1.25") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (p2pIp.isNotBlank()) {
                        VoidNet.connect(p2pIp.trim())
                    }
                    showP2P = false
                }) { Text("Подключиться", color = MaterialTheme.colorScheme.primary) }
            }
        )
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
