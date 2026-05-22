package com.example.kwagae

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.Listing
import com.example.kwagae.data.repository.ListingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

// Gaborone areas used both in filter chips and location filter logic
val GABORONE_AREAS = listOf(
    "Block 6", "Block 7", "Block 8", "Block 9",
    "Phase 2", "Phase 4", "Broadhurst", "CBD",
    "Old Naledi", "Bontleng", "Phakalane", "Riverwalk",
    "Extension 10", "Extension 12", "G-West",
    "Ledumang", "Mogoditshane", "Tlokweng"
)

data class FilterState(
    val searchQuery: String = "",
    val minPrice: Float = 0f,
    val maxPrice: Float = 15000f,
    val selectedTypes: Set<String> = emptySet(),
    val selectedLocations: Set<String> = emptySet(),
    val availabilityDateMillis: Long? = null,
    val wifiOnly: Boolean = false,
    val furnishedOnly: Boolean = false
)

class ListingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao         = AppDatabase.getDatabase(application).listingDao()
    private val dateFmt     = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val listingRepo = ListingRepository(dao, FirebaseFirestore.getInstance())

    init {
        // Keep Room in sync with Firestore for real-time listing updates
        listingRepo.startListening(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        listingRepo.stopListening()
    }

    private val _filters = MutableStateFlow(FilterState())
    val filters: StateFlow<FilterState> = _filters.asStateFlow()

    val hasActiveFilters: StateFlow<Boolean> = _filters.map { f ->
        f.searchQuery.isNotEmpty() ||
        f.selectedTypes.isNotEmpty() ||
        f.selectedLocations.isNotEmpty() ||
        f.availabilityDateMillis != null ||
        f.wifiOnly ||
        f.furnishedOnly ||
        f.minPrice > 0f ||
        f.maxPrice < 15000f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val filteredListings: StateFlow<List<Listing>> = dao.getAvailableListings()
        .combine(_filters) { listings, f -> applyFilters(listings, f) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateSearch(q: String) {
        _filters.value = _filters.value.copy(searchQuery = q)
    }

    fun updatePriceRange(min: Float, max: Float) {
        _filters.value = _filters.value.copy(minPrice = min, maxPrice = max)
    }

    fun toggleType(type: String) {
        val current = _filters.value.selectedTypes
        _filters.value = _filters.value.copy(
            selectedTypes = if (type in current) current - type else current + type
        )
    }

    fun toggleLocation(area: String) {
        val current = _filters.value.selectedLocations
        _filters.value = _filters.value.copy(
            selectedLocations = if (area in current) current - area else current + area
        )
    }

    fun updateAvailabilityDate(millis: Long?) {
        _filters.value = _filters.value.copy(availabilityDateMillis = millis)
    }

    fun setWifiOnly(v: Boolean) {
        _filters.value = _filters.value.copy(wifiOnly = v)
    }

    fun setFurnishedOnly(v: Boolean) {
        _filters.value = _filters.value.copy(furnishedOnly = v)
    }

    fun clearFilters() {
        _filters.value = FilterState()
    }

    private fun applyFilters(listings: List<Listing>, f: FilterState): List<Listing> {
        return listings.filter { listing ->
            // Studios hidden by default — only appear when explicitly searched or type-selected
            val studioExplicit = "Studio" in f.selectedTypes ||
                f.searchQuery.contains("studio", ignoreCase = true)
            if (listing.type.equals("Studio", ignoreCase = true) && !studioExplicit) {
                return@filter false
            }

            // Search (title, location, or type)
            if (f.searchQuery.isNotBlank() &&
                !listing.title.contains(f.searchQuery, ignoreCase = true) &&
                !listing.location.contains(f.searchQuery, ignoreCase = true) &&
                !listing.type.contains(f.searchQuery, ignoreCase = true)
            ) return@filter false

            // Price range
            if (listing.price < f.minPrice || listing.price > f.maxPrice) return@filter false

            // Property type
            if (f.selectedTypes.isNotEmpty() && listing.type !in f.selectedTypes) return@filter false

            // Location — match if ANY selected area appears in the listing's location string
            if (f.selectedLocations.isNotEmpty()) {
                val matchesArea = f.selectedLocations.any { area ->
                    listing.location.contains(area, ignoreCase = true)
                }
                if (!matchesArea) return@filter false
            }

            // Availability date — show only listings available on or before user's chosen date
            if (f.availabilityDateMillis != null) {
                val listingDate = runCatching { dateFmt.parse(listing.availabilityDate) }.getOrNull()
                if (listingDate != null && listingDate.time > f.availabilityDateMillis) return@filter false
            }

            // Wi-Fi
            if (f.wifiOnly) {
                val a = listing.amenities.lowercase()
                if (!a.contains("wi-fi") && !a.contains("wifi") && !a.contains("fibre")) return@filter false
            }

            // Furnished
            if (f.furnishedOnly && !listing.amenities.contains("furnished", ignoreCase = true)) return@filter false

            true
        }
    }
}
