package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = NavyDarkBackground,
    primaryContainer = NavyDarkCardElevated,
    onPrimaryContainer = CyanAccent,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = NavyDarkCard,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = NavyDarkBackground,
    onBackground = TextPrimaryDark,
    surface = NavyDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NavyDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = NavyBorder,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = ElectricBlue,
    secondary = CyanAccentVariant,
    onSecondary = Color.White,
    secondaryContainer = NavyLightCard,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = NavyLightBackground,
    onBackground = TextPrimaryLight,
    surface = NavyLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = NavyLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = NavyLightBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to professional dark camera theme
    dynamicColor: Boolean = false, // Keep consistent branding colors
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
