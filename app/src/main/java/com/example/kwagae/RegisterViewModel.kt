package com.example.kwagae

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwagae.data.StudentVerifier
import com.example.kwagae.data.VerificationResult
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

enum class VerifyState {
    IDLE, CHECKING, VERIFIED, FAILED_NOT_FOUND, FAILED_FORMAT, FAILED_UNKNOWN_UNI
}

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: String = "student",
    val ubNumber: String = "",
    val passwordVisible: Boolean = false,
    val confirmVisible: Boolean = false,
    val isRegistering: Boolean = false,
    val nameError: String = "",
    val emailError: String = "",
    val passwordError: String = "",
    val confirmError: String = "",
    val ubNumberError: String = "",
    val verifyState: VerifyState = VerifyState.IDLE,
    val detectedUniversity: StudentVerifier.University? = null,
    val verifiedUniversity: StudentVerifier.University? = null
) {
    val canRegister: Boolean get() = selectedRole == "provider" || verifyState == VerifyState.VERIFIED
}

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _navigateTo = MutableSharedFlow<String>()
    val navigateTo: SharedFlow<String> = _navigateTo.asSharedFlow()

    init {
        // Detect university live as the student number changes
        viewModelScope.launch {
            _uiState.map { it.ubNumber }.distinctUntilChanged().collect { num ->
                _uiState.update {
                    it.copy(
                        verifyState        = VerifyState.IDLE,
                        verifiedUniversity = null,
                        ubNumberError      = "",
                        detectedUniversity = StudentVerifier.detectUniversity(num)
                    )
                }
            }
        }
        // Reset verification when role changes
        viewModelScope.launch {
            _uiState.map { it.selectedRole }.distinctUntilChanged().collect {
                _uiState.update {
                    it.copy(
                        verifyState        = VerifyState.IDLE,
                        ubNumber           = "",
                        ubNumberError      = "",
                        detectedUniversity = null,
                        verifiedUniversity = null
                    )
                }
            }
        }
    }

    fun onFullNameChange(v: String)        { _uiState.update { it.copy(fullName = v, nameError = "") } }
    fun onEmailChange(v: String)           { _uiState.update { it.copy(email = v, emailError = "") } }
    fun onPasswordChange(v: String)        { _uiState.update { it.copy(password = v, passwordError = "") } }
    fun onConfirmPasswordChange(v: String) { _uiState.update { it.copy(confirmPassword = v, confirmError = "") } }
    fun onRoleChange(role: String)         { _uiState.update { it.copy(selectedRole = role) } }
    fun onUbNumberChange(v: String)        { _uiState.update { it.copy(ubNumber = v) } }
    fun togglePasswordVisible()            { _uiState.update { it.copy(passwordVisible = !it.passwordVisible) } }
    fun toggleConfirmVisible()             { _uiState.update { it.copy(confirmVisible = !it.confirmVisible) } }

    fun verify() {
        _uiState.update { it.copy(ubNumberError = "", verifyState = VerifyState.CHECKING) }
        viewModelScope.launch {
            val result = StudentVerifier.verify(_uiState.value.ubNumber)
            _uiState.update {
                when (result) {
                    is VerificationResult.Verified ->
                        it.copy(verifiedUniversity = result.university, verifyState = VerifyState.VERIFIED)
                    VerificationResult.NotFound ->
                        it.copy(verifyState = VerifyState.FAILED_NOT_FOUND)
                    is VerificationResult.InvalidFormat ->
                        it.copy(verifyState = VerifyState.FAILED_FORMAT)
                    VerificationResult.UnknownUniversity ->
                        it.copy(verifyState = VerifyState.FAILED_UNKNOWN_UNI)
                }
            }
        }
    }

    fun register() {
        val s = _uiState.value
        var valid = true

        if (s.fullName.isBlank()) {
            _uiState.update { it.copy(nameError = "Enter your full name") }
            valid = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email address") }
            valid = false
        }
        if (s.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Minimum 6 characters") }
            valid = false
        }
        if (s.password != s.confirmPassword) {
            _uiState.update { it.copy(confirmError = "Passwords do not match") }
            valid = false
        }
        if (s.selectedRole == "student" && s.verifyState != VerifyState.VERIFIED) {
            _uiState.update { it.copy(ubNumberError = "Please verify your student number first") }
            valid = false
        }
        if (!valid) return

        _uiState.update { it.copy(isRegistering = true) }
        viewModelScope.launch {
            // Check duplicate email locally first (covers seeded accounts too)
            val exists = db.userDao().getUserByEmail(s.email)
            if (exists != null) {
                _uiState.update { it.copy(emailError = "Email already registered", isRegistering = false) }
                return@launch
            }

            val count  = db.userDao().getStudentCount(s.selectedRole)
            val prefix = if (s.selectedRole == "student") "KW" else "PR"

            // Create Firebase Auth account (fall back gracefully if offline)
            var firebaseUid = ""
            try {
                val authResult = FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(s.email, s.password).await()
                firebaseUid = authResult.user?.uid ?: ""
            } catch (e: FirebaseAuthUserCollisionException) {
                _uiState.update { it.copy(emailError = "Email already in use", isRegistering = false) }
                return@launch
            } catch (_: Exception) {
                // Network unavailable — register locally only
            }

            val user = User(
                firebaseUid     = firebaseUid,
                studentId       = "$prefix${count + 1}",
                fullName        = s.fullName,
                email           = s.email,
                passwordHash    = hash(s.password),
                role            = s.selectedRole,
                ubStudentNumber = if (s.selectedRole == "student") s.ubNumber.trim().uppercase() else "",
                university      = s.verifiedUniversity?.name ?: "",
                isVerified      = s.selectedRole == "student"
            )

            val localId = db.userDao().insertUser(user)

            // Push profile to Firestore if Firebase Auth succeeded
            if (firebaseUid.isNotEmpty()) {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(firebaseUid)
                        .set(user.copy(userId = localId))
                        .await()
                } catch (_: Exception) { /* will sync later via pushPendingUsers */ }
            }

            _uiState.update { it.copy(isRegistering = false) }
            _navigateTo.emit("login")
        }
    }

    private fun hash(pw: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
