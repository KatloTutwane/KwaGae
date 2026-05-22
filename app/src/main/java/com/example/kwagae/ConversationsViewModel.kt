package com.example.kwagae

import android.app.Application
import android.content.Context
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

    val currentUserId = prefs.getLong("user_id", -1L).toString()
    val currentRole   = prefs.getString("role", "student") ?: "student"

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                ChatRepository.getThreadsForUser(currentUserId, currentRole).collect { list ->
                    _uiState.update { it.copy(threads = list, isLoading = false) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
