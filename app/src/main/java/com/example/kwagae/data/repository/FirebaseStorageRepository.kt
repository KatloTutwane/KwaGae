package com.example.kwagae.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Uploads listing images to Firebase Storage.
 * Local content URIs are uploaded; existing HTTPS URLs are returned unchanged.
 * Storage path: listings/{ownerUid}/{listingLocalId}/img_{index}.jpg
 */
object FirebaseStorageRepository {

    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadListingImages(
        context: Context,
        ownerUid: String,
        listingLocalId: Long,
        uris: List<String>
    ): List<String> = uris.mapIndexed { index, uriStr ->
        if (uriStr.startsWith("http://") || uriStr.startsWith("https://")) return@mapIndexed uriStr
        try {
            val bytes = context.contentResolver
                .openInputStream(Uri.parse(uriStr))
                ?.use { it.readBytes() }
                ?: return@mapIndexed uriStr
            val ref = storage.reference.child("listings/$ownerUid/$listingLocalId/img_$index.jpg")
            ref.putBytes(bytes).await()
            ref.downloadUrl.await().toString()
        } catch (_: Exception) {
            uriStr   // keep original URI on any failure
        }
    }

    suspend fun deleteListingFolder(ownerUid: String, listingLocalId: Long) {
        try {
            val ref = storage.reference.child("listings/$ownerUid/$listingLocalId")
            ref.listAll().await().items.forEach { item ->
                try { item.delete().await() } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
