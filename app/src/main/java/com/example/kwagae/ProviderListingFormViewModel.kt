package com.example.kwagae

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.Listing
import com.example.kwagae.data.repository.FirebaseStorageRepository
import com.example.kwagae.data.repository.ProviderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ListingFormState(
    val listingId: Long = 0L,

    // Images
    val imageUris: List<String> = emptyList(),

    // Basic info
    val title: String = "",
    val titleError: String = "",
    val description: String = "",
    val location: String = "",
    val locationError: String = "",
    val contactInfo: String = "",
    val type: String = "Apartment",
    val price: String = "",
    val priceError: String = "",
    val depositAmount: String = "",
    val availabilityDate: String = "",
    val isAvailable: Boolean = true,

    // Utilities & features
    val wifiIncluded: Boolean = false,
    val waterIncluded: Boolean = false,
    val electricityIncluded: Boolean = false,
    val parkingAvailable: Boolean = false,
    val securityAvailable: Boolean = false,
    val isFurnished: Boolean = false,
    val kitchenAvailable: Boolean = false,

    // Room details
    val roomCount: Int = 1,
    val bathroomType: String = "Shared",
    val maxOccupants: Int = 1,
    val genderPreference: String = "Any",

    // Additional
    val nearbySchools: String = "",
    val rules: String = "",

    // UI control
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String = ""
)

class ProviderListingFormViewModel(
    application: Application,
    private val existingListingId: Long
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)
    // Firebase UID → seeded studentId (e.g. "PR001") → Room auto-ID
    val ownerUid  = prefs.getString("firebase_uid", null)?.takeIf { it.isNotEmpty() }
        ?: prefs.getString("student_id", null)?.takeIf { it.isNotEmpty() }
        ?: prefs.getLong("user_id", -1L).toString()
    val ownerName = prefs.getString("full_name", "Provider") ?: "Provider"

    private val db   = AppDatabase.getDatabase(application)
    private val repo = ProviderRepository(db.listingDao())

    private val _state = MutableStateFlow(ListingFormState())
    val state: StateFlow<ListingFormState> = _state.asStateFlow()

    init {
        if (existingListingId > 0L) {
            viewModelScope.launch {
                val listing = db.listingDao().getById(existingListingId) ?: return@launch
                _state.value = ListingFormState(
                    listingId        = listing.listingId,
                    imageUris        = listing.imageUrls.split(",").filter { it.isNotBlank() },
                    title            = listing.title,
                    description      = listing.description,
                    location         = listing.location,
                    contactInfo      = listing.contactInfo,
                    type             = listing.type.ifEmpty { "Apartment" },
                    price            = if (listing.price > 0) listing.price.toInt().toString() else "",
                    depositAmount    = if (listing.depositAmount > 0) listing.depositAmount.toInt().toString() else "",
                    availabilityDate = listing.availabilityDate,
                    isAvailable      = listing.isAvailable,
                    wifiIncluded     = listing.wifiIncluded,
                    waterIncluded    = listing.waterIncluded,
                    electricityIncluded = listing.electricityIncluded,
                    parkingAvailable = listing.parkingAvailable,
                    securityAvailable = listing.securityAvailable,
                    isFurnished      = listing.isFurnished,
                    kitchenAvailable = listing.kitchenAvailable,
                    roomCount        = listing.roomCount.coerceAtLeast(1),
                    bathroomType     = listing.bathroomType.ifEmpty { "Shared" },
                    maxOccupants     = listing.maxOccupants.coerceAtLeast(1),
                    genderPreference = listing.genderPreference.ifEmpty { "Any" },
                    nearbySchools    = listing.nearbySchools,
                    rules            = listing.rules
                )
            }
        }
    }

    // ── Image management ──────────────────────────────────────────────────────

    fun addImages(uris: List<String>) {
        _state.update { s -> s.copy(imageUris = (s.imageUris + uris).distinct()) }
    }

    fun removeImage(index: Int) {
        _state.update { s ->
            s.copy(imageUris = s.imageUris.toMutableList().also { it.removeAt(index) })
        }
    }

    // ── Form field setters ────────────────────────────────────────────────────

    fun setTitle(v: String)            { _state.update { it.copy(title = v, titleError = "") } }
    fun setDescription(v: String)      { _state.update { it.copy(description = v) } }
    fun setLocation(v: String)         { _state.update { it.copy(location = v, locationError = "") } }
    fun setContactInfo(v: String)      { _state.update { it.copy(contactInfo = v) } }
    fun setType(v: String)             { _state.update { it.copy(type = v) } }
    fun setPrice(v: String)            { _state.update { it.copy(price = v, priceError = "") } }
    fun setDeposit(v: String)          { _state.update { it.copy(depositAmount = v) } }
    fun setAvailabilityDate(v: String) { _state.update { it.copy(availabilityDate = v) } }
    fun setIsAvailable(v: Boolean)     { _state.update { it.copy(isAvailable = v) } }
    fun setWifi(v: Boolean)            { _state.update { it.copy(wifiIncluded = v) } }
    fun setWater(v: Boolean)           { _state.update { it.copy(waterIncluded = v) } }
    fun setElectricity(v: Boolean)     { _state.update { it.copy(electricityIncluded = v) } }
    fun setParking(v: Boolean)         { _state.update { it.copy(parkingAvailable = v) } }
    fun setSecurity(v: Boolean)        { _state.update { it.copy(securityAvailable = v) } }
    fun setFurnished(v: Boolean)       { _state.update { it.copy(isFurnished = v) } }
    fun setKitchen(v: Boolean)         { _state.update { it.copy(kitchenAvailable = v) } }
    fun setRoomCount(v: Int)           { _state.update { it.copy(roomCount = v.coerceIn(1, 20)) } }
    fun setBathroomType(v: String)     { _state.update { it.copy(bathroomType = v) } }
    fun setMaxOccupants(v: Int)        { _state.update { it.copy(maxOccupants = v.coerceIn(1, 20)) } }
    fun setGenderPreference(v: String) { _state.update { it.copy(genderPreference = v) } }
    fun setNearbySchools(v: String)    { _state.update { it.copy(nearbySchools = v) } }
    fun setRules(v: String)            { _state.update { it.copy(rules = v) } }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun save() {
        val s = _state.value
        var valid = true

        if (s.title.isBlank()) {
            _state.update { it.copy(titleError = "Title is required") }
            valid = false
        }
        if (s.location.isBlank()) {
            _state.update { it.copy(locationError = "Location is required") }
            valid = false
        }
        val priceVal = s.price.toDoubleOrNull()
        if (priceVal == null || priceVal <= 0.0) {
            _state.update { it.copy(priceError = "Enter a valid monthly price") }
            valid = false
        }
        if (!valid) return

        _state.update { it.copy(isSaving = true, errorMessage = "") }

        viewModelScope.launch {
            try {
                // Reserve a local ID so we can use it as the Storage folder name
                val placeholderListing = Listing(
                    listingId    = s.listingId,
                    ownerUid     = ownerUid,
                    providerName = ownerName,
                    title        = s.title.trim()
                )
                val reservedId = if (s.listingId == 0L) repo.saveListing(placeholderListing) else s.listingId

                // Upload any local content URIs to Firebase Storage
                val uploadedUris = FirebaseStorageRepository.uploadListingImages(
                    context        = getApplication(),
                    ownerUid       = ownerUid,
                    listingLocalId = reservedId,
                    uris           = s.imageUris
                )

                val listing = Listing(
                    listingId        = reservedId,
                    ownerUid         = ownerUid,
                    providerName     = ownerName,
                    title            = s.title.trim(),
                    description      = s.description.trim(),
                    location         = s.location.trim(),
                    contactInfo      = s.contactInfo.trim(),
                    type             = s.type,
                    price            = priceVal!!,
                    depositAmount    = s.depositAmount.toDoubleOrNull() ?: 0.0,
                    availabilityDate = s.availabilityDate.trim(),
                    isAvailable      = s.isAvailable,
                    imageUrl         = uploadedUris.firstOrNull() ?: "",
                    imageUrls        = uploadedUris.joinToString(","),
                    amenities        = buildAmenitiesString(s),
                    wifiIncluded     = s.wifiIncluded,
                    waterIncluded    = s.waterIncluded,
                    electricityIncluded = s.electricityIncluded,
                    parkingAvailable = s.parkingAvailable,
                    securityAvailable = s.securityAvailable,
                    isFurnished      = s.isFurnished,
                    kitchenAvailable = s.kitchenAvailable,
                    roomCount        = s.roomCount,
                    bathroomType     = s.bathroomType,
                    maxOccupants     = s.maxOccupants,
                    genderPreference = s.genderPreference,
                    nearbySchools    = s.nearbySchools.trim(),
                    rules            = s.rules.trim(),
                    pendingSync      = true
                )

                // For new listings the placeholder was already saved; update it with full data
                // For edits, run a normal update
                repo.updateListing(listing)

                _state.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorMessage = "Failed to save: ${e.message}") }
            }
        }
    }

    private fun buildAmenitiesString(s: ListingFormState): String = buildList {
        if (s.wifiIncluded)       add("Wi-Fi")
        if (s.waterIncluded)      add("Water Included")
        if (s.electricityIncluded) add("Electricity Included")
        if (s.parkingAvailable)   add("Parking")
        if (s.securityAvailable)  add("Security")
        if (s.isFurnished)        add("Furnished")
        if (s.kitchenAvailable)   add("Kitchen")
    }.joinToString(", ")

    companion object {
        fun factory(listingId: Long = 0L) = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                ProviderListingFormViewModel(app, listingId)
            }
        }
    }
}
