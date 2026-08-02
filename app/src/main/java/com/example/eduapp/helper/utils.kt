package com.example.eduapp.helper

//utility functions
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
 * Prevents blocking the UI thread (ANRs).
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
 * Plays on a background thread to avoid UI lag.
 */
fun playSound(context: Context, soundResName: String) {
    val resId = context.resources.getIdentifier(soundResName, "raw", context.packageName)
    if (resId != 0) {
        // Run MediaPlayer operations in a background thread to avoid blocking UI
        Thread {
            try {
                val mediaPlayer = MediaPlayer.create(context, resId)
                mediaPlayer?.let {
                    it.setOnCompletionListener { player -> player.release() }
                    it.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
