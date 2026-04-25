package com.example.kwagae.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// 🎨 Anime Color Palette - Pastel Dream
private val AnimeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF9A9E),        // sakura pink
    onPrimary = Color(0xFF2D1B36),
    primaryContainer = Color(0xFFFFB7B9),
    onPrimaryContainer = Color(0xFF2D1B36),

    secondary = Color(0xFFA8E6CF),      // mint green
    onSecondary = Color(0xFF1B2F2B),
    secondaryContainer = Color(0xFFC3F2E3),
    onSecondaryContainer = Color(0xFF1B2F2B),

    tertiary = Color(0xFFFFD3B6),       // peach orange
    onTertiary = Color(0xFF2D1F15),

    background = Color(0xFF1A1A2E),     // deep night sky
    onBackground = Color(0xFFF0EFF4),

    surface = Color(0xFF252542),        // dark lavender surface
    onSurface = Color(0xFFF0EFF4),

    surfaceVariant = Color(0xFF2D2D4A),
    onSurfaceVariant = Color(0xFFC9C9E0),

    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2D1B1B),

    outline = Color(0xFFFF9A9E).copy(alpha = 0.3f)
)

// 🌸 Light Anime Theme - Kawaii Pastel
private val AnimeLightColorScheme = lightColorScheme(
    primary = Color(0xFFFF6B8A),        // vibrant sakura
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0E6),
    onPrimaryContainer = Color(0xFF5C2E3E),

    secondary = Color(0xFF6DD5B5),      // fresh mint
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7F0),
    onSecondaryContainer = Color(0xFF1C4A3B),

    tertiary = Color(0xFFFFB347),       // warm amber
    onTertiary = Color.White,

    background = Color(0xFFFEFAF6),     // soft cream
    onBackground = Color(0xFF2C2C3A),

    surface = Color(0xFFFFFFFF),        // pure white cards
    onSurface = Color(0xFF2C2C3A),

    surfaceVariant = Color(0xFFF5F0FF),
    onSurfaceVariant = Color(0xFF5D5D75),

    error = Color(0xFFFF8A8A),
    onError = Color.White,

    outline = Color(0xFFFFB7C5).copy(alpha = 0.5f)
)

// ✨ Custom Anime Typography
private val AnimeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)

// 🌟 Custom Anime Shapes (rounded + playful)
val AnimeShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)

@Composable
fun KwaGaeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useAnimeStyle: Boolean = true,  // Toggle between anime and glass style
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        useAnimeStyle && darkTheme -> AnimeDarkColorScheme
        useAnimeStyle && !darkTheme -> AnimeLightColorScheme
        !useAnimeStyle && darkTheme -> GlassDarkColorScheme
        else -> GlassLightColorScheme
    }

    val typography = if (useAnimeStyle) AnimeTypography else Typography()
    val shapes = if (useAnimeStyle) AnimeShapes else Shapes()

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Anime style: colorful status bar with gradient effect
            if (useAnimeStyle) {
                window.statusBarColor = if (darkTheme)
                    Color(0xFF1A1A2E).toArgb()
                else
                    Color(0xFFFFF0F5).toArgb()

                window.navigationBarColor = if (darkTheme)
                    Color(0xFF252542).toArgb()
                else
                    Color(0xFFFFF5F8).toArgb()

                WindowCompat.getInsetsController(window, view)
                    .isAppearanceLightStatusBars = !darkTheme
            } else {
                // Glass style: transparent bars
                window.statusBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view)
                    .isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

// Keep your original Glass color schemes
private val GlassDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4B8EFF),
    onPrimary = Color.White,
    secondary = Color(0xFF74D1FF),
    onSecondary = Color(0xFF003548),
    background = Color(0xFF121317),
    onBackground = Color(0xFFE3E2E7),
    surface = Color.White.copy(alpha = 0.08f),
    onSurface = Color(0xFFE3E2E7),
    surfaceVariant = Color.White.copy(alpha = 0.05f),
    onSurfaceVariant = Color(0xFFC1C6D7),
    outline = Color.White.copy(alpha = 0.15f),
    error = Color(0xFFFFB4AB)
)

private val GlassLightColorScheme = lightColorScheme(
    primary = Color(0xFF005BC1),
    onPrimary = Color.White,
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF1A1B1F),
    surface = Color.White.copy(alpha = 0.6f),
    onSurface = Color.Black,
    surfaceVariant = Color.White.copy(alpha = 0.4f),
    onSurfaceVariant = Color.DarkGray,
    outline = Color.Gray.copy(alpha = 0.3f)
)