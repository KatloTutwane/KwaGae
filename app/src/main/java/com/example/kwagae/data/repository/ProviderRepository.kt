package com.example.kwagae.data.repository

import com.example.kwagae.data.dao.ListingDao
import com.example.kwagae.data.models.Listing
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ProviderRepository(private val dao: ListingDao) {

    private val firestore   = FirebaseFirestore.getInstance()
    private val listingsCol = firestore.collection("listings")

    fun getProviderListings(ownerUid: String): Flow<List<Listing>> = dao.getByOwnerUid(ownerUid)

    suspend fun saveListing(listing: Listing): Long {
        val localId = dao.insert(listing.copy(pendingSync = true))
        try {
            val docRef = listingsCol.document()
            docRef.set(listing.copy(listingId = localId)).await()
            dao.markSynced(localId, docRef.id)
        } catch (_: Exception) { /* offline — will sync on next launch */ }
        return localId
    }

    suspend fun updateListing(listing: Listing) {
        dao.update(listing.copy(pendingSync = true))
        try {
            if (listing.firestoreId.isNotEmpty()) {
                listingsCol.document(listing.firestoreId)
                    .set(listing, SetOptions.merge())
                    .await()
                dao.markSynced(listing.listingId, listing.firestoreId)
            } else {
                val docRef = listingsCol.document()
                docRef.set(listing).await()
                dao.markSynced(listing.listingId, docRef.id)
            }
        } catch (_: Exception) { /* offline */ }
    }

    suspend fun deleteListing(listingId: Long) {
        val listing = dao.getById(listingId)
        dao.deleteById(listingId)
        val fid = listing?.firestoreId
        if (!fid.isNullOrEmpty()) {
            try { listingsCol.document(fid).delete().await() } catch (_: Exception) {}
        }
    }

    suspend fun toggleAvailability(listingId: Long, currentValue: Boolean) {
        dao.updateAvailability(listingId, !currentValue)
        val listing = dao.getById(listingId) ?: return
        if (listing.firestoreId.isNotEmpty()) {
            try {
                // Firebase serializes isAvailable as "available" (JavaBeans boolean convention)
                listingsCol.document(listing.firestoreId)
                    .update("available", !currentValue)
                    .await()
            } catch (_: Exception) {}
        }
    }
}
