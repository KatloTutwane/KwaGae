package com.example.kwagae.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class Listing(
    @PrimaryKey(autoGenerate = true)
    val listingId: Long = 0,
    val title: String,
    val price: Double,
    val location: String,
    val availabilityDate: String,
    val amenities: String,
    val isAvailable: Boolean = true
)
