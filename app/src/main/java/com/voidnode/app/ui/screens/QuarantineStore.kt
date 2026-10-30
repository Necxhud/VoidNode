package com.voidnode.app.ui.screens

import android.content.Context

object QuarantineStore {
    private const val PREFS = "quarantine_prefs"
    private const val KEY = "blocked_contacts"

    fun load(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY, emptySet())?.toSet() ?: emptySet()
    }

    fun save(context: Context, blocked: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY, blocked).apply()
    }
}
