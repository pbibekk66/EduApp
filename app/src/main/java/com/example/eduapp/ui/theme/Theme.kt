package com.example.eduapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun EduAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    fontSizeMultiplier: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val typography = Typography.copy(
        bodyLarge = Typography.bodyLarge.copy(fontSize = Typography.bodyLarge.fontSize * fontSizeMultiplier),
        bodyMedium = Typography.bodyMedium.copy(fontSize = Typography.bodyMedium.fontSize * fontSizeMultiplier),
        bodySmall = Typography.bodySmall.copy(fontSize = Typography.bodySmall.fontSize * fontSizeMultiplier),
        titleLarge = Typography.titleLarge.copy(fontSize = Typography.titleLarge.fontSize * fontSizeMultiplier),
        titleMedium = Typography.titleMedium.copy(fontSize = Typography.titleMedium.fontSize * fontSizeMultiplier),
        titleSmall = Typography.titleSmall.copy(fontSize = Typography.titleSmall.fontSize * fontSizeMultiplier),
        labelLarge = Typography.labelLarge.copy(fontSize = Typography.labelLarge.fontSize * fontSizeMultiplier),
        labelMedium = Typography.labelMedium.copy(fontSize = Typography.labelMedium.fontSize * fontSizeMultiplier),
        labelSmall = Typography.labelSmall.copy(fontSize = Typography.labelSmall.fontSize * fontSizeMultiplier),
        displayLarge = Typography.displayLarge.copy(fontSize = Typography.displayLarge.fontSize * fontSizeMultiplier),
        displayMedium = Typography.displayMedium.copy(fontSize = Typography.displayMedium.fontSize * fontSizeMultiplier),
        displaySmall = Typography.displaySmall.copy(fontSize = Typography.displaySmall.fontSize * fontSizeMultiplier),
        headlineLarge = Typography.headlineLarge.copy(fontSize = Typography.headlineLarge.fontSize * fontSizeMultiplier),
        headlineMedium = Typography.headlineMedium.copy(fontSize = Typography.headlineMedium.fontSize * fontSizeMultiplier),
        headlineSmall = Typography.headlineSmall.copy(fontSize = Typography.headlineSmall.fontSize * fontSizeMultiplier)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
