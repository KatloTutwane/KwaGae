package com.example.kwagae.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * User entity — stored in Room locally and mirrored in Firestore under:
 *   /users/{firebaseUid}
 *
 * Sync strategy:
 *  - On registration: write to Room first, then push to Firestore.
 *  - On login: authenticate with Firebase Auth, then pull Firestore doc to Room.
 *  - [pendingSync] = true flags rows that haven't been pushed to Firestore yet
 *    (e.g. created while offline).
 */
@Entity(tableName = "users")
data class User(

    // ── Local Room PK ─────────────────────────────────────────────────────────
    @PrimaryKey(autoGenerate = true)
    @get:Exclude                        // don't push Room's auto-id to Firestore
    val userId: Long = 0,

    // ── Firestore document ID (Firebase Auth UID) ─────────────────────────────
    @DocumentId
    val firebaseUid: String = "",

    // ── Core fields ───────────────────────────────────────────────────────────
    val fullName: String = "",
    val email: String = "",
    val studentId: String = "",

    /**
     * Password hash — stored locally only for offline login fallback.
     * Never pushed to Firestore (use Firebase Auth for real authentication).
     */
    @get:Exclude
    val passwordHash: String = "",

    // ── Sync metadata ─────────────────────────────────────────────────────────
    @ServerTimestamp
    val syncedAt: Date? = null,

    @get:Exclude
    val pendingSync: Boolean = false
)