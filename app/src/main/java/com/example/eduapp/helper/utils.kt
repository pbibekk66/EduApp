package com.example.eduapp.helper

//utility functions
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.IOException
import java.io.InputStream

/**
 * Utility function to load an ImageBitmap from an asset path.
 */
fun loadAssetImage(context: Context, path: String): ImageBitmap? {
    return try {
        // Explicitly specifying the type 'InputStream' helps resolve the 'use' extension
        context.assets.open(path).use { inputStream: InputStream ->
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

/**
 * Composable function that remembers the loaded ImageBitmap.
 */
@Composable
fun rememberAssetImage(path: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(path) {
        loadAssetImage(context, path)
    }
}

/**
 * Utility function to play a sound from the raw resources.
 */
fun playSound(context: Context, soundResName: String) {
    val resId = context.resources.getIdentifier(soundResName, "raw", context.packageName)
    if (resId != 0) {
        try {
            val mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.let {
                it.setOnCompletionListener { player -> player.release() }
                it.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
