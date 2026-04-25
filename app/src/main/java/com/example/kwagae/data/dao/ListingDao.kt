package com.example.kwagae.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kwagae.data.models.Listing
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings WHERE isAvailable = 1")
    fun getAvailableListings(): Flow<List<Listing>>

    @Query("SELECT * FROM listings")
    suspend fun getAllListings(): List<Listing>

    @Insert
    suspend fun insertListing(listing: Listing)
}
