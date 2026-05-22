package com.example.kwagae.data.repository

import com.example.kwagae.data.models.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BookingRepository {

    private val col = FirebaseFirestore.getInstance().collection("bookings")

    /** Real-time stream of all bookings belonging to a provider. */
    fun getProviderBookings(providerUid: String): Flow<List<Booking>> = callbackFlow {
        val sub = col
            .whereEqualTo("providerUid", providerUid)
            .orderBy("bookingDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(Booking::class.java) } ?: emptyList())
            }
        awaitClose { sub.remove() }
    }

    /** Real-time stream of all bookings made by a student. */
    fun getStudentBookings(studentUid: String): Flow<List<Booking>> = callbackFlow {
        val sub = col
            .whereEqualTo("studentUid", studentUid)
            .orderBy("bookingDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(Booking::class.java) } ?: emptyList())
            }
        awaitClose { sub.remove() }
    }

    /** Create a new booking document; returns the generated Firestore ID. */
    suspend fun createBooking(booking: Booking): String {
        val ref = col.document()
        ref.set(booking.copy(bookingId = ref.id)).await()
        return ref.id
    }

    /** Provider approves or rejects a booking. status = "approved" | "rejected". */
    suspend fun updateStatus(bookingId: String, status: String) {
        col.document(bookingId).update("status", status).await()
    }

    /** Student cancels their own booking. */
    suspend fun cancelBooking(bookingId: String) {
        col.document(bookingId).update("status", "cancelled").await()
    }

    /** One-time fetch of all bookings for a specific listing. */
    suspend fun getBookingsForListing(firestoreListingId: String): List<Booking> =
        col.whereEqualTo("firestoreListingId", firestoreListingId)
            .get().await()
            .documents.mapNotNull { it.toObject(Booking::class.java) }
}
