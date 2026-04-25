package com.example.kwagae.ui.theme

import androidx.compose.ui.graphics.Color

// 🎨 PRIMARY (Glass Blue instead of dull teal)
val KwagaePrimary        = Color(0xFF4B8EFF)  // brighter, more modern
val KwagaePrimaryLight   = Color(0xFF82B1FF)
val KwagaePrimaryDark    = Color(0xFF005BC1)

// 🍯 ACCENT (keep Botswana warmth but refine)
val KwagaeAccent         = Color(0xFFFFC107)  // cleaner amber
val KwagaeAccentLight    = Color(0xFFFFE082)
val KwagaeAccentDark     = Color(0xFFFF8F00)

// 🌙 BACKGROUND (true glass base)
val KwagaeDarkBg         = Color(0xFF121317)
val KwagaeDarkSurface    = Color(0xFF1A1B1F)
val KwagaeDarkCard       = Color(0xFF202226)

// 🔮 GLASS LAYERS
val KwagaeGlass          = Color.White.copy(alpha = 0.08f)
val KwagaeGlassStrong    = Color.White.copy(alpha = 0.12f)
val KwagaeGlassBorder    = Color.White.copy(alpha = 0.18f)

// ✍️ TEXT (FIXED FOR VISIBILITY)
val KwagaeTextPrimary    = Color(0xFFFFFFFF)        // pure white (main fix)
val KwagaeTextSecondary  = Color(0xFFB0B8C1)        // softer but readable
val KwagaeTextHint       = Color(0xFF7A8594)        // muted but visible

// ⚠️ STATES
val KwagaeError          = Color(0xFFFF6B6B)        // brighter error
val KwagaeSuccess        = Color(0xFF22C55E)        // more vibrant green

// ☀️ LIGHT MODE (updated with black text)
val KwagaeLightBg        = Color(0xFFF5F7FA)        // soft light background
val KwagaeLightSurface   = Color(0xFFFFFFFF)        // pure white surface
val KwagaeLightText      = Color(0xFF000000)        // BLACK text for better contrast
val KwagaeLightTextSecondary = Color(0xFF333333)    // Dark gray for secondary text
val KwagaeLightTextHint  = Color(0xFF666666)        // Medium gray for hints