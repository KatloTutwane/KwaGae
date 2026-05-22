package com.example.kwagae

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db        = AppDatabase.getDatabase(application)
    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigateTo = MutableSharedFlow<String>()
    val navigateTo: SharedFlow<String> = _navigateTo.asSharedFlow()

    fun onEmailChange(v: String)    { _uiState.update { it.copy(email = v.trim(), errorMessage = "") } }
    fun onPasswordChange(v: String) { _uiState.update { it.copy(password = v, errorMessage = "") } }
    fun togglePasswordVisible()     { _uiState.update { it.copy(passwordVisible = !it.passwordVisible) } }

    fun login() {
        val s = _uiState.value
        if (s.email.isEmpty() || s.password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please fill all fields") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = "") }
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(s.email, s.password).await()
                val uid = authResult.user?.uid
                if (uid != null) {
                    val user = fetchAndCacheProfile(uid, s.email, s.password)
                    savePrefsAndNavigate(user)
                } else {
                    fallbackToRoom(s.email, s.password)
                }
            } catch (_: Exception) {
                // Firebase unavailable or no account — try local Room (seeded users)
                fallbackToRoom(s.email, s.password)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = "") }
        viewModelScope.launch {
            try {
                val credential   = GoogleAuthProvider.getCredential(idToken, null)
                val authResult   = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                    ?: throw Exception("No user returned from Google Sign-In")
                val user = fetchOrCreateGoogleUser(
                    uid         = firebaseUser.uid,
                    displayName = firebaseUser.displayName ?: "",
                    email       = firebaseUser.email ?: ""
                )
                savePrefsAndNavigate(user)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Google sign-in failed: ${e.localizedMessage}")
                }
            }
        }
    }

    private suspend fun fetchAndCacheProfile(uid: String, email: String, password: String): User {
        return try {
            val doc    = firestore.collection("users").document(uid).get().await()
            val remote = doc.toObject(User::class.java)
            if (remote != null) {
                val existing = db.userDao().getByFirebaseUid(uid)
                val toSave   = remote.copy(
                    userId       = existing?.userId ?: 0,
                    firebaseUid  = uid,
                    passwordHash = hash(password),
                    pendingSync  = false
                )
                val localId = db.userDao().insert(toSave)
                toSave.copy(userId = localId)
            } else {
                db.userDao().getByFirebaseUid(uid)
                    ?: db.userDao().login(email, hash(password))
                    ?: User(firebaseUid = uid, email = email)
            }
        } catch (_: Exception) {
            db.userDao().getByFirebaseUid(uid)
                ?: db.userDao().login(email, hash(password))
                ?: User(firebaseUid = uid, email = email)
        }
    }

    private suspend fun fetchOrCreateGoogleUser(uid: String, displayName: String, email: String): User {
        db.userDao().getByFirebaseUid(uid)?.let { return it }
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val user = doc.toObject(User::class.java)!!.copy(firebaseUid = uid)
                val localId = db.userDao().insert(user)
                user.copy(userId = localId)
            } else {
                val newUser = User(
                    firebaseUid = uid,
                    fullName    = displayName,
                    email       = email,
                    role        = "student",
                    studentId   = "KWG-${uid.take(6)}"
                )
                firestore.collection("users").document(uid).set(newUser).await()
                val localId = db.userDao().insert(newUser)
                newUser.copy(userId = localId)
            }
        } catch (_: Exception) {
            val newUser = User(
                firebaseUid = uid,
                fullName    = displayName,
                email       = email,
                role        = "student",
                studentId   = "KWG-${uid.take(6)}"
            )
            val localId = db.userDao().insert(newUser)
            newUser.copy(userId = localId)
        }
    }

    private suspend fun fallbackToRoom(email: String, password: String) {
        val localUser = db.userDao().login(email, hash(password))
        if (localUser != null) {
            savePrefsAndNavigate(localUser)
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid email or password") }
        }
    }

    private suspend fun savePrefsAndNavigate(user: User) {
        getApplication<Application>()
            .getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)
            .edit()
            .putLong("user_id",     user.userId)
            .putString("student_id",  user.studentId)
            .putString("full_name",   user.fullName)
            .putString("role",        user.role)
            .putString("firebase_uid", user.firebaseUid)
            .apply()
        _uiState.update { it.copy(isLoading = false) }
        _navigateTo.emit("main")
    }

    private fun hash(pw: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
