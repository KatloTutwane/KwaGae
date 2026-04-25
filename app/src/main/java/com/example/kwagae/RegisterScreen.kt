package com.example.kwagae

import android.util.Patterns
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
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

    // ── Anime magic states ──
    var floatingOffset by remember { mutableStateOf(0f) }
    var glowAlpha by remember { mutableStateOf(0.3f) }
    var particlePositions by remember { mutableStateOf(List(8) { Pair(0f, 0f) }) }
    var showCharacter by remember { mutableStateOf(false) }
    var bounceScale by remember { mutableStateOf(1f) }

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

    // Anime floating animation
    val infiniteTransition = rememberInfiniteTransition()
    val floatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val floatX by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Glow pulse animation
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            glowAlpha = if (glowAlpha == 0.3f) 0.6f else 0.3f
            bounceScale = 1.05f
            delay(500)
            bounceScale = 1f
        }
    }

    // Particle generation
    LaunchedEffect(Unit) {
        particlePositions = List(8) { index ->
            val angle = (index * 45f) * Math.PI.toFloat() / 180f
            Pair(cos(angle) * 120f, sin(angle) * 120f)
        }
        delay(500)
        showCharacter = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    ),
                    radius = 800f,
                    center = Offset(200f, 200f)
                )
            )
    ) {
        // ── Anime floating particles (FIXED VERSION) ──
        particlePositions.forEachIndexed { index, (x, y) ->
            AnimatedVisibility(
                visible = showCharacter,
                enter = fadeIn() + scaleIn() + slideIn(
                    initialOffset = { fullSize ->
                        IntOffset((x + 50).toInt(), (y - 200).toInt())
                    }
                ),
                modifier = Modifier.offset(x = (x + floatX).dp, y = (y + floatY).dp)
            ) {
                Text(
                    "✨",
                    fontSize = 24.sp,
                    modifier = Modifier.alpha(glowAlpha)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Anime Character Header ──
            AnimatedVisibility(
                visible = showCharacter,
                enter = slideInVertically(initialOffsetY = { -200 }) + fadeIn() + scaleIn(),
                modifier = Modifier.offset(y = floatY.dp, x = floatX.dp)
            ) {
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(scaleX = bounceScale, scaleY = bounceScale),
                    shape = RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            when (selectedRole) {
                                "student" -> "🎓"
                                "provider" -> "🏠"
                                else -> "✨"
                            },
                            fontSize = 40.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Magical Title ──
            AnimatedContent(
                targetState = selectedRole,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith
                            fadeOut() + slideOutHorizontally()
                }
            ) { role ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (role == "student") "🌟 Begin Your Journey" else "🏯 Share Your Space",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (role == "student") "Find your dream home in Botswana"
                        else "Welcome hosts with magical spaces",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                    )
                }
            }

            // ── Role Selector (Anime Slider Style) ──
            Text(
                text = "⚡ Choose your path ⚡",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    Triple("student",  "🎓", "Seeker"),
                    Triple("provider", "🏠", "Host")
                ).forEach { (role, emoji, label) ->
                    val isSelected = selectedRole == role

                    Card(
                        onClick = {
                            selectedRole = role
                            showCharacter = false
                            scope.launch {
                                delay(200)
                                showCharacter = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer(
                                scaleX = if (isSelected) 1.05f else 1f,
                                scaleY = if (isSelected) 1.05f else 1f
                            ),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isSelected) 8.dp else 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(emoji, style = MaterialTheme.typography.displaySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Styled Input Fields with Anime Flair ──
            AnimatedTextField(
                value = fullName,
                onValueChange = { fullName = it; nameError = "" },
                label = "✨ Your Magical Name",
                icon = Icons.Default.Person,
                error = nameError,
                isError = nameError.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedTextField(
                value = email,
                onValueChange = { email = it; emailError = "" },
                label = "📧 Spirit Email",
                icon = Icons.Default.Email,
                error = emailError,
                isError = emailError.isNotEmpty(),
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedTextField(
                value = password,
                onValueChange = { password = it; passwordError = "" },
                label = "🔒 Secret Spell",
                icon = Icons.Default.Lock,
                error = passwordError,
                isError = passwordError.isNotEmpty(),
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                supportingText = if (passwordError.isEmpty()) "6+ characters (make it magical)" else null
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmError = "" },
                label = "🔐 Confirm Your Spell",
                icon = Icons.Default.Lock,
                error = confirmError,
                isError = confirmError.isNotEmpty(),
                isPassword = true,
                passwordVisible = confirmVisible,
                onPasswordToggle = { confirmVisible = !confirmVisible }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Success / Error Messages with Anime Flair ──
            if (successMessage.isNotEmpty()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✨", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                successMessage,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (generalError.isNotEmpty()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
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
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Magical Register Button ──
            Button(
                onClick = {
                    var valid = true
                    if (fullName.isBlank()) {
                        nameError = "✨ Your magical name is required ✨"
                        valid = false
                    }
                    if (email.isBlank()) {
                        emailError = "📧 We need your spirit email"
                        valid = false
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "That email doesn't look magical..."
                        valid = false
                    }
                    if (password.length < 6) {
                        passwordError = "Your spell needs 6+ characters"
                        valid = false
                    }
                    if (confirmPassword != password) {
                        confirmError = "Spells don't match! 🔮"
                        valid = false
                    }

                    if (valid) {
                        isLoading = true
                        scope.launch {
                            try {
                                val db = AppDatabase.getDatabase(context)

                                if (selectedRole == "student") {
                                    val count = db.userDao().getStudentCount()
                                    if (count >= 50) {
                                        generalError = "✨ The magical realm is full (50/50 seekers)"
                                        isLoading = false
                                        return@launch
                                    }
                                }

                                val existing = db.userDao().getUserByEmail(email.trim())
                                if (existing != null) {
                                    emailError = "This spirit is already registered! ✨"
                                    isLoading = false
                                    return@launch
                                }

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
                                    successMessage = "🎉 Welcome! Your magical ID: $studentId 🎉"
                                    delay(2000)
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                } else {
                                    generalError = "✨ Magic misfired. Please try again! ✨"
                                }

                            } catch (e: Exception) {
                                generalError = "✨ A wild error appeared: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .graphicsLayer(
                        shadowElevation = 8f,
                        spotShadowColor = MaterialTheme.colorScheme.primary
                    )
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Casting spell...", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Text("🌟 Begin Your Journey 🌟", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Login link with anime flair ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(y = floatY.dp * 0.5f)
            ) {
                Text(
                    "Already traveled here before?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { navController.navigate("login") },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("⚡ Return ⚡", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AnimatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String,
    isError: Boolean,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    supportingText: String? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                color = if (isFocused) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                icon, null,
                tint = if (isFocused) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (isPassword && onPasswordToggle != null) {
            {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (isFocused) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation()
        else VisualTransformation.None,
        isError = isError,
        supportingText = {
            when {
                error.isNotEmpty() -> Text(error, color = MaterialTheme.colorScheme.error)
                supportingText != null -> Text(supportingText)
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = androidx.compose.ui.text.input.ImeAction.Next
        ),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
    )
}