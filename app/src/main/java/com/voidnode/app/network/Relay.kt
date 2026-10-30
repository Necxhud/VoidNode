package com.voidnode.app.network

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

object Relay {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    var relayUrl = "http://127.0.0.1:8000"

    fun start(context: Context, p2pId: String) {
        if (running || p2pId.isBlank()) return
        running = true
        val prefs = context.getSharedPreferences("voidnode", Context.MODE_PRIVATE)
        relayUrl = prefs.getString("relay_url", relayUrl) ?: relayUrl
        val task = object : Runnable {
            override fun run() {
                try {
                    val conn = URL(relayUrl + "/fetch?id=" + p2pId).openConnection() as HttpURLConnection
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    val text = conn.inputStream.bufferedReader().readText()
                    val arr = JSONObject(text).optJSONArray("messages") ?: JSONArray()
                    for (i in 0 until arr.length()) {
                        val m = arr.getJSONObject(i)
                        VoidNet.showNotification(
                            m.optString("chat", "Чат"),
                            m.optString("from", ""),
                            m.optString("text", "")
                        )
                    }
                } catch (e: Exception) {
                }
                handler.postDelayed(this, 15000)
            }
        }
        handler.post(task)
    }
}
