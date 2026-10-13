package com.voidnode.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.voidnode.app.ui.VoidNodeApp
import com.voidnode.app.ui.theme.VoidNodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VoidNodeTheme {
                VoidNodeApp()
            }
        }
    }
}
