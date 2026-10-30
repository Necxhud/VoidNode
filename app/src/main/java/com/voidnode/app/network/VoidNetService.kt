package com.voidnode.app.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class VoidNetService : Service() {

    override fun onCreate() {
        super.onCreate()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "voidnet_service",
                "VoidNode работает в фоне",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "voidnet_service")
        } else {
            Notification.Builder(this)
        }

        builder
            .setContentTitle("VoidNode")
            .setContentText("Ожидаю сообщения по P2P")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setOngoing(true)

        startForeground(2001, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        VoidNet.appContext = this
        VoidNet.startServer()
        val prefs = getSharedPreferences("voidnode", MODE_PRIVATE)
        Relay.start(this, prefs.getString("p2p_id", "") ?: "")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
