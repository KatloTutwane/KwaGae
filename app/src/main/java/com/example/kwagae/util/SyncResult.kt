package com.example.kwagae.util

/** Wraps the result of any Room ↔ Firestore sync operation */
sealed class SyncResult<out T> {
    data class Success<T>(val data: T) : SyncResult<T>()
    data class Error(val message: String, val cause: Exception? = null) : SyncResult<Nothing>()
    object Loading : SyncResult<Nothing>()
}