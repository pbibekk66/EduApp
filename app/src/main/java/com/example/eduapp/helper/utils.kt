package com.example.eduapp.helper

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

/**
 * Utility function to load an ImageBitmap from an asset path on a background thread.
 * Using Dispatchers.IO ensures that disk I/O does not block the main UI thread, preventing ANRs.
 */
suspend fun loadAssetImageAsync(context: Context, path: String): ImageBitmap? {
    return withContext(Dispatchers.IO) {
        try {
            context.assets.open(path).use { inputStream: InputStream ->
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Composable function that loads and remembers the ImageBitmap asynchronously.
 * Uses LaunchedEffect to trigger the background load whenever the path changes.
 */
@Composable
fun rememberAssetImage(path: String): ImageBitmap? {
    val context = LocalContext.current
    var imageBitmap by remember(path) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(path) {
        if (path.isNotEmpty()) {
            imageBitmap = loadAssetImageAsync(context, path)
        } else {
            imageBitmap = null
        }
    }

    return imageBitmap
}

/**
 * Utility function to play a sound from the raw resources.
 * FEATURE: Sound Effects Management.
 * Runs playback in a separate thread to ensure game performance is not affected by audio initialization.
 */
fun playSound(context: Context, soundResName: String) {
    // Dynamic resource lookup allows us to call sounds by name string.
    val resId = context.resources.getIdentifier(soundResName, "raw", context.packageName)
    if (resId != 0) {
        Thread {
            try {
                val mediaPlayer = MediaPlayer.create(context, resId)
                mediaPlayer?.let { player ->
                    // Ensures the player is properly released from memory after the sound finishes.
                    player.setOnCompletionListener { mp -> mp.release() }
                    player.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
