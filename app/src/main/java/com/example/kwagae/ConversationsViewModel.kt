package com.example.kwagae

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwagae.data.models.ChatThread
import com.example.kwagae.data.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ConversationsUiState(
    val threads: List<ChatThread> = emptyList(),
    val isLoading: Boolean = true
)

class ConversationsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)

    val currentRole   = prefs.getString("role", "student") ?: "student"
    val currentUserId = if (currentRole == "provider") {
        // Real Firebase providers use firebase_uid; seeded providers (PR001 etc.) use studentId
        val uid = prefs.getString("firebase_uid", "") ?: ""
        if (uid.isNotEmpty()) uid
        else prefs.getString("student_id", "") ?: ""
    } else {
        prefs.getLong("user_id", -1L).toString()
    }

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        Log.d("ConversationsVM", "Loading threads for userId=$currentUserId role=$currentRole")
        viewModelScope.launch {
            try {
                ChatRepository.getThreadsForUser(currentUserId, currentRole).collect { list ->
                    Log.d("ConversationsVM", "Got ${list.size} threads")
                    _uiState.update { it.copy(threads = list, isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("ConversationsVM", "Failed to load threads", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
