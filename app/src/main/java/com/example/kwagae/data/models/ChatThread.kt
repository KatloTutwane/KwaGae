package com.example.kwagae.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Metadata document for a conversation thread, stored in Firestore under:
 *   /chats/{threadId}
 *
 * threadId is deterministic: "chat_u{studentRoomId}_l{listingId}"
 * This ensures one thread per student-listing pair (no duplicates).
 */
data class ChatThread(
    @DocumentId val threadId: String = "",
    val studentId: String = "",        // Room userId.toString() of the student
    val studentName: String = "",
    val providerId: String = "",       // ownerUid / studentId of the provider
    val providerName: String = "",
    val listingId: Long = 0L,
    val listingTitle: String = "",
    val lastMessage: String = "",
    @ServerTimestamp val lastMessageAt: Date? = null
)
