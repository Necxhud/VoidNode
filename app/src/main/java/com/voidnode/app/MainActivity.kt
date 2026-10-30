package com.voidnode.app

import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import java.io.File
import com.voidnode.app.ui.theme.ThemeState
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.voidnode.app.ui.VoidNodeApp
import com.voidnode.app.ui.theme.VoidNodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val text = "Thread: " + t.name + "" + android.util.Log.getStackTraceString(e)
                try {
                    getExternalFilesDir(null)?.let { File(it, "crash.txt").writeText(text) }
                } catch (ignored: Exception) {}
                try {
                    if (android.os.Build.VERSION.SDK_INT >= 29) {
                        val values = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, "voidnode_crash.txt")
                            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                        }
                        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        uri?.let { contentResolver.openOutputStream(it)?.write(text.toByteArray()) }
                    } else {
                        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "voidnode_crash.txt").writeText(text)
                    }
                } catch (ignored: Exception) {}
            } catch (ignored: Exception) {}
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        val prefs = getSharedPreferences("voidnode_data", MODE_PRIVATE)
        ThemeState.dark = prefs.getBoolean("dark_theme", true)
        try {
            val f = File(getExternalFilesDir(null), "crash.txt")
            val prev = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { t, e ->
                try {
                    f.writeText(e.stackTraceToString())
                } catch (_: Exception) {}
                try {
                    val cv = ContentValues()
                    cv.put(MediaStore.Downloads.DISPLAY_NAME, "voidnode_crash.txt")
                    cv.put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { os ->
                            os.write(e.stackTraceToString().toByteArray())
                        }
                    }
                } catch (_: Exception) {}
                prev?.uncaughtException(t, e)
            }
        } catch (_: Exception) {}
        setContent {
            VoidNodeTheme(darkTheme = ThemeState.dark) {
                VoidNodeApp()
            }
        }
    }
}
