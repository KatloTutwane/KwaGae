package com.example.kwagae.data.repository

import com.example.kwagae.data.dao.UserDao
import com.example.kwagae.data.models.User
import com.example.kwagae.data.util.SyncResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for [User] data.
 *
 * Architecture:
 *   1. Firebase Auth handles authentication (email/password).
 *   2. Firestore stores the full user profile under /users/{uid}.
 *   3. Room caches the profile locally for offline access.
 *
 * Login flow:  Firebase Auth → fetch Firestore profile → cache in Room → return User
 * Register:    Firebase Auth → build User → write Room (pendingSync=true) → push Firestore
 * Offline:     Room local fallback using stored passwordHash for login
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val USERS_COLLECTION = "users"
    }

    // ── Registration ──────────────────────────────────────────────────────────

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        studentId: String
    ): SyncResult<User> {
        return try {
            // 1. Create Firebase Auth account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid
                ?: return SyncResult.Error("Firebase Auth returned no UID")

            // 2. Build user model
            val user = User(
                firebaseUid  = uid,
                fullName     = fullName,
                email        = email,
                studentId    = studentId,
                passwordHash = hashPassword(password), // local offline fallback only
                pendingSync  = false
            )

            // 3. Cache in Room
            val localId = userDao.insert(user)

            // 4. Push profile to Firestore (excludes passwordHash via @Exclude)
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .set(user, SetOptions.merge())
                .await()

            SyncResult.Success(user.copy(userId = localId))

        } catch (e: Exception) {
            SyncResult.Error("Registration failed: ${e.localizedMessage}", e)
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Attempts Firebase Auth sign-in, then syncs the Firestore profile into Room.
     * Falls back to Room-only login if offline.
     */
    suspend fun login(email: String, password: String): SyncResult<User> {
        return try {
            // 1. Firebase Auth sign-in
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid
                ?: return SyncResult.Error("Sign-in returned no UID")

            // 2. Pull fresh profile from Firestore and upsert into Room
            val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
            val firestoreUser = snapshot.toObject(User::class.java)
                ?: return SyncResult.Error("User profile not found in Firestore")

            val existing = userDao.getByFirebaseUid(uid)
            val toSave = firestoreUser.copy(
                userId       = existing?.userId ?: 0,
                passwordHash = hashPassword(password),
                pendingSync  = false
            )
            val localId = userDao.insert(toSave)

            SyncResult.Success(toSave.copy(userId = localId))

        } catch (e: Exception) {
            // Network unavailable — fall back to local Room login
            val localUser = userDao.login(email, hashPassword(password))
            if (localUser != null) {
                SyncResult.Success(localUser)
            } else {
                SyncResult.Error("Login failed (offline + no local account): ${e.localizedMessage}", e)
            }
        }
    }

    // ── Sign out ──────────────────────────────────────────────────────────────

    fun signOut() = auth.signOut()

    val currentFirebaseUser get() = auth.currentUser

    // ── Profile fetch ─────────────────────────────────────────────────────────

    suspend fun getUserByLocalId(localId: Long): User? = userDao.getById(localId)

    // ── Pending sync push (call from WorkManager / connectivity receiver) ─────

    suspend fun pushPendingUsers() {
        userDao.getPendingSyncUsers().forEach { user ->
            try {
                firestore.collection(USERS_COLLECTION)
                    .document(user.firebaseUid)
                    .set(user, SetOptions.merge())
                    .await()
                userDao.markSynced(user.userId, user.firebaseUid)
            } catch (_: Exception) { /* retry next time */ }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}