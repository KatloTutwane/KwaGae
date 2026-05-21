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

    val role: String = "student",        // "student" | "provider"

    /** Student number in the format used by the student's university (e.g. UB20210001, BOTHO210001) */
    val ubStudentNumber: String = "",

    /** Name of the university that issued the student number (e.g. "University of Botswana") */
    val university: String = "",

    /** True once the university registry has confirmed this is a real enrolled student */
    val isVerified: Boolean = false,

    @get:Exclude
    val passwordHash: String = "",

    @ServerTimestamp
    val syncedAt: Date? = null,

    @get:Exclude
    val pendingSync: Boolean = false
)