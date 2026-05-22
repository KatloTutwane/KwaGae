package com.example.kwagae.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * A single chat message stored in Firestore under:
 *   /chats/{threadId}/messages/{messageId}
 *
 * This is a Firestore-only model — not stored in Room.
 */
data class Message(
    @DocumentId val id: String = "",
    val senderId: String = "",      // Room userId as String
    val senderName: String = "",
    val text: String = "",
    @ServerTimestamp val sentAt: Date? = null,
    val isRead: Boolean = false
)
