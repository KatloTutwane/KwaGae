package com.example.kwagae

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.User
import com.example.kwagae.ui.theme.GroundedColors
import kotlinx.coroutines.launch
import java.security.MessageDigest

// ── GRADIENTS ─────────────────────────────────────────────────────────────────
private val backgroundGradient = Brush.verticalGradient(
    listOf(
        Color(0xFF3B2F1E),
        Color(0xFF5C4033),
        Color(0xFF7A5C40),
        Color(0xFFA0784E)
    )
)

private val buttonGradient = Brush.horizontalGradient(
    listOf(
        GroundedColors.BarkMid,
        GroundedColors.ClayWarm,
        GroundedColors.SandLight
    )
)

// ── HELPER — hash password before storing ─────────────────────────────────────
private fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

// ── SCREEN ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {

    var fullName         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var selectedRole     by remember { mutableStateOf("student") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var confirmVisible   by remember { mutableStateOf(false) }

    var nameError        by remember { mutableStateOf("") }
    var emailError       by remember { mutableStateOf("") }
    var passwordError    by remember { mutableStateOf("") }
    var confirmError     by remember { mutableStateOf("") }

    var isLoading        by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        "Create Account",
                        fontSize    = 22.sp,
                        fontWeight  = FontWeight.Bold,
                        color       = GroundedColors.TextPrimary
                    )

                    Spacer(Modifier.height(16.dp))

                    Field("FULL NAME", fullName, { fullName = it; nameError = "" }, Icons.Default.Person)
                    ErrorText(nameError)

                    Field("EMAIL", email, { email = it; emailError = "" }, Icons.Default.Email)
                    ErrorText(emailError)

                    PasswordField(
                        "PASSWORD", password,
                        { password = it; passwordError = "" },
                        passwordVisible
                    ) { passwordVisible = !passwordVisible }
                    ErrorText(passwordError)

                    PasswordField(
                        "CONFIRM PASSWORD", confirmPassword,
                        { confirmPassword = it; confirmError = "" },
                        confirmVisible
                    ) { confirmVisible = !confirmVisible }
                    ErrorText(confirmError)

                    Spacer(Modifier.height(12.dp))

                    // ── Role selector ──────────────────────────────────────────
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("student", "provider").forEach { role ->
                            val isSelected = selectedRole == role
                            Button(
                                onClick = { selectedRole = role },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) GroundedColors.ClayWarm
                                    else GroundedColors.CreamField,
                                    contentColor   = if (isSelected) Color(0xFFF5E8CC)
                                    else GroundedColors.TextSecondary
                                )
                            ) {
                                Text(role.uppercase(), fontSize = 12.sp, letterSpacing = 1.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Register button ────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isLoading)
                                    Brush.horizontalGradient(
                                        listOf(
                                            GroundedColors.BarkMid.copy(alpha = 0.6f),
                                            GroundedColors.ClayWarm.copy(alpha = 0.6f)
                                        )
                                    )
                                else buttonGradient
                            )
                            .clickable(enabled = !isLoading) {

                                var valid = true

                                if (fullName.isBlank()) {
                                    nameError = "Enter your full name"
                                    valid = false
                                }
                                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    emailError = "Invalid email address"
                                    valid = false
                                }
                                if (password.length < 6) {
                                    passwordError = "Minimum 6 characters"
                                    valid = false
                                }
                                if (password != confirmPassword) {
                                    confirmError = "Passwords do not match"
                                    valid = false
                                }

                                if (valid) {
                                    isLoading = true
                                    scope.launch {
                                        val db = AppDatabase.getDatabase(context)

                                        // 1. Check email not already taken
                                        val exists = db.userDao().getUserByEmail(email)
                                        if (exists != null) {
                                            emailError = "Email already registered"
                                            isLoading  = false
                                            return@launch
                                        }

                                        // 2. Generate student/provider ID
                                        val count = db.userDao().getStudentCount(selectedRole)
                                        val prefix = if (selectedRole == "student") "KW" else "PR"
                                        val generatedId = "$prefix${count + 1}"

                                        // 3. Build User — store hashed password, never plain text
                                        val user = User(
                                            studentId    = generatedId,
                                            fullName     = fullName,
                                            email        = email,
                                            passwordHash = hashPassword(password), // ← fixed field name
                                            role         = selectedRole
                                        )

                                        db.userDao().insertUser(user)

                                        Toast.makeText(
                                            context,
                                            "Account created! Welcome $fullName",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        navController.navigate("login") {
                                            popUpTo("register") { inclusive = true }
                                        }

                                        isLoading = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color    = Color(0xFFF5E8CC),
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "REGISTER",
                                color       = Color(0xFFF5E8CC),
                                fontSize    = 13.sp,
                                fontWeight  = FontWeight.Medium,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    TextButton(
                        onClick  = { navController.navigate("login") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Already have an account? Login",
                            fontSize = 13.sp,
                            color    = GroundedColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ── REUSABLE COMPONENTS ───────────────────────────────────────────────────────

@Composable
fun Field(
    label    : String,
    value    : String,
    onChange : (String) -> Unit,
    icon     : ImageVector
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(label, fontSize = 11.sp, letterSpacing = 1.sp) },
        leadingIcon   = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(10.dp),
        singleLine    = true,
        colors        = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = GroundedColors.BorderDefault,
            focusedBorderColor   = GroundedColors.BorderFocus,
            cursorColor          = GroundedColors.ClayWarm,
            focusedTextColor     = GroundedColors.TextPrimary,
            unfocusedTextColor   = GroundedColors.TextPrimary
        )
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
fun PasswordField(
    label    : String,
    value    : String,
    onChange : (String) -> Unit,
    visible  : Boolean,
    toggle   : () -> Unit
) {
    OutlinedTextField(
        value               = value,
        onValueChange       = onChange,
        label               = { Text(label, fontSize = 11.sp, letterSpacing = 1.sp) },
        leadingIcon         = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp)) },
        trailingIcon        = {
            IconButton(onClick = toggle) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle password"
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None
        else PasswordVisualTransformation(),
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        singleLine = true,
        colors    = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = GroundedColors.BorderDefault,
            focusedBorderColor   = GroundedColors.BorderFocus,
            cursorColor          = GroundedColors.ClayWarm,
            focusedTextColor     = GroundedColors.TextPrimary,
            unfocusedTextColor   = GroundedColors.TextPrimary
        )
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
fun ErrorText(msg: String) {
    if (msg.isNotEmpty()) {
        Text(
            text     = msg,
            color    = Color(0xFFA32D2D),
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
    }
}