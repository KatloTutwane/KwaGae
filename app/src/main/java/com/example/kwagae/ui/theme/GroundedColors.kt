package com.example.kwagae.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object GroundedColors {

    // ── Base colours ──────────────────────────────────────────────────────────
    val EspressoDeep  = Color(0xFF3B2416)   // darkest brown – text, logo bg
    val BarkMid       = Color(0xFF5C4033)   // medium bark – button start
    val ClayWarm      = Color(0xFF8B6A40)   // warm clay – button end, borders
    val SandLight     = Color(0xFFA07848)   // light sand – button accent
    val CreamCard     = Color(0xFFFCF7EE)   // card background
    val CreamField    = Color(0xFFFBF6EE)   // input field background
    val CreamFocus    = Color(0xFFFDF9F3)   // focused input background
    val BorderDefault = Color(0xFFD4B896)   // warm tan border
    val BorderFocus   = Color(0xFF8B6A40)   // clay border on focus
    val TextPrimary   = Color(0xFF3B2416)   // same as EspressoDeep
    val TextSecondary = Color(0xFF8B6A50)   // muted brown
    val TextHint      = Color(0xFFC4A882)   // light hint / placeholder
    val TextMuted     = Color(0xFFB09070)   // divider label, badge text
    val AccentMoss    = Color(0xFF8BA87A)   // moss green (top stripe start)
    val AccentClay    = Color(0xFFC4874A)   // stripe mid
    val AccentBark    = Color(0xFF8B6A40)   // stripe end

    // Background gradient stops (top → bottom, dark soil to warm earth)
    val BgTop    = Color(0xFF3B2F1E)
    val BgMid1   = Color(0xFF5C4033)
    val BgMid2   = Color(0xFF7A5C40)
    val BgBottom = Color(0xFFA0784E)

    // Leaf overlay
    val LeafOverlay = Color(0xFF8BA87A).copy(alpha = 0.07f)

    // ── Ready-made gradient brushes — use these in every screen ──────────────
    //    No more copy-pasting Brush.verticalGradient(...) per file!

    /** Dark-soil → warm-earth page background (use as Modifier.background(…)) */
    val backgroundGradient: Brush
        get() = Brush.verticalGradient(listOf(BgTop, BgMid1, BgMid2, BgBottom))

    /** Moss → clay → bark stripe used at the top of cards and screens */
    val topStripeGradient: Brush
        get() = Brush.horizontalGradient(listOf(AccentMoss, AccentClay, AccentBark))

    /** Bark → clay → sand button fill */
    val buttonGradient: Brush
        get() = Brush.horizontalGradient(listOf(BarkMid, ClayWarm, SandLight))

    /** Bark → clay logo circle fill */
    val logoGradient: Brush
        get() = Brush.linearGradient(listOf(BarkMid, ClayWarm))

    /** Dimmed button gradient shown while a loading action is in progress */
    val buttonGradientLoading: Brush
        get() = Brush.horizontalGradient(
            listOf(BarkMid.copy(alpha = 0.6f), ClayWarm.copy(alpha = 0.6f))
        )
}
