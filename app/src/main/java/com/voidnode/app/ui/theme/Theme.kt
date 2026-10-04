package com.voidnode.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VoidColors = darkColorScheme(
    primary = VoidPrimary,
    secondary = VoidSecondary,
    background = VoidBackground,
    surface = VoidSurface,
    onPrimary = VoidText,
    onSecondary = VoidBackground,
    onBackground = VoidText,
    onSurface = VoidText
)

@Composable
fun VoidNodeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VoidColors,
        content = content
    )
}
