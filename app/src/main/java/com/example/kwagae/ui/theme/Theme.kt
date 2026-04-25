package com.example.kwagae.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KwagaeLightColorScheme = lightColorScheme(
    primary          = KwagaeTeal,
    onPrimary        = KwagaeCard,
    primaryContainer = KwagaeTealLight,
    onPrimaryContainer = KwagaeTealDark,

    secondary        = KwagaeAmber,
    onSecondary      = KwagaeTextPrimary,
    secondaryContainer = KwagaeAmberLight,
    onSecondaryContainer = KwagaeAmberDark,

    background       = KwagaeSurface,
    onBackground     = KwagaeTextPrimary,

    surface          = KwagaeCard,
    onSurface        = KwagaeTextPrimary,
    surfaceVariant   = KwagaeBorder,
    onSurfaceVariant = KwagaeTextSecondary,

    error            = KwagaeError,
    outline          = KwagaeBorder
)

private val KwagaeDarkColorScheme = darkColorScheme(
    primary          = KwagaeTealDark80,
    onPrimary        = KwagaeTealDark,
    primaryContainer = KwagaeTeal,
    onPrimaryContainer = KwagaeTealDark80,

    secondary        = KwagaeAmberDark80,
    onSecondary      = KwagaeDarkBg,
    secondaryContainer = KwagaeAmberDark,
    onSecondaryContainer = KwagaeAmberDark80,

    background       = KwagaeDarkBg,
    onBackground     = KwagaeCard,

    surface          = KwagaeDarkSurface,
    onSurface        = KwagaeCard,
    surfaceVariant   = KwagaeDarkCard,
    onSurfaceVariant = KwagaeTextHint,

    error            = KwagaeError,
    outline          = KwagaeDarkCard
)

@Composable
fun KwaGaeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We use our own custom scheme — no dynamic color
    // so branding stays consistent on all devices
    val colorScheme = if (darkTheme) KwagaeDarkColorScheme else KwagaeLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}