package com.voidnode.app.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

object VoidNet {

    private const val PORT = 5050
    private const val TAG = "VoidNet"

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: PrintWriter? = null

    var appContext: Context? = null
    var currentOpenChat: String? = null
    var encryptionKey: String? = null
    var onVoice: ((String, String, String, Int) -> Unit)? = null
    var onTyping: ((String) -> Unit)? = null
    var uiListener: ((String, String, String, String) -> Unit)? = null
    private var serverStarted = false
    private var notificationId = 1000

    var onMessage: ((String, String, String, String) -> Unit)? = null

    fun showNotification(chat: String, senderName: String, text: String) {
        val ctx = appContext ?: return
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "voidnet_messages",
                "Сообщения VoidNode",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
        val pendingIntent = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(ctx, "voidnet_messages")
        } else {
            Notification.Builder(ctx)
        }

        builder
            .setContentTitle("VoidNode: " + chat)
            .setContentText(senderName + ": " + text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        manager.notify(notificationId++, builder.build())
    }

    fun sendVoice(chat: String, file: File, senderName: String, senderId: String, durationSec: Int) {
        Thread {
            try {
                val bytes = file.readBytes()
                val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                val audio = if (encryptionKey != null) {
                    "ENC:" + CryptoUtil.encrypt(b64, encryptionKey!!)
                } else {
                    b64
                }

                val json = JSONObject()
                    .put("chat", chat)
                    .put("voice", true)
                    .put("audio", audio)
                    .put("duration", durationSec)
                    .put("senderName", senderName)
                    .put("senderId", senderId)

                writer?.println(json.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка отправки голосового: " + e.message)
            }
        }.start()
    }

    fun sendTyping(chat: String) {
        try {
            val json = JSONObject()
            json.put("chat", chat)
            json.put("typing", true)
            writer?.println(json.toString())
            writer?.flush()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка отправки typing: " + e.message)
        }
    }

    private fun handleLine(line: String) {
        try {
            val json = JSONObject(line)
            val chat = json.optString("chat", "Локальный чат")
            val text = json.optString("text", "")
            val senderName = json.optString("senderName", "Собеседник")
            val senderId = json.optString("senderId", "")

            var displayText = text
            if (displayText.startsWith("ENC:")) {
                displayText = try {
                    if (encryptionKey != null) {
                        CryptoUtil.decrypt(displayText.removePrefix("ENC:"), encryptionKey!!)
                    } else {
                        "[Зашифрованное сообщение]"
                    }
                } catch (e: Exception) {
                    "[Зашифрованное сообщение]"
                }
            }

            if (json.has("audio")) {
                val audio = json.optString("audio")
                val dur = json.optInt("duration", 0)

                val b64 = try {
                    if (audio.startsWith("ENC:")) {
                        if (encryptionKey != null) {
                            CryptoUtil.decrypt(audio.removePrefix("ENC:"), encryptionKey!!)
                        } else {
                            null
                        }
                    } else {
                        audio
                    }
                } catch (e: Exception) {
                    null
                }

                if (b64 != null) {
                    val dir = File(appContext?.filesDir, "voices").apply { mkdirs() }
                    val f = File(dir, "voice_" + System.currentTimeMillis() + ".m4a")
                    f.writeBytes(android.util.Base64.decode(b64, android.util.Base64.NO_WRAP))
                    onVoice?.invoke(chat, f.absolutePath, senderName, dur)
                }
                return
            }

            if (json.optBoolean("typing", false)) {
                onTyping?.invoke(chat)
                return
            }

            if (text.isNotEmpty()) {
                uiListener?.invoke(chat, displayText, senderName, senderId)

                if (chat != currentOpenChat) {
                    showNotification(chat, senderName, displayText)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка разбора сообщения: ${e.message}")
        }
    }

    fun localIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val nif = interfaces.nextElement()
                val addresses = nif.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    val host = addr.hostAddress ?: continue
                    if (!addr.isLoopbackAddress && !host.contains(":")) {
                        return host
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun startServer() {
        if (serverStarted) return
        serverStarted = true
        Thread {
            try {
                serverSocket = ServerSocket(PORT)
                Log.i(TAG, "Сервер запущен на порту $PORT")

                while (true) {
                    val socket = serverSocket!!.accept()
                    clientSocket = socket
                    writer = PrintWriter(socket.getOutputStream(), true)

                    val reader = BufferedReader(
                        InputStreamReader(socket.getInputStream())
                    )

                    Thread {
                        try {
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                handleLine(line ?: continue)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Ошибка чтения: ${e.message}")
                        }
                    }.start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка сервера: ${e.message}")
            }
        }.start()
    }

    fun connect(ip: String) {
        Thread {
            try {
                val socket = Socket(ip, PORT)
                clientSocket = socket
                writer = PrintWriter(socket.getOutputStream(), true)
                Log.i(TAG, "Подключено к $ip")

                val reader = BufferedReader(
                    InputStreamReader(socket.getInputStream())
                )

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    handleLine(line ?: continue)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка подключения: ${e.message}")
            }
        }.start()
    }

    fun sendMessage(
        chat: String,
        text: String,
        senderName: String = "Я",
        senderId: String = "",
        recipientId: String = ""
    ) {
        Thread {
            val payload = JSONObject()
                .put("chat", chat)
                .put("text", if (encryptionKey != null) "ENC:" + CryptoUtil.encrypt(text, encryptionKey!!) else text)
                .put("senderName", senderName)
                .put("senderId", senderId)
            var sentDirect = false
            try {
                val w = writer
                if (w != null) {
                    w.println(payload.toString())
                    sentDirect = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка отправки: " + e.message)
            }
            if (!sentDirect && recipientId.isNotBlank()) {
                sendViaRelay(recipientId, chat, text, senderName, senderId)
            }
        }.start()
    }

    fun sendViaRelay(to: String, chat: String, text: String, senderName: String, senderId: String) {
        try {
            val body = JSONObject()
                .put("to", to)
                .put("chat", chat)
                .put("text", if (encryptionKey != null) "ENC:" + CryptoUtil.encrypt(text, encryptionKey!!) else text)
                .put("from", senderName)
                .put("senderId", senderId)
            val url = java.net.URL(Relay.relayUrl + "/send")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            Log.i(TAG, "Сообщение отправлено через релей")
        } catch (e: Exception) {
            Log.e(TAG, "Релей недоступен: " + e.message)
        }
    }

    fun stop() {
        try {
            clientSocket?.close()
            serverSocket?.close()
        } catch (_: Exception) {
        }
    }
}
