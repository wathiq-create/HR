package com.example.ui.theme

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
    primary = HrDarkPrimary,
    onPrimary = HrDarkOnPrimary,
    primaryContainer = HrDarkPrimaryContainer,
    onPrimaryContainer = HrDarkOnPrimaryContainer,
    secondary = HrDarkSecondary,
    onSecondary = HrDarkOnSecondary,
    secondaryContainer = HrDarkSecondaryContainer,
    onSecondaryContainer = HrDarkOnSecondaryContainer,
    tertiary = HrDarkTertiary,
    onTertiary = HrDarkOnTertiary,
    tertiaryContainer = HrDarkTertiaryContainer,
    onTertiaryContainer = HrDarkOnTertiaryContainer,
    background = HrDarkBackground,
    onBackground = HrDarkOnBackground,
    surface = HrDarkSurface,
    onSurface = HrDarkOnSurface,
    surfaceVariant = HrDarkSurfaceVariant,
    onSurfaceVariant = HrDarkOnSurfaceVariant,
    outline = HrDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = HrPrimary,
    onPrimary = HrOnPrimary,
    primaryContainer = HrPrimaryContainer,
    onPrimaryContainer = HrOnPrimaryContainer,
    secondary = HrSecondary,
    onSecondary = HrOnSecondary,
    secondaryContainer = HrSecondaryContainer,
    onSecondaryContainer = HrOnSecondaryContainer,
    tertiary = HrTertiary,
    onTertiary = HrOnTertiary,
    tertiaryContainer = HrTertiaryContainer,
    onTertiaryContainer = HrOnTertiaryContainer,
    background = HrBackground,
    onBackground = HrOnBackground,
    surface = HrSurface,
    onSurface = HrOnSurface,
    surfaceVariant = HrSurfaceVariant,
    onSurfaceVariant = HrOnSurfaceVariant,
    outline = HrOutline
)

@Composable
fun HrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    HrTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
