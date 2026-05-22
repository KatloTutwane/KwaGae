package com.example.kwagae

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kwagae.ui.components.*
import com.example.kwagae.ui.theme.GroundedColors
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(navController: NavController) {
    val viewModel: LoginViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                account.idToken?.let { viewModel.signInWithGoogle(it) }
                    ?: Toast.makeText(context, "Google Sign-In failed: no token", Toast.LENGTH_SHORT).show()
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateTo.collect { route ->
            navController.navigate(route) { popUpTo("login") { inclusive = true } }
        }
    }

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
                    value         = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label         = "EMAIL ADDRESS",
                    placeholder   = "student@example.com",
                    leadingIcon   = Icons.Default.Email,
                    enabled       = !state.isLoading
                )

                Spacer(Modifier.height(12.dp))

                GroundedTextField(
                    value            = state.password,
                    onValueChange    = viewModel::onPasswordChange,
                    label            = "PASSWORD",
                    placeholder      = "••••••••",
                    leadingIcon      = Icons.Default.Lock,
                    enabled          = !state.isLoading,
                    isPassword       = true,
                    passwordVisible  = state.passwordVisible,
                    onTogglePassword = viewModel::togglePasswordVisible
                )

                if (state.errorMessage.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    FieldErrorText(state.errorMessage)
                }

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
                    isLoading   = state.isLoading,
                    onClick     = viewModel::login
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
                        onClick  = {
                            try {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(context.getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .build()
                                val client = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(client.signInIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Google Sign-In not configured", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GroundedSocialButton(
                        text     = "Apple",
                        icon     = Icons.Default.Lock,
                        onClick  = { Toast.makeText(context, "Apple Login coming soon", Toast.LENGTH_SHORT).show() },
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
