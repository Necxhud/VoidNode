package com.voidnode.app.data

import android.content.Context
import org.json.JSONObject

data class Profile(
    val name: String = "",
    val avatarUri: String = ""
)

object ProfileStorage {
    private const val FILE = "profile.json"

    fun load(context: Context): Profile {
        return try {
            val text = context.openFileInput(FILE).bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            Profile(
                name = json.optString("name", ""),
                avatarUri = json.optString("avatarUri", "")
            )
        } catch (e: Exception) {
            Profile()
        }
    }

    fun save(context: Context, profile: Profile) {
        val json = JSONObject()
        json.put("name", profile.name)
        json.put("avatarUri", profile.avatarUri)

        context.openFileOutput(FILE, Context.MODE_PRIVATE).use {
            it.write(json.toString().toByteArray())
        }
    }
}
