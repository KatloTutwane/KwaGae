package com.example.kwagae

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kwagae.data.database.AppDatabase
import kotlinx.coroutines.launch
import java.security.MessageDigest

// ── Password hashing helper ──────────────────────────────────────────────────
private fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

// ── Grounded Earth Palette ────────────────────────────────────────────────────
private object GroundedColors {
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

// ── Brush helpers ─────────────────────────────────────────────────────────────
private val backgroundGradient = Brush.verticalGradient(
    colors = listOf(
        GroundedColors.BgTop,
        GroundedColors.BgMid1,
        GroundedColors.BgMid2,
        GroundedColors.BgBottom
    )
)

private val buttonGradient = Brush.horizontalGradient(
    colors = listOf(
        GroundedColors.BarkMid,
        GroundedColors.ClayWarm,
        GroundedColors.SandLight
    )
)

private val topStripeGradient = Brush.horizontalGradient(
    colors = listOf(
        GroundedColors.AccentMoss,
        GroundedColors.AccentClay,
        GroundedColors.AccentBark
    )
)

private val logoGradient = Brush.linearGradient(
    colors = listOf(GroundedColors.BarkMid, GroundedColors.ClayWarm)
)

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // ── Decorative leaf shapes ────────────────────────────────────────────
        LeafShape(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-80).dp)
        )
        LeafShape(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 220.dp, y = 540.dp)
        )

        // ── Scrollable content ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GroundedCard {
                // Top colour stripe
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .background(topStripeGradient)
                )

                Spacer(Modifier.height(24.dp))

                // Logo circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(logoGradient)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "K",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF5E8CC)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // App name
                Text(
                    text = "Kwagae",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GroundedColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Subtitle
                Text(
                    text = "STUDENT HOUSING FINDER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = GroundedColors.TextSecondary,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 20.dp)
                )

                // "SIGN IN" divider
                EarthDivider(label = "SIGN IN")

                Spacer(Modifier.height(16.dp))

                // Email field
                GroundedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "EMAIL ADDRESS",
                    placeholder = "student@example.com",
                    leadingIcon = Icons.Default.Email,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(12.dp))

                // Password field
                GroundedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "PASSWORD",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Default.Lock,
                    enabled = !isLoading,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible }
                )

                // Forgot password
                TextButton(
                    onClick = { navController.navigate("forgotPassword") },
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Forgot your password?",
                        fontSize = 12.sp,
                        color = GroundedColors.ClayWarm,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Login button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isLoading) Brush.horizontalGradient(
                                listOf(GroundedColors.BarkMid.copy(alpha = 0.6f),
                                    GroundedColors.ClayWarm.copy(alpha = 0.6f))
                            ) else buttonGradient
                        )
                        .clickable(enabled = !isLoading) {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                isLoading = true
                                scope.launch {
                                    val db   = AppDatabase.getDatabase(context)
                                    val user = db.userDao().login(email, hashPassword(password))
                                    if (user != null) {
                                        val prefs = context.getSharedPreferences(
                                            "kwagae_prefs", Context.MODE_PRIVATE
                                        )
                                        prefs.edit()
                                            .putLong("user_id", user.userId)
                                            .putString("student_id", user.studentId)
                                            .apply()
                                        Toast.makeText(
                                            context,
                                            "Welcome ${user.fullName}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isLoading = false
                                        navController.navigate("main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Invalid credentials",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please fill all fields",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFF5E8CC),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "LOGGING IN...",
                                color = Color(0xFFF5E8CC),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 2.sp
                            )
                        }
                    } else {
                        Text(
                            text = "LOGIN",
                            color = Color(0xFFF5E8CC),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Register link
                TextButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No account yet?  ",
                        fontSize = 13.sp,
                        color = GroundedColors.TextSecondary
                    )
                    Text(
                        text = "Register here",
                        fontSize = 13.sp,
                        color = GroundedColors.EspressoDeep,
                        fontWeight = FontWeight.Medium
                    )
                }

                // OR divider
                EarthDivider(label = "OR")

                Spacer(Modifier.height(12.dp))

                // Social buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GroundedSocialButton(
                        text = "Google",
                        icon = Icons.Default.Email, // swap with your Google SVG asset
                        onClick = {
                            Toast.makeText(context, "Google Login", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GroundedSocialButton(
                        text = "Apple",
                        icon = Icons.Default.Lock,  // swap with your Apple SVG asset
                        onClick = {
                            Toast.makeText(context, "Apple Login", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Trust badge
                TrustBadge()

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

/** Warm cream card with soft shadow */
@Composable
private fun GroundedCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF1E1208).copy(alpha = 0.5f),
                spotColor   = Color(0xFF1E1208).copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
        border = BorderStroke(1.dp, GroundedColors.BorderDefault.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

/** Leaf-shaped decorative background blob */
@Composable
private fun LeafShape(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStartPercent = 50, bottomEndPercent = 50))
            .background(GroundedColors.LeafOverlay)
    )
}

/** Horizontal divider with centred text label */
@Composable
private fun EarthDivider(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = GroundedColors.BorderDefault
        )
        Text(
            text = "  $label  ",
            fontSize = 10.sp,
            color = GroundedColors.TextMuted,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Normal
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = GroundedColors.BorderDefault
        )
    }
}

/** Styled text field matching the grounded cream/clay design */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = GroundedColors.ClayWarm,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = GroundedColors.TextHint,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = GroundedColors.TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (isPassword) ({
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint = GroundedColors.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }) else null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            enabled = enabled,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                color = GroundedColors.TextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor   = GroundedColors.CreamField,
                focusedContainerColor     = GroundedColors.CreamFocus,
                disabledContainerColor    = GroundedColors.CreamField,
                unfocusedBorderColor      = GroundedColors.BorderDefault,
                focusedBorderColor        = GroundedColors.BorderFocus,
                cursorColor               = GroundedColors.ClayWarm,
                unfocusedLeadingIconColor = GroundedColors.TextMuted,
                focusedLeadingIconColor   = GroundedColors.ClayWarm,
                // Fix white text input - make it dark brown
                focusedTextColor          = GroundedColors.TextPrimary,
                unfocusedTextColor        = GroundedColors.TextPrimary,
                disabledTextColor         = GroundedColors.TextSecondary
            )
        )
    }
}

/** Outlined social login button in warm cream style */
@Composable
private fun GroundedSocialButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.5.dp, GroundedColors.BorderDefault),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = GroundedColors.CreamField,
            contentColor   = GroundedColors.EspressoDeep
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = GroundedColors.BarkMid
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = GroundedColors.EspressoDeep
        )
    }
}

/** Small "Secure · Student Verified" trust badge row */
@Composable
private fun TrustBadge() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrustDot()
        Text(
            text = "  Secure  ",
            fontSize = 10.sp,
            color = GroundedColors.TextMuted,
            letterSpacing = 0.5.sp
        )
        TrustDot()
        Text(
            text = "  Student Verified  ",
            fontSize = 10.sp,
            color = GroundedColors.TextMuted,
            letterSpacing = 0.5.sp
        )
        TrustDot()
    }
}

@Composable
private fun TrustDot() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(GroundedColors.ClayWarm)
    )
}