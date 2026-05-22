package com.example.kwagae.data.repository

import com.example.kwagae.data.models.ChatThread
import com.example.kwagae.data.models.Message
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * All Firestore operations for the chat feature.
 *
 * Firestore structure:
 *   /chats/{threadId}                — ChatThread metadata
 *   /chats/{threadId}/messages/{id} — individual Message documents
 */
object ChatRepository {

    private val db    = FirebaseFirestore.getInstance()
    private val chats = db.collection("chats")

    // ── Real-time streams ─────────────────────────────────────────────────────

    /**
     * Emits the full message list for [threadId] every time Firestore updates.
     * Messages are ordered oldest-first so they stack naturally in a chat UI.
     */
    fun getMessages(threadId: String): Flow<List<Message>> = callbackFlow {
        if (threadId.isBlank()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val listener = chats.document(threadId)
            .collection("messages")
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Emit empty list on error instead of crashing the coroutine
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Emits all threads where the current user is either a student or provider.
     * [userId] is the user's identifier (Room userId as String for students,
     * studentId / ownerUid for providers).
     * [role] must be "student" or "provider".
     */
    fun getThreadsForUser(userId: String, role: String): Flow<List<ChatThread>> = callbackFlow {
        if (userId.isBlank() || userId == "-1") {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val field = if (role == "provider") "providerId" else "studentId"

        // No orderBy here — that would require a composite index in Firestore.
        // We sort client-side by lastMessageAt instead.
        val listener = chats
            .whereEqualTo(field, userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "getThreadsForUser failed (userId=$userId role=$role): $error")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val threads = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toObject(ChatThread::class.java) }
                    ?.sortedByDescending { it.lastMessageAt }
                    ?: emptyList()
                trySend(threads)
            }
        awaitClose { listener.remove() }
    }

    // ── Writes ────────────────────────────────────────────────────────────────

    /**
     * Send a message.
     *
     * On the first message in a new thread this creates the /chats/{threadId}
     * metadata document.  On subsequent messages it updates [lastMessage] and
     * [lastMessageAt] via a merge write so other fields are not overwritten.
     */
    suspend fun sendMessage(
        thread: ChatThread,
        senderId: String,
        senderName: String,
        text: String
    ) {
        val threadRef = chats.document(thread.threadId)

        // Upsert thread metadata (merge so we don't clobber existing fields)
        threadRef.set(
            thread.copy(lastMessage = text),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()

        // Append the message to the sub-collection
        threadRef.collection("messages").add(
            Message(
                senderId   = senderId,
                senderName = senderName,
                text       = text
            )
        ).await()
    }
}
