package com.example.kwagae.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

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

    val type: String = "",

    /** Monthly rent in Botswana Pula (BWP) */
    val price: Double = 0.0,

    /** Security deposit in BWP */
    val depositAmount: Double = 0.0,

    val availabilityDate: String = "",
    val isAvailable: Boolean = true,

    /** Comma-separated string in Room; List<String> in Firestore */
    val amenities: String = "",

    /** Primary image URL (kept for backward compat with seeded/Firestore data) */
    val imageUrl: String = "",

    /** Firebase Auth UID of the provider who posted this listing */
    val ownerUid: String = "",

    /** Display name of the provider for the chat UI */
    val providerName: String = "",

    // ── Provider-extended fields ──────────────────────────────────────────────

    /** Comma-separated content URIs or remote URLs for multiple listing images */
    val imageUrls: String = "",

    /** Contact phone / WhatsApp number for direct enquiries */
    val contactInfo: String = "",

    // Utilities included
    val wifiIncluded: Boolean = false,
    val waterIncluded: Boolean = false,
    val electricityIncluded: Boolean = false,

    // Property features
    val parkingAvailable: Boolean = false,
    val securityAvailable: Boolean = false,
    val isFurnished: Boolean = false,
    val kitchenAvailable: Boolean = false,

    /** Number of rentable rooms */
    val roomCount: Int = 1,

    /** "Shared", "En-suite", or "Private" */
    val bathroomType: String = "Shared",

    // Policies
    /** Comma-separated nearby institutions (e.g. "UB, Botho University") */
    val nearbySchools: String = "",

    /** House rules shown to prospective tenants */
    val rules: String = "",

    /** "Any", "Male only", or "Female only" */
    val genderPreference: String = "Any",

    /** Maximum number of occupants allowed */
    val maxOccupants: Int = 1,

    // ── Reservation ───────────────────────────────────────────────────────────

    /** True once a student has paid the deposit and reserved this listing */
    val isReserved: Boolean = false,

    /** Firebase UID (or Room userId string) of the student who reserved it */
    val reservedByUid: String = "",

    /** Receipt reference number, e.g. KWG-REF-A3F7C2 */
    val reservationRef: String = "",

    // ── Sync metadata ─────────────────────────────────────────────────────────
    @ServerTimestamp
    val syncedAt: Date? = null,

    @get:Exclude
    val pendingSync: Boolean = false
)
