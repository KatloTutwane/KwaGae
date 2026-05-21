package com.example.kwagae

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.ui.components.*   // ← all shared widgets live here
import com.example.kwagae.ui.theme.GroundedColors
import kotlinx.coroutines.launch
import java.security.MessageDigest

private fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(navController: NavController) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // AppBackground = gradient + leaf shapes. No hardcoding needed.
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GroundedCard {
                Spacer(Modifier.height(24.dp))

                // App logo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(GroundedColors.logoGradient)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("K", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF5E8CC))
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text       = "Kwagae",
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = GroundedColors.TextPrimary,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
                Text(
                    text          = "STUDENT HOUSING FINDER",
                    fontSize      = 10.sp,
                    color         = GroundedColors.TextSecondary,
                    letterSpacing = 2.sp,
                    textAlign     = TextAlign.Center,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 20.dp)
                )

                EarthDivider(label = "SIGN IN")
                Spacer(Modifier.height(16.dp))

                GroundedTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = "EMAIL ADDRESS",
                    placeholder   = "student@example.com",
                    leadingIcon   = Icons.Default.Email,
                    enabled       = !isLoading
                )

                Spacer(Modifier.height(12.dp))

                GroundedTextField(
                    value            = password,
                    onValueChange    = { password = it },
                    label            = "PASSWORD",
                    placeholder      = "••••••••",
                    leadingIcon      = Icons.Default.Lock,
                    enabled          = !isLoading,
                    isPassword       = true,
                    passwordVisible  = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible }
                )

                TextButton(
                    onClick        = { /* TODO: forgot password */ },
                    modifier       = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text       = "Forgot your password?",
                        fontSize   = 12.sp,
                        color      = GroundedColors.ClayWarm,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(12.dp))

                GroundedPrimaryButton(
                    text        = "LOGIN",
                    loadingText = "LOGGING IN...",
                    isLoading   = isLoading,
                    onClick     = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            scope.launch {
                                val db   = AppDatabase.getDatabase(context)
                                val user = db.userDao().login(email, hashPassword(password))
                                if (user != null) {
                                    context.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)
                                        .edit()
                                        .putLong("user_id", user.userId)
                                        .putString("student_id", user.studentId)
                                        .putString("full_name", user.fullName)
                                        .putString("role", user.role)
                                        .apply()
                                    Toast.makeText(context, "Welcome ${user.fullName}", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                    navController.navigate("main") { popUpTo("login") { inclusive = true } }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(Modifier.height(14.dp))

                TextButton(
                    onClick  = { navController.navigate("register") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("No account yet?  ",  fontSize = 13.sp, color = GroundedColors.TextSecondary)
                    Text("Register here",       fontSize = 13.sp, color = GroundedColors.EspressoDeep, fontWeight = FontWeight.Medium)
                }

                EarthDivider(label = "OR")
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GroundedSocialButton(
                        text     = "Google",
                        icon     = Icons.Default.Email,
                        onClick  = { Toast.makeText(context, "Google Login", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f)
                    )
                    GroundedSocialButton(
                        text     = "Apple",
                        icon     = Icons.Default.Lock,
                        onClick  = { Toast.makeText(context, "Apple Login", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(20.dp))
                TrustBadge()
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
