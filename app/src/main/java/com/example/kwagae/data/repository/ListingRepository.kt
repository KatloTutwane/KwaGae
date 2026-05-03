package com.example.kwagae.data.repository

import com.example.kwagae.data.dao.ListingDao
import com.example.kwagae.data.models.Listing
import com.example.kwagae.data.util.SyncResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for [Listing] data.
 *
 * Architecture:
 *   1. Room serves all UI queries via Flow — instant, offline-capable.
 *   2. A Firestore real-time snapshot listener keeps Room up-to-date whenever
 *      the device is online.
 *   3. Listings created locally while offline are flagged [pendingSync = true]
 *      and pushed when connectivity returns.
 *
 * Firestore path:  /listings/{listingId}
 */
@Singleton
class ListingRepository @Inject constructor(
    private val listingDao: ListingDao,
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val LISTINGS_COLLECTION = "listings"
    }

    private var listenerRegistration: ListenerRegistration? = null

    // ── UI-facing Room flows (always fast, works offline) ─────────────────────

    fun getAvailableListings(): Flow<List<Listing>> =
        listingDao.getAvailableListings()

    fun getAllListings(): Flow<List<Listing>> =
        listingDao.getAllListingsFlow()

    fun searchListings(query: String): Flow<List<Listing>> =
        listingDao.searchListings(query)

    suspend fun getListingById(id: Long): Listing? =
        listingDao.getById(id)

    // ── Real-time Firestore → Room sync ───────────────────────────────────────

    /**
     * Attaches a Firestore snapshot listener that keeps Room in sync.
     * Call from Application.onCreate() or a long-lived ViewModel.
     * Cancel with [stopListening] when no longer needed.
     */
    fun startListening(scope: CoroutineScope) {
        listenerRegistration?.remove()   // clear any existing listener

        listenerRegistration = firestore
            .collection(LISTINGS_COLLECTION)
            .whereEqualTo("available", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                scope.launch(Dispatchers.IO) {
                    val listings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Listing::class.java)?.copy(
                            firestoreId = doc.id,
                            pendingSync = false
                        )
                    }
                    // Merge: preserve local Room IDs for docs we already have
                    listings.forEach { remote ->
                        val existing = listingDao.getByFirestoreId(remote.firestoreId)
                        listingDao.insert(remote.copy(listingId = existing?.listingId ?: 0))
                    }

                    // Remove local listings that were deleted remotely
                    val remoteIds = listings.map { it.firestoreId }.toSet()
                    listingDao.getAllListings().forEach { local ->
                        if (local.firestoreId.isNotEmpty() && local.firestoreId !in remoteIds) {
                            listingDao.deleteByFirestoreId(local.firestoreId)
                        }
                    }
                }
            }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    // ── Write operations ──────────────────────────────────────────────────────

    /** Create a new listing — saves locally first, then pushes to Firestore */
    suspend fun createListing(listing: Listing): SyncResult<Listing> {
        return try {
            // 1. Save locally (pendingSync = true until Firestore confirms)
            val localId = listingDao.insert(listing.copy(pendingSync = true))

            // 2. Push to Firestore
            val docRef = firestore.collection(LISTINGS_COLLECTION).document()
            docRef.set(listing).await()

            // 3. Mark synced with the Firestore ID
            listingDao.markSynced(localId, docRef.id)

            SyncResult.Success(listing.copy(listingId = localId, firestoreId = docRef.id))
        } catch (e: Exception) {
            // Listing is saved locally; will sync when online
            SyncResult.Error("Saved locally; will sync when online: ${e.localizedMessage}", e)
        }
    }

    /** Update an existing listing in both Room and Firestore */
    suspend fun updateListing(listing: Listing): SyncResult<Listing> {
        return try {
            listingDao.update(listing.copy(pendingSync = true))

            if (listing.firestoreId.isNotEmpty()) {
                firestore.collection(LISTINGS_COLLECTION)
                    .document(listing.firestoreId)
                    .set(listing, SetOptions.merge())
                    .await()
                listingDao.markSynced(listing.listingId, listing.firestoreId)
            }

            SyncResult.Success(listing)
        } catch (e: Exception) {
            SyncResult.Error("Update failed: ${e.localizedMessage}", e)
        }
    }

    /** Delete listing from both Room and Firestore */
    suspend fun deleteListing(listing: Listing): SyncResult<Unit> {
        return try {
            if (listing.firestoreId.isNotEmpty()) {
                firestore.collection(LISTINGS_COLLECTION)
                    .document(listing.firestoreId)
                    .delete()
                    .await()
            }
            listingDao.deleteByFirestoreId(listing.firestoreId)
            SyncResult.Success(Unit)
        } catch (e: Exception) {
            SyncResult.Error("Delete failed: ${e.localizedMessage}", e)
        }
    }

    // ── Offline sync push ─────────────────────────────────────────────────────

    /** Push any locally created listings that haven't reached Firestore yet */
    suspend fun pushPendingListings() {
        listingDao.getPendingSyncListings().forEach { listing ->
            try {
                if (listing.firestoreId.isEmpty()) {
                    // New listing — get a fresh Firestore ID
                    val docRef = firestore.collection(LISTINGS_COLLECTION).document()
                    docRef.set(listing).await()
                    listingDao.markSynced(listing.listingId, docRef.id)
                } else {
                    // Existing listing — just push the update
                    firestore.collection(LISTINGS_COLLECTION)
                        .document(listing.firestoreId)
                        .set(listing, SetOptions.merge())
                        .await()
                    listingDao.markSynced(listing.listingId, listing.firestoreId)
                }
            } catch (_: Exception) { /* retry next time */ }
        }
    }
}
