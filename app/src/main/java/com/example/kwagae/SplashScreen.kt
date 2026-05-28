package com.example.kwagae

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

// Brand colours (mirrors the logo image)
private val SplashCream      = Color(0xFFF5E8CC)
private val SplashCreamDeep  = Color(0xFFEDD9A3)
private val SplashBrown      = Color(0xFF3D2108)
private val SplashBrownMid   = Color(0xFF7A5C3A)
private val SplashClay       = Color(0xFFC05A2A)
private val SplashGreen      = Color(0xFF4A7C3F)
private val SplashGreenLight = Color(0xFF5C9E51)

@Composable
fun SplashScreen(navController: NavController) {

    // Controls the fade-in of the whole content
    var contentVisible by remember { mutableStateOf(false) }
    // Controls the entrance of the tagline (slight delay after logo)
    var taglineVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
        delay(400)
        taglineVisible = true
        delay(2200)          // total visible for ~2.6 s
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SplashCream, SplashCreamDeep)
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Centre content ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = contentVisible,
            enter   = fadeIn(tween(700)) + scaleIn(initialScale = 0.82f, animationSpec = tween(700, easing = EaseOutBack))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {

                // ── Logo icon ─────────────────────────────────────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(20.dp, CircleShape,
                            ambientColor = SplashBrown.copy(alpha = 0.18f),
                            spotColor    = SplashBrown.copy(alpha = 0.12f))
                        .background(SplashCream, CircleShape)
                ) {
                    // Outer green ring
                    Box(
                        modifier = Modifier
                            .size(118.dp)
                            .background(Color.Transparent, CircleShape)
                            .clip(CircleShape)
                    )

                    // House icon
                    Icon(
                        imageVector        = Icons.Default.Home,
                        contentDescription = null,
                        modifier           = Modifier.size(78.dp),
                        tint               = SplashClay
                    )

                    // Location pin overlay (top-right of house)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 12.dp)
                            .size(34.dp)
                            .background(SplashGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier           = Modifier.size(20.dp),
                            tint               = SplashCream
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── App name ──────────────────────────────────────────────────
                Text(
                    text       = "KWAGAE",
                    fontSize   = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = SplashBrown,
                    letterSpacing = 5.sp,
                    textAlign  = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                // ── Divider line ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(SplashGreen, SplashClay)
                            )
                        )
                )

                Spacer(Modifier.height(10.dp))

                // ── Tagline ───────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = taglineVisible,
                    enter   = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
                ) {
                    Text(
                        text       = "Find Housing for Rent",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = SplashBrownMid,
                        letterSpacing = 1.sp,
                        textAlign  = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(56.dp))

                // ── Loading indicator ─────────────────────────────────────────
                CircularProgressIndicator(
                    modifier    = Modifier.size(22.dp),
                    color       = SplashClay,
                    strokeWidth = 2.dp
                )
            }
        }

        // ── Bottom badge ──────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = taglineVisible,
            enter   = fadeIn(tween(800)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text      = "Gaborone, Botswana",
                fontSize  = 11.sp,
                color     = SplashBrownMid.copy(alpha = 0.6f),
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
