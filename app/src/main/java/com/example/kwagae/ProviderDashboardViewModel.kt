package com.example.kwagae

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.Listing
import com.example.kwagae.data.repository.ProviderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProviderDashboardUiState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = true,
    val totalCount: Int = 0,
    val availableCount: Int = 0,
    val unavailableCount: Int = 0,
    val deletedMessage: String = ""
)

class ProviderDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)

    // Firebase UID → seeded studentId (e.g. "PR001") → Room auto-ID
    val ownerUid  = prefs.getString("firebase_uid", null)?.takeIf { it.isNotEmpty() }
        ?: prefs.getString("student_id", null)?.takeIf { it.isNotEmpty() }
        ?: prefs.getLong("user_id", -1L).toString()
    val ownerName = prefs.getString("full_name", "Provider") ?: "Provider"

    private val repo = ProviderRepository(AppDatabase.getDatabase(application).listingDao())

    private val _rawListings    = MutableStateFlow<List<Listing>>(emptyList())
    private val _isLoading      = MutableStateFlow(true)
    private val _deletedMessage = MutableStateFlow("")
    private val _searchQuery    = MutableStateFlow("")

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<ProviderDashboardUiState> = combine(
        _rawListings, _isLoading, _deletedMessage, _searchQuery
    ) { listings, loading, deleted, q ->
        val filtered = if (q.isBlank()) listings
        else listings.filter {
            it.title.contains(q, ignoreCase = true) ||
            it.location.contains(q, ignoreCase = true) ||
            it.type.contains(q, ignoreCase = true)
        }
        ProviderDashboardUiState(
            listings         = filtered,
            isLoading        = loading,
            totalCount       = listings.size,
            availableCount   = listings.count { it.isAvailable },
            unavailableCount = listings.count { !it.isAvailable },
            deletedMessage   = deleted
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProviderDashboardUiState())

    init {
        viewModelScope.launch {
            repo.getProviderListings(ownerUid).collect { list ->
                _rawListings.value = list
                _isLoading.value   = false
            }
        }
    }

    fun updateSearch(q: String) { _searchQuery.value = q }

    fun toggleAvailability(listing: Listing) {
        viewModelScope.launch {
            repo.toggleAvailability(listing.listingId, listing.isAvailable)
        }
    }

    fun deleteListing(listing: Listing) {
        viewModelScope.launch {
            repo.deleteListing(listing.listingId)
            _deletedMessage.value = "\"${listing.title}\" deleted"
        }
    }

    fun clearDeletedMessage() { _deletedMessage.value = "" }
}
