package com.example.kwagae

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {

    // ── Form state ──
    var fullName        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole    by remember { mutableStateOf("student") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    // ── Error state ──
    var nameError     by remember { mutableStateOf("") }
    var emailError    by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmError  by remember { mutableStateOf("") }
    var generalError  by remember { mutableStateOf("") }

    // ── UI state ──
    var isLoading      by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ──
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Join KwagaeStays today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
            )

            // ── Role Selector ──
            Text(
                text = "I am a:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    Triple("student",  "🎓", "Find accommodation"),
                    Triple("provider", "🏠", "List your property")
                ).forEach { (role, emoji, subtitle) ->
                    val isSelected = selectedRole == role
                    Card(
                        onClick = { selectedRole = role },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected)
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        else
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(emoji, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = role.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Full Name ──
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; nameError = "" },
                label = { Text("Full Name") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person, null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                isError = nameError.isNotEmpty(),
                supportingText = {
                    if (nameError.isNotEmpty())
                        Text(nameError, color = MaterialTheme.colorScheme.error)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Email ──
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = "" },
                label = { Text("Email address") },
                placeholder = { Text("you@example.com") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email, null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                isError = emailError.isNotEmpty(),
                supportingText = {
                    if (emailError.isNotEmpty())
                        Text(emailError, color = MaterialTheme.colorScheme.error)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Password ──
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = "" },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock, null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide" else "Show"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                isError = passwordError.isNotEmpty(),
                supportingText = {
                    Text(
                        if (passwordError.isNotEmpty()) passwordError
                        else "Minimum 6 characters",
                        color = if (passwordError.isNotEmpty())
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Confirm Password ──
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmError = "" },
                label = { Text("Confirm Password") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock, null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            if (confirmVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (confirmVisible) "Hide" else "Show"
                        )
                    }
                },
                visualTransformation = if (confirmVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                isError = confirmError.isNotEmpty(),
                supportingText = {
                    if (confirmError.isNotEmpty())
                        Text(confirmError, color = MaterialTheme.colorScheme.error)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Success banner ──
            if (successMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            successMessage,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Error banner ──
            if (generalError.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            generalError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Register Button ──
            Button(
                onClick = {
                    // Reset errors
                    nameError = ""; emailError = ""
                    passwordError = ""; confirmError = ""
                    generalError = ""

                    // Validate
                    var valid = true
                    if (fullName.isBlank()) {
                        nameError = "Full name is required"; valid = false
                    }
                    if (email.isBlank()) {
                        emailError = "Email is required"; valid = false
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Enter a valid email address"; valid = false
                    }
                    if (password.length < 6) {
                        passwordError = "Password must be at least 6 characters"; valid = false
                    }
                    if (confirmPassword != password) {
                        confirmError = "Passwords do not match"; valid = false
                    }

                    if (valid) {
                        isLoading = true
                        scope.launch {
                            try {
                                val db = AppDatabase.getDatabase(context)

                                // Students only — cap at 50
                                if (selectedRole == "student") {
                                    val count = db.userDao().getStudentCount()
                                    if (count >= 50) {
                                        generalError = "Student registration is full (50/50)."
                                        isLoading = false
                                        return@launch
                                    }
                                }

                                // Duplicate email check
                                val existing = db.userDao().getUserByEmail(email.trim())
                                if (existing != null) {
                                    emailError = "This email is already registered"
                                    isLoading = false
                                    return@launch
                                }

                                // Generate ID
                                val count = db.userDao().getStudentCount()
                                val prefix = if (selectedRole == "student") "KW" else "PR"
                                val studentId = "$prefix${String.format("%03d", count + 1)}"

                                val newUser = User(
                                    studentId  = studentId,
                                    fullName   = fullName.trim(),
                                    email      = email.trim().lowercase(),
                                    password   = password,
                                    role       = selectedRole
                                )

                                val insertedId = db.userDao().insertUser(newUser)

                                if (insertedId > 0) {
                                    successMessage = "Account created! Your ID: $studentId"
                                    delay(1500)
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                } else {
                                    generalError = "Registration failed. Please try again."
                                }

                            } catch (e: Exception) {
                                generalError = "Error: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Create Account", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Login link ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { navController.navigate("login") }) {
                    Text("Login", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}