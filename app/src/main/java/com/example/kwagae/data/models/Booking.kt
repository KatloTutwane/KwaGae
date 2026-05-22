package com.example.kwagae.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore-only model — no Room annotation.
 * Stored in the "bookings" collection.
 */
data class Booking(
    @DocumentId
    val bookingId: String = "",

    // Listing reference
    val listingLocalId: Long = 0,
    val firestoreListingId: String = "",
    val listingTitle: String = "",
    val listingLocation: String = "",
    val listingImageUrl: String = "",

    // Provider reference
    val providerUid: String = "",
    val providerName: String = "",

    // Student reference
    val studentUid: String = "",
    val studentName: String = "",
    val studentEmail: String = "",

    // Financials
    val price: Double = 0.0,
    val depositAmount: Double = 0.0,

    // Status: pending | approved | rejected | cancelled
    val status: String = "pending",

    // Linked reservation
    val reservationRef: String = "",

    @ServerTimestamp
    val bookingDate: Date? = null,

    val moveInDate: String = "",
    val notes: String = ""
)
