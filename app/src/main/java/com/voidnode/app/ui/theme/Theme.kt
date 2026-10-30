package com.voidnode.app.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

object ThemeState {
    var dark by mutableStateOf(true)
    var dynamicVersion by mutableStateOf(0)
}

private val DarkColors = darkColorScheme(
    primary = VoidPrimary,
    secondary = VoidSecondary,
    background = VoidBackground,
    surface = VoidSurface,
    onPrimary = VoidText,
    onSecondary = VoidBackground,
    onBackground = VoidText,
    onSurface = VoidText
)

private val LightColors = lightColorScheme(
    primary = VoidPrimary,
    secondary = VoidSecondary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightText,
    onSurface = LightText
)

@Composable
fun VoidNodeTheme(
    darkTheme: Boolean = ThemeState.dark,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("voidnode_data", Context.MODE_PRIVATE) }
    val dynamicEnabled = prefs.getBoolean("dynamic_color", false)
    ThemeState.dynamicVersion
    val colorScheme = when {
        dynamicEnabled && Build.VERSION.SDK_INT >= 31 ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
