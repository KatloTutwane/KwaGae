package com.example.kwagae.data.dao

import androidx.room.*
import com.example.kwagae.data.models.Listing
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {

    // ── Reads (reactive Flow for UI) ──────────────────────────────────────────

    @Query("SELECT * FROM listings WHERE isAvailable = 1 ORDER BY syncedAt DESC")
    fun getAvailableListings(): Flow<List<Listing>>

    @Query("SELECT * FROM listings ORDER BY syncedAt DESC")
    fun getAllListings(): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE listingId = :id LIMIT 1")
    suspend fun getById(id: Long): Listing?

    @Query("SELECT * FROM listings WHERE firestoreId = :fid LIMIT 1")
    suspend fun getByFirestoreId(fid: String): Listing?

    @Query("SELECT * FROM listings WHERE pendingSync = 1")
    suspend fun getPendingSyncListings(): List<Listing>

    @Query("""
        SELECT * FROM listings
        WHERE isAvailable = 1
          AND (title LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%')
        ORDER BY price ASC
    """)
    fun searchListings(query: String): Flow<List<Listing>>

    // ── Writes ────────────────────────────────────────────────────────────────

    /** Upsert — used when pulling from Firestore snapshot */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listing: Listing): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listings: List<Listing>)

    @Update
    suspend fun update(listing: Listing)

    @Query("UPDATE listings SET pendingSync = 0, firestoreId = :fid WHERE listingId = :localId")
    suspend fun markSynced(localId: Long, fid: String)

    @Query("DELETE FROM listings WHERE firestoreId = :fid")
    suspend fun deleteByFirestoreId(fid: String)

    @Query("DELETE FROM listings")
    suspend fun clearAll()
}