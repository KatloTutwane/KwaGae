package com.example.kwagae.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kwagae.data.models.User

@Dao
interface UserDao {

    // ── Auth ──
    @Insert(onConflict = OnConflictStrategy.ABORT) // fails if email already exists
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    // ── Checks ──
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?  // duplicate email check

    @Query("SELECT * FROM users WHERE userId = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?           // load profile by session

    // ── Counts (separate so student cap works correctly) ──
    @Query("SELECT COUNT(*) FROM users WHERE role = 'student'")
    suspend fun getStudentCount(): Int                 // was counting ALL users

    @Query("SELECT COUNT(*) FROM users WHERE role = 'provider'")
    suspend fun getProviderCount(): Int

    // ── Provider specific ──
    @Query("SELECT * FROM users WHERE role = 'provider'")
    suspend fun getAllProviders(): List<User>
}