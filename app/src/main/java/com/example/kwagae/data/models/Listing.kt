package com.example.kwagae.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Listing entity — stored in Room locally and mirrored in Firestore under:
 *   /listings/{firestoreId}
 *
 * Sync strategy:
 *  - Listings are authored by landlords via Firebase Console / admin app,
 *    and pulled down into Room via a real-time Firestore listener.
 *  - Student users can favourite/apply; those writes are pushed back to Firestore.
 *  - [pendingSync] flags any listing created/edited offline.
 */
@Entity(tableName = "listings")
data class Listing(

    // ── Local Room PK ─────────────────────────────────────────────────────────
    @PrimaryKey(autoGenerate = true)
    @get:Exclude
    val listingId: Long = 0,

    // ── Firestore document ID ─────────────────────────────────────────────────
    @DocumentId
    val firestoreId: String = "",

    // ── Core listing fields ───────────────────────────────────────────────────
    val title: String = "",
    val description: String = "",
    val location: String = "",

    /** Property type — e.g. "House", "Apartment", "Studio", "Room", "Townhouse" */
    val type: String = "",

    /** Monthly rent in Botswana Pula (BWP) */
    val price: Double = 0.0,

    /** Security deposit in BWP (typically 1–2 months rent) */
    val depositAmount: Double = 0.0,

    val availabilityDate: String = "",
    val isAvailable: Boolean = true,

    /** Comma-separated string in Room, converted by [Converters]; List<String> in Firestore */
    val amenities: String = "",

    /** URL of the listing's primary image (loaded with Coil) */
    val imageUrl: String = "",

    /** Firebase Auth UID / studentId of the landlord who posted this listing */
    val ownerUid: String = "",

    /** Display name of the provider / landlord for use in the chat UI */
    val providerName: String = "",

    // ── Sync metadata ─────────────────────────────────────────────────────────
    @ServerTimestamp
    val syncedAt: Date? = null,

    @get:Exclude
    val pendingSync: Boolean = false
)