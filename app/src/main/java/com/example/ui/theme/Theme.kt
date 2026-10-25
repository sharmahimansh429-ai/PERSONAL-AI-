package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NexusDarkColorScheme = darkColorScheme(
    primary = NexusSilverBright,
    onPrimary = NexusPureBlack,
    primaryContainer = NexusSurfaceElevated,
    onPrimaryContainer = NexusSilverBright,
    secondary = NexusTitanium,
    onSecondary = NexusPureBlack,
    secondaryContainer = NexusSurfaceDark,
    onSecondaryContainer = NexusSilverBright,
    tertiary = ParticleCyan,
    onTertiary = NexusPureBlack,
    background = NexusPureBlack,
    onBackground = NexusSilverBright,
    surface = NexusCardDark,
    onSurface = NexusSilverBright,
    surfaceVariant = NexusSurfaceDark,
    onSurfaceVariant = NexusSilver,
    outline = NexusBorderHairline,
    outlineVariant = NexusSilverDark
)

@Composable
fun NexusAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = NexusDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = NexusPureBlack.toArgb()
                window.navigationBarColor = NexusPureBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
