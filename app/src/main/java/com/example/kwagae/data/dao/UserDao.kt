package com.example.kwagae.data.dao

import androidx.room.*
import com.example.kwagae.data.models.User
import kotlinx.coroutines.flow.Flow

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

    // ── Writes ────────────────────────────────────────────────────────────────

    /** Insert or replace — used when pulling from Firestore */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("UPDATE users SET pendingSync = 0, firebaseUid = :uid WHERE userId = :localId")
    suspend fun markSynced(localId: Long, uid: String)

    @Query("DELETE FROM users WHERE firebaseUid = :uid")
    suspend fun deleteByFirebaseUid(uid: String)
}