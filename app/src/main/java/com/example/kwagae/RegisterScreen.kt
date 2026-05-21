package com.example.kwagae

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kwagae.data.StudentVerifier
import com.example.kwagae.data.VerificationResult
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.User
import com.example.kwagae.ui.components.*
import com.example.kwagae.ui.theme.GroundedColors
import kotlinx.coroutines.launch
import java.security.MessageDigest

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun hashPw(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

/** The five possible states for the student-number verification widget */
private enum class VerifyState {
    IDLE,
    CHECKING,
    VERIFIED,
    FAILED_NOT_FOUND,
    FAILED_FORMAT,
    FAILED_UNKNOWN_UNI   // prefix not recognised — not a Gaborone university
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun RegisterScreen(navController: NavController) {

    var fullName        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole    by remember { mutableStateOf("student") }
    var ubNumber        by remember { mutableStateOf("") }  // UB student number

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    // Validation error strings
    var nameError    by remember { mutableStateOf("") }
    var emailError   by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmError  by remember { mutableStateOf("") }
    var ubNumberError by remember { mutableStateOf("") }

    // Student verification state machine
    var verifyState         by remember { mutableStateOf(VerifyState.IDLE) }
    // University detected live from the prefix (no network call)
    var detectedUniversity  by remember { mutableStateOf<StudentVerifier.University?>(null) }
    // University confirmed after a successful registry check
    var verifiedUniversity  by remember { mutableStateOf<StudentVerifier.University?>(null) }

    var isRegistering by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // Reset verification and update live university detection on every keystroke
    LaunchedEffect(ubNumber) {
        if (verifyState != VerifyState.IDLE) {
            verifyState        = VerifyState.IDLE
            verifiedUniversity = null
        }
        ubNumberError      = ""
        detectedUniversity = StudentVerifier.detectUniversity(ubNumber)
    }

    // Reset everything if user switches role
    LaunchedEffect(selectedRole) {
        verifyState        = VerifyState.IDLE
        ubNumberError      = ""
        ubNumber           = ""
        detectedUniversity = null
        verifiedUniversity = null
    }

    // Registration may only proceed when:
    //  • role = provider  (no verification needed), OR
    //  • role = student AND verifyState = VERIFIED
    val canRegister = selectedRole == "provider" || verifyState == VerifyState.VERIFIED

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GroundedCard {
                Spacer(Modifier.height(20.dp))

                Text(
                    "Create Account",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GroundedColors.TextPrimary
                )
                Text(
                    "Join Kwagae — Gaborone Student Housing",
                    fontSize = 12.sp,
                    color    = GroundedColors.TextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                // ── Basic fields ───────────────────────────────────────────────
                GroundedField("FULL NAME", fullName, { fullName = it; nameError = "" }, Icons.Default.Person)
                FieldErrorText(nameError)

                GroundedField("EMAIL ADDRESS", email, { email = it; emailError = "" }, Icons.Default.Email)
                FieldErrorText(emailError)

                GroundedPasswordField(
                    label    = "PASSWORD",
                    value    = password,
                    onChange = { password = it; passwordError = "" },
                    visible  = passwordVisible,
                    toggle   = { passwordVisible = !passwordVisible }
                )
                FieldErrorText(passwordError)

                GroundedPasswordField(
                    label    = "CONFIRM PASSWORD",
                    value    = confirmPassword,
                    onChange = { confirmPassword = it; confirmError = "" },
                    visible  = confirmVisible,
                    toggle   = { confirmVisible = !confirmVisible }
                )
                FieldErrorText(confirmError)

                Spacer(Modifier.height(12.dp))

                // ── Role selector ──────────────────────────────────────────────
                Text(
                    text          = "I AM A",
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Medium,
                    color         = GroundedColors.ClayWarm,
                    letterSpacing = 1.5.sp,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("student", "provider").forEach { role ->
                        val isSelected = selectedRole == role
                        Button(
                            onClick  = { selectedRole = role },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) GroundedColors.ClayWarm
                                                else GroundedColors.CreamField,
                                contentColor   = if (isSelected) Color(0xFFF5E8CC)
                                                else GroundedColors.TextSecondary
                            )
                        ) {
                            if (role == "student") {
                                Icon(
                                    Icons.Default.School, null,
                                    modifier = Modifier.size(14.dp),
                                    tint     = if (isSelected) Color(0xFFF5E8CC) else GroundedColors.TextMuted
                                )
                                Spacer(Modifier.width(4.dp))
                            } else {
                                Icon(
                                    Icons.Default.Business, null,
                                    modifier = Modifier.size(14.dp),
                                    tint     = if (isSelected) Color(0xFFF5E8CC) else GroundedColors.TextMuted
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(role.uppercase(), fontSize = 12.sp, letterSpacing = 1.sp)
                        }
                    }
                }

                // ── Student verification panel (animated, students only) ────────
                AnimatedVisibility(
                    visible = selectedRole == "student",
                    enter   = expandVertically() + fadeIn(),
                    exit    = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.height(16.dp))
                        StudentVerificationPanel(
                            ubNumber           = ubNumber,
                            onUbChange         = { ubNumber = it },
                            verifyState        = verifyState,
                            ubNumberError      = ubNumberError,
                            detectedUniversity = detectedUniversity,
                            verifiedUniversity = verifiedUniversity,
                            onVerifyClick      = {
                                ubNumberError = ""
                                verifyState   = VerifyState.CHECKING
                                scope.launch {
                                    when (val result = StudentVerifier.verify(ubNumber)) {
                                        is VerificationResult.Verified -> {
                                            verifiedUniversity = result.university
                                            verifyState        = VerifyState.VERIFIED
                                        }
                                        VerificationResult.NotFound -> {
                                            verifyState = VerifyState.FAILED_NOT_FOUND
                                        }
                                        is VerificationResult.InvalidFormat -> {
                                            verifyState = VerifyState.FAILED_FORMAT
                                        }
                                        VerificationResult.UnknownUniversity -> {
                                            verifyState = VerifyState.FAILED_UNKNOWN_UNI
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Register button ────────────────────────────────────────────
                GroundedPrimaryButton(
                    text        = if (canRegister) "REGISTER" else "VERIFY STUDENT FIRST",
                    isLoading   = isRegistering,
                    onClick     = {
                        // Validate common fields
                        var valid = true
                        if (fullName.isBlank())  { nameError = "Enter your full name";           valid = false }
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            emailError = "Invalid email address"; valid = false
                        }
                        if (password.length < 6) { passwordError = "Minimum 6 characters";      valid = false }
                        if (password != confirmPassword) { confirmError = "Passwords do not match"; valid = false }

                        // Student-only: must be verified
                        if (selectedRole == "student" && verifyState != VerifyState.VERIFIED) {
                            ubNumberError = "Please verify your UB student number first"
                            valid = false
                        }

                        if (valid) {
                            isRegistering = true
                            scope.launch {
                                val db     = AppDatabase.getDatabase(context)
                                val exists = db.userDao().getUserByEmail(email)
                                if (exists != null) {
                                    emailError     = "Email already registered"
                                    isRegistering  = false
                                    return@launch
                                }
                                val count       = db.userDao().getStudentCount(selectedRole)
                                val prefix      = if (selectedRole == "student") "KW" else "PR"
                                val generatedId = "$prefix${count + 1}"

                                val user = User(
                                    studentId       = generatedId,
                                    fullName        = fullName,
                                    email           = email,
                                    passwordHash    = hashPw(password),
                                    role            = selectedRole,
                                    ubStudentNumber = if (selectedRole == "student") ubNumber.trim().uppercase() else "",
                                    university      = verifiedUniversity?.name ?: "",
                                    isVerified      = selectedRole == "student"
                                )
                                db.userDao().insertUser(user)
                                Toast.makeText(context, "Account created! Welcome $fullName", Toast.LENGTH_SHORT).show()
                                navController.navigate("login") { popUpTo("register") { inclusive = true } }
                                isRegistering = false
                            }
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick  = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Already have an account? Login", fontSize = 13.sp, color = GroundedColors.TextSecondary)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Student Verification Panel ────────────────────────────────────────────────

@Composable
private fun StudentVerificationPanel(
    ubNumber: String,
    onUbChange: (String) -> Unit,
    verifyState: VerifyState,
    ubNumberError: String,
    detectedUniversity: StudentVerifier.University?,
    verifiedUniversity: StudentVerifier.University?,
    onVerifyClick: () -> Unit
) {
    val isError = verifyState in listOf(
        VerifyState.FAILED_NOT_FOUND,
        VerifyState.FAILED_FORMAT,
        VerifyState.FAILED_UNKNOWN_UNI
    )

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── Section label ──────────────────────────────────────────────────────
        Text(
            text          = "STUDENT VERIFICATION",
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Medium,
            color         = GroundedColors.ClayWarm,
            letterSpacing = 1.5.sp,
            modifier      = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text     = "Enter your student number. Supports UB, Botho, Limkokwing, BOU, ABM, BAISAGO, and Botswana Accountancy College.",
            fontSize = 11.sp,
            color    = GroundedColors.TextMuted,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // ── Live university detection chip ─────────────────────────────────────
        AnimatedVisibility(
            visible = detectedUniversity != null && verifyState == VerifyState.IDLE,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            if (detectedUniversity != null) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .background(
                            GroundedColors.ClayWarm.copy(alpha = 0.08f),
                            RoundedCornerShape(6.dp)
                        )
                        .border(1.dp, GroundedColors.BorderDefault, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.School, null,
                        modifier = Modifier.size(13.dp),
                        tint     = GroundedColors.ClayWarm
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text     = "Detected: ${detectedUniversity.name}",
                        fontSize = 11.sp,
                        color    = GroundedColors.ClayWarm,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ── Input row + verify button ──────────────────────────────────────────
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value         = ubNumber,
                onValueChange = onUbChange,
                placeholder   = {
                    Text(
                        "e.g. UB20210001 · BOTHO210001 · CSE24-099",
                        color    = GroundedColors.TextHint,
                        fontSize = 11.sp
                    )
                },
                leadingIcon   = {
                    Icon(
                        Icons.Default.Badge, null,
                        modifier = Modifier.size(18.dp),
                        tint     = when {
                            verifyState == VerifyState.VERIFIED -> Color(0xFF4CAF50)
                            isError                             -> Color(0xFFD32F2F)
                            else                                -> GroundedColors.TextMuted
                        }
                    )
                },
                trailingIcon  = when {
                    verifyState == VerifyState.VERIFIED -> ({
                        Icon(
                            Icons.Default.CheckCircle, null,
                            tint     = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    })
                    isError -> ({
                        Icon(
                            Icons.Default.Cancel, null,
                            tint     = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                    })
                    else -> null
                },
                enabled       = verifyState != VerifyState.CHECKING && verifyState != VerifyState.VERIFIED,
                singleLine    = true,
                modifier      = Modifier.weight(1f),
                shape         = RoundedCornerShape(10.dp),
                textStyle     = LocalTextStyle.current.copy(
                    fontSize = 13.sp,
                    color    = GroundedColors.TextPrimary
                ),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = GroundedColors.CreamField,
                    focusedContainerColor   = GroundedColors.CreamFocus,
                    unfocusedBorderColor    = when {
                        verifyState == VerifyState.VERIFIED -> Color(0xFF4CAF50)
                        isError                             -> Color(0xFFD32F2F)
                        else                                -> GroundedColors.BorderDefault
                    },
                    focusedBorderColor      = GroundedColors.BorderFocus,
                    cursorColor             = GroundedColors.ClayWarm,
                    focusedTextColor        = GroundedColors.TextPrimary,
                    unfocusedTextColor      = GroundedColors.TextPrimary,
                    disabledTextColor       = GroundedColors.TextPrimary,
                    disabledBorderColor     = Color(0xFF4CAF50),
                    disabledContainerColor  = Color(0xFF4CAF50).copy(alpha = 0.05f)
                )
            )

            // Verify button — hidden once verified
            if (verifyState != VerifyState.VERIFIED) {
                Button(
                    onClick        = onVerifyClick,
                    enabled        = ubNumber.isNotBlank() && verifyState != VerifyState.CHECKING,
                    shape          = RoundedCornerShape(10.dp),
                    colors         = ButtonDefaults.buttonColors(containerColor = GroundedColors.BarkMid),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    if (verifyState == VerifyState.CHECKING) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            color       = Color(0xFFF5E8CC),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text          = "VERIFY",
                            fontSize      = 12.sp,
                            fontWeight    = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color         = Color(0xFFF5E8CC)
                        )
                    }
                }
            }
        }

        // ── Animated status banner ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = verifyState != VerifyState.IDLE,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            val isSuccess = verifyState == VerifyState.VERIFIED

            val bgColor = when {
                verifyState == VerifyState.CHECKING -> GroundedColors.CreamField
                isSuccess                           -> Color(0xFF4CAF50).copy(alpha = 0.10f)
                else                                -> Color(0xFFD32F2F).copy(alpha = 0.08f)
            }
            val borderColor = when {
                verifyState == VerifyState.CHECKING -> GroundedColors.BorderDefault
                isSuccess                           -> Color(0xFF4CAF50).copy(alpha = 0.40f)
                else                                -> Color(0xFFD32F2F).copy(alpha = 0.30f)
            }
            val iconVec = when (verifyState) {
                VerifyState.CHECKING          -> Icons.Default.HourglassEmpty
                VerifyState.VERIFIED          -> Icons.Default.VerifiedUser
                VerifyState.FAILED_NOT_FOUND  -> Icons.Default.PersonOff
                VerifyState.FAILED_FORMAT     -> Icons.Default.ErrorOutline
                VerifyState.FAILED_UNKNOWN_UNI -> Icons.Default.HelpOutline
                else                          -> Icons.Default.Info
            }
            val msg = when (verifyState) {
                VerifyState.CHECKING ->
                    "Checking national student registry…"
                VerifyState.VERIFIED ->
                    "Verified ✓ — Enrolled at ${verifiedUniversity?.name ?: "your university"}"
                VerifyState.FAILED_NOT_FOUND ->
                    "Number not found in the registry. Check for typos and try again."
                VerifyState.FAILED_FORMAT ->
                    "Wrong format for ${detectedUniversity?.name ?: "that university"} — e.g. ${detectedUniversity?.example ?: "check your number"}"
                VerifyState.FAILED_UNKNOWN_UNI ->
                    "Unknown university prefix. Supported: UB, BOTHO, LU, BOU, ABM, BAI"
                else -> ""
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (verifyState == VerifyState.CHECKING) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        color       = GroundedColors.BarkMid,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        iconVec, null,
                        modifier = Modifier.size(16.dp),
                        tint     = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text     = msg,
                    fontSize = 11.sp,
                    color    = when {
                        verifyState == VerifyState.CHECKING -> GroundedColors.TextSecondary
                        isSuccess                           -> Color(0xFF2E7D32)
                        else                                -> Color(0xFFB71C1C)
                    }
                )
            }
        }

        // ── Validation error from submit attempt ───────────────────────────────
        FieldErrorText(ubNumberError)

        // ── Supported formats hint ─────────────────────────────────────────────
        if (verifyState == VerifyState.IDLE || isError) {
            Text(
                text     = "UB · BOTHO · LU · BOU · ABM · BAI · BAC (e.g. CSE24-099)",
                fontSize = 9.5.sp,
                color    = GroundedColors.TextHint,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text     = "BAC format: course + year + number  e.g. CSE24-099  ACC24-001  BAF24-005",
                fontSize = 9.sp,
                color    = GroundedColors.TextHint,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}
