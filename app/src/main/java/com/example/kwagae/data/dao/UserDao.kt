package com.example.kwagae.data.dao

import androidx.room.*
import com.example.kwagae.data.models.User

@Dao
interface UserDao {

    // ── Reads ─────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun login(email: String, passwordHash: String): User?

    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    suspend fun getByFirebaseUid(uid: String): User?

    @Query("SELECT * FROM users WHERE userId = :id LIMIT 1")
    suspend fun getById(id: Long): User?

    @Query("SELECT * FROM users WHERE pendingSync = 1")
    suspend fun getPendingSyncUsers(): List<User>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    // ── RegisterScreen needs these three ──────────────────────────────────────

    // 1. Check if email already registered
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // 2. Count users by role for generating IDs like KW1, PR2
    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    suspend fun getStudentCount(role: String): Int  // ✅ Removed default parameter

    // 3. Named insert to match RegisterScreen's call
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    // 4. Bulk insert for DatabaseSeeder
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(users: List<User>)

    // ── Writes ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("UPDATE users SET pendingSync = 0, firebaseUid = :uid WHERE userId = :localId")
    suspend fun markSynced(localId: Long, uid: String)

    @Query("DELETE FROM users WHERE firebaseUid = :uid")
    suspend fun deleteByFirebaseUid(uid: String)
}