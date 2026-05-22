package com.example.kwagae.data.dao

import androidx.room.*
import com.example.kwagae.data.models.Listing
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {

    // ── Reactive Flow queries (for UI / real-time updates) ────────────────────

    @Query("SELECT * FROM listings WHERE isAvailable = 1 ORDER BY syncedAt DESC")
    fun getAvailableListings(): Flow<List<Listing>>

    @Query("SELECT * FROM listings ORDER BY syncedAt DESC")
    fun getAllListingsFlow(): Flow<List<Listing>>

    @Query("""
        SELECT * FROM listings
        WHERE isAvailable = 1
          AND (title LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%')
        ORDER BY price ASC
    """)
    fun searchListings(query: String): Flow<List<Listing>>

    // ── One-shot suspend queries (for coroutine / LaunchedEffect use) ──────────

    // ADD THIS — MainScreen calls db.listingDao().getAllListings() in a coroutine
    @Query("SELECT * FROM listings ORDER BY syncedAt DESC")
    suspend fun getAllListings(): List<Listing>

    @Query("SELECT * FROM listings WHERE listingId = :id LIMIT 1")
    suspend fun getById(id: Long): Listing?

    @Query("SELECT * FROM listings WHERE firestoreId = :fid LIMIT 1")
    suspend fun getByFirestoreId(fid: String): Listing?

    @Query("SELECT * FROM listings WHERE pendingSync = 1")
    suspend fun getPendingSyncListings(): List<Listing>

    // ── Writes ────────────────────────────────────────────────────────────────

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

    // ── Provider-specific queries ──────────────────────────────────────────────

    /** All listings owned by a specific provider, newest first */
    @Query("SELECT * FROM listings WHERE ownerUid = :ownerUid ORDER BY syncedAt DESC, listingId DESC")
    fun getByOwnerUid(ownerUid: String): Flow<List<Listing>>

    /** Hard-delete a listing by its local Room ID */
    @Query("DELETE FROM listings WHERE listingId = :listingId")
    suspend fun deleteById(listingId: Long)

    /** Flip the availability flag without touching other fields */
    @Query("UPDATE listings SET isAvailable = :isAvailable WHERE listingId = :listingId")
    suspend fun updateAvailability(listingId: Long, isAvailable: Boolean)

    /** Mark a listing as reserved after deposit payment */
    @Query("UPDATE listings SET isReserved = 1, reservedByUid = :uid, reservationRef = :ref WHERE listingId = :listingId")
    suspend fun reserveListing(listingId: Long, uid: String, ref: String)
}