package com.example.kwagae

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.Listing
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ListingDetailUiState(
    val listing: Listing? = null,
    val isLoading: Boolean = true
)

class ListingDetailViewModel(
    application: Application,
    private val listingId: Long
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(ListingDetailUiState())
    val uiState: StateFlow<ListingDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val listing = db.listingDao().getById(listingId)
            _uiState.update { it.copy(listing = listing, isLoading = false) }
        }
    }

    companion object {
        fun factory(listingId: Long) = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                ListingDetailViewModel(app, listingId)
            }
        }
    }
}
