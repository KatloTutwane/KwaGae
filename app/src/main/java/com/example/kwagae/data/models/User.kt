package com.example.kwagae.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)] // prevents duplicate emails at DB level
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val studentId: String = "",   // KW001 for students, PR001 for providers
    val fullName: String,
    val email: String,
    val password: String,
    val role: String = "student"  // "student" or "provider"
)