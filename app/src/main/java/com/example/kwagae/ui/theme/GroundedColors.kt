package com.example.kwagae.ui.theme

import androidx.compose.ui.graphics.Color

object GroundedColors {
    val EspressoDeep    = Color(0xFF3B2416)   // darkest brown – text, logo bg
    val BarkMid         = Color(0xFF5C4033)   // medium bark – button start
    val ClayWarm        = Color(0xFF8B6A40)   // warm clay – button end, borders
    val SandLight       = Color(0xFFA07848)   // light sand – button accent
    val CreamCard       = Color(0xFFFCF7EE)   // card background
    val CreamField      = Color(0xFFFBF6EE)   // input field background
    val CreamFocus      = Color(0xFFFDF9F3)   // focused input background
    val BorderDefault   = Color(0xFFD4B896)   // warm tan border
    val BorderFocus     = Color(0xFF8B6A40)   // clay border on focus
    val TextPrimary     = Color(0xFF3B2416)   // same as EspressoDeep
    val TextSecondary   = Color(0xFF8B6A50)   // muted brown
    val TextHint        = Color(0xFFC4A882)   // light hint / placeholder
    val TextMuted       = Color(0xFFB09070)   // divider label, badge text
    val AccentMoss      = Color(0xFF8BA87A)   // moss green (top stripe start)
    val AccentClay      = Color(0xFFC4874A)   // stripe mid
    val AccentBark      = Color(0xFF8B6A40)   // stripe end

    // Background gradient stops (top → bottom, dark soil to warm earth)
    val BgTop    = Color(0xFF3B2F1E)
    val BgMid1   = Color(0xFF5C4033)
    val BgMid2   = Color(0xFF7A5C40)
    val BgBottom = Color(0xFFA0784E)

    // Leaf overlay
    val LeafOverlay = Color(0xFF8BA87A).copy(alpha = 0.07f)
}
