package com.voidnode.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppState {
    var categories by mutableStateOf(listOf<String>())
}
