package com.example.kwagae

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kwagae.data.database.AppDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kwagae",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Student Housing Finder",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("e.g., student@example.com") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    scope.launch {
                        val database = AppDatabase.getDatabase(context)
                        val user = database.userDao().login(email, password)
                        if (user != null) {
                            // Save session
                            val prefs = context.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putLong("user_id", user.userId).apply()
                            prefs.edit().putString("student_id", user.studentId).apply()
                            
                            Toast.makeText(context, "Welcome ${user.fullName}", Toast.LENGTH_SHORT).show()
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please enter credentials", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Login")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Don't have an account? Register")
        }
    }
}