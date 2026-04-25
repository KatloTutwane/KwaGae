package com.example.kwagae

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kwagae.data.database.AppDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E), // Dark blue
                        Color(0xFF0D47A1), // Medium blue
                        Color(0xFF42A5F5)  // Light blue
                    ),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        // Decorative circles / shapes
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(
                    Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(100.dp)
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 200.dp, y = 500.dp)
                .background(
                    Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(150.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card with form content
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Icon / Illustration
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your own
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Kwagae",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFF1A237E)
                    )

                    Text(
                        text = "Student Housing Finder",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("student@example.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field with visibility toggle
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Forgot Password
                    TextButton(
                        onClick = { navController.navigate("forgotPassword") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Forgot Password?")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login Button with loading state
                    Button(
                        onClick = {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                isLoading = true
                                scope.launch {
                                    val database = AppDatabase.getDatabase(context)
                                    val user = database.userDao().login(email, password)
                                    if (user != null) {
                                        val prefs = context.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)
                                        prefs.edit().putLong("user_id", user.userId).apply()
                                        prefs.edit().putString("student_id", user.studentId).apply()

                                        Toast.makeText(context, "Welcome ${user.fullName}", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                        navController.navigate("main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Please enter credentials", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isLoading) "Logging in..." else "Login")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Register Link
                    TextButton(
                        onClick = { navController.navigate("register") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Don't have an account? Register")
                    }

                    // Social Login Divider
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f))
                        Text(
                            text = " OR ",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Divider(modifier = Modifier.weight(1f))
                    }

                    // Social Login Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SocialLoginButton(
                            icon = Icons.Default.AccountCircle,
                            text = "Google",
                            onClick = { Toast.makeText(context, "Google Login", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f)
                        )
                        SocialLoginButton(
                            icon = Icons.Default.Favorite, // Placeholder for Apple icon (Material Icons don't include brand logos)
                            text = "Apple",
                            onClick = { Toast.makeText(context, "Apple Login", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SocialLoginButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}
