package com.example.kwagae.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Entity(tableName = "users")
data class User(

    @PrimaryKey(autoGenerate = true)
    @get:Exclude
    val userId: Long = 0,

    @DocumentId
    val firebaseUid: String = "",

    val fullName: String = "",
    val email: String = "",
    val studentId: String = "",

    // ── ADD THIS — MainScreen reads user.role to show "STUDENT HOMESEEKER" etc.
    val role: String = "student",   // "student" | "landlord"

    @get:Exclude
    val passwordHash: String = "",

    @ServerTimestamp
    val syncedAt: Date? = null,

    @get:Exclude
    val pendingSync: Boolean = false
)