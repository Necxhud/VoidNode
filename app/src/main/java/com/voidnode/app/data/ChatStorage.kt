package com.voidnode.app.data

import android.content.Context
import com.voidnode.app.ui.screens.Message
import org.json.JSONArray
import org.json.JSONObject

object ChatStorage {

    private const val PREFS_NAME = "voidnode_chats"

    fun loadMessages(context: Context, chatTitle: String): List<Message> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(chatTitle, null) ?: return emptyList()
        val messages = mutableListOf<Message>()

        try {
            val array = JSONArray(json)

            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                messages.add(
                    Message(
                        text = item.getString("text"),
                        time = item.getString("time"),
                        isMe = item.optBoolean("isMe", true)
                    )
                )
            }
        } catch (exception: Exception) {
            return emptyList()
        }

        return messages
    }

    fun saveMessages(
        context: Context,
        chatTitle: String,
        messages: List<Message>
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()

        messages.forEach { message ->
            val item = JSONObject()
            item.put("text", message.text)
            item.put("time", message.time)
            item.put("isMe", message.isMe)
            array.put(item)
        }

        prefs.edit().putString(chatTitle, array.toString()).apply()
    }
}
