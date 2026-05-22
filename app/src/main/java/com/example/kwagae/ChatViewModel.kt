package com.example.kwagae

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwagae.data.models.ChatThread
import com.example.kwagae.data.models.Message
import com.example.kwagae.data.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val otherName: String = "Landlord"
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)

    // Chat context — read from the global args object set before navigation
    val threadId     = ChatNavArgs.threadId
    val providerId   = ChatNavArgs.providerId
    val providerName = ChatNavArgs.providerName
    val listingId    = ChatNavArgs.listingId
    val listingTitle = ChatNavArgs.listingTitle

    val currentUserId = prefs.getLong("user_id", -1L).toString()
    val currentName   = prefs.getString("full_name", "Student") ?: "Student"
    val currentRole   = prefs.getString("role", "student") ?: "student"

    private val _uiState = MutableStateFlow(
        ChatUiState(otherName = providerName.ifEmpty { "Landlord" })
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        if (threadId.isNotBlank()) {
            viewModelScope.launch {
                try {
                    ChatRepository.getMessages(threadId).collect { msgs ->
                        val other = if (currentRole == "provider") {
                            msgs.firstOrNull { it.senderId != currentUserId }?.senderName ?: "Student"
                        } else {
                            providerName.ifEmpty { "Landlord" }
                        }
                        _uiState.update { it.copy(messages = msgs, otherName = other) }
                    }
                } catch (_: Exception) { /* flow already handles errors silently */ }
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isSending) return
        _uiState.update { it.copy(inputText = "", isSending = true) }
        viewModelScope.launch {
            try {
                val msgs      = _uiState.value.messages
                val studentId = if (currentRole == "student") currentUserId
                    else msgs.firstOrNull { it.senderId != currentUserId }?.senderId ?: ""
                val thread = ChatThread(
                    threadId     = threadId,
                    studentId    = studentId,
                    studentName  = if (currentRole == "student") currentName else _uiState.value.otherName,
                    providerId   = if (currentRole == "provider") currentUserId else providerId,
                    providerName = if (currentRole == "provider") currentName else providerName,
                    listingId    = listingId,
                    listingTitle = listingTitle,
                    lastMessage  = text
                )
                ChatRepository.sendMessage(thread, currentUserId, currentName, text)
            } finally {
                _uiState.update { it.copy(isSending = false) }
            }
        }
    }
}
