package com.voidnode.app.ui.screens

import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoCircle(path: String) {
    var playing by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(Color.Black)
            .clickable { playing = !playing }
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoPath(path)
                    setOnPreparedListener { it.isLooping = false }
                }
            },
            update = { vv ->
                if (playing) {
                    if (!vv.isPlaying) vv.start()
                } else if (vv.isPlaying) vv.pause()
            }
        )
    }
}
