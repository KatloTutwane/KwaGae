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

// ── GRADIENTS ─────────────────────────────────────────
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

// ── SCREEN ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("student") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmError by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard),
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        "Create Account",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GroundedColors.TextPrimary
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

                    Row {
                        listOf("student", "provider").forEach {
                            Button(
                                onClick = { selectedRole = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(it.uppercase())
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(buttonGradient)
                            .clickable(enabled = !isLoading) {

                                var valid = true

                                if (fullName.isBlank()) {
                                    nameError = "Enter name"
                                    valid = false
                                }

                                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    emailError = "Invalid email"
                                    valid = false
                                }

                                if (password.length < 6) {
                                    passwordError = "Min 6 characters"
                                    valid = false
                                }

                                if (password != confirmPassword) {
                                    confirmError = "Passwords mismatch"
                                    valid = false
                                }

                                if (valid) {
                                    isLoading = true

                                    scope.launch {
                                        val db = AppDatabase.getDatabase(context)

                                        val exists = db.userDao().getUserByEmail(email)
                                        if (exists != null) {
                                            emailError = "Email exists"
                                            isLoading = false
                                            return@launch
                                        }

                                        val count = db.userDao().getStudentCount()
                                        val id = "${if (selectedRole=="student") "KW" else "PR"}${count+1}"

                                        val user = User(
                                            studentId = id,
                                            fullName = fullName,
                                            email = email,
                                            password = password,
                                            role = selectedRole
                                        )

                                        db.userDao().insertUser(user)

                                        Toast.makeText(context,"Account created",Toast.LENGTH_SHORT).show()

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
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("REGISTER", color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    TextButton(onClick = { navController.navigate("login") }) {
                        Text("Already have an account? Login")
                    }
                }
            }
        }
    }
}

// ── COMPONENTS ────────────────────────────────────────
@Composable
fun Field(label: String, value: String, onChange: (String)->Unit, icon: ImageVector) {
    OutlinedTextField(
        value, onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon,null) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onChange: (String)->Unit,
    visible: Boolean,
    toggle: ()->Unit
){
    OutlinedTextField(
        value, onChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock,null) },
        trailingIcon = {
            IconButton(onClick = toggle) {
                Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,null)
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ErrorText(msg: String){
    if(msg.isNotEmpty()){
        Text(msg, color = Color.Red, fontSize = 12.sp)
    }
}