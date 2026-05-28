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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ReservationUiState(
    val listing: Listing? = null,
    val isLoading: Boolean = true,

    // Payment form
    val cardHolder: String = "",
    val cardNumber: String = "",
    val expiry: String = "",
    val cvv: String = "",

    // Validation errors
    val cardHolderError: String = "",
    val cardNumberError: String = "",
    val expiryError: String = "",
    val cvvError: String = "",

    // UI control
    val isPaying: Boolean = false,
    val errorMessage: String = ""
)

class ReservationViewModel(
    application: Application,
    private val listingId: Long
) : AndroidViewModel(application) {

    private val db        = AppDatabase.getDatabase(application)
    private val prefs     = application.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)
    private val firestore = FirebaseFirestore.getInstance()

    private val currentUserUid: String
        get() = prefs.getString("firebase_uid", null)?.takeIf { it.isNotEmpty() }
            ?: prefs.getLong("user_id", -1L).toString()

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private val _navigateToReceipt = MutableSharedFlow<String>()
    val navigateToReceipt: SharedFlow<String> = _navigateToReceipt.asSharedFlow()

    init {
        viewModelScope.launch {
            val listing = db.listingDao().getById(listingId)
            _uiState.update { it.copy(listing = listing, isLoading = false) }
        }
    }

    fun onCardHolderChange(v: String) { _uiState.update { it.copy(cardHolder = v, cardHolderError = "") } }
    fun onCardNumberChange(v: String) {
        val digits = v.filter { it.isDigit() }.take(16)
        val formatted = digits.chunked(4).joinToString(" ")
        _uiState.update { it.copy(cardNumber = formatted, cardNumberError = "") }
    }
    fun onExpiryChange(v: String) {
        // Store ONLY raw digits — the "/" is added purely as a visual overlay in the UI.
        // Storing it in state caused the cursor to land before the "/" on the next keystroke,
        // making typed digits appear out of order (e.g. "10/27" became "10/72").
        val digits = v.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(expiry = digits, expiryError = "") }
    }
    fun onCvvChange(v: String) {
        val digits = v.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(cvv = digits, cvvError = "") }
    }

    fun pay() {
        val s = _uiState.value
        var valid = true

        if (s.cardHolder.isBlank()) {
            _uiState.update { it.copy(cardHolderError = "Enter cardholder name") }
            valid = false
        }
        val rawDigits = s.cardNumber.filter { it.isDigit() }
        if (rawDigits.length < 16) {
            _uiState.update { it.copy(cardNumberError = "Enter a valid 16-digit card number") }
            valid = false
        }
        if (s.expiry.length < 4) {   // state holds raw digits e.g. "1027", not "10/27"
            _uiState.update { it.copy(expiryError = "Enter expiry as MM/YY") }
            valid = false
        }
        if (s.cvv.length < 3) {
            _uiState.update { it.copy(cvvError = "Enter 3-4 digit CVV") }
            valid = false
        }
        if (!valid) return

        _uiState.update { it.copy(isPaying = true, errorMessage = "") }

        viewModelScope.launch {
            try {
                // Generate a unique reference number
                val ref = "KWG-${System.currentTimeMillis().toString(36).uppercase().takeLast(6)}"

                // Mark reserved in Room
                db.listingDao().reserveListing(listingId, currentUserUid, ref)

                // Sync to Firestore if the listing has a Firestore ID
                val listing = db.listingDao().getById(listingId)
                val fid = listing?.firestoreId
                if (!fid.isNullOrEmpty()) {
                    try {
                        firestore.collection("listings").document(fid).update(
                            mapOf(
                                "reserved"      to true,
                                "reservedByUid" to currentUserUid,
                                "reservationRef" to ref
                            )
                        ).await()
                    } catch (_: Exception) { /* will sync later */ }
                }

                _uiState.update { it.copy(isPaying = false) }
                _navigateToReceipt.emit(ref)
            } catch (e: Exception) {
                _uiState.update { it.copy(isPaying = false, errorMessage = "Payment failed: ${e.message}") }
            }
        }
    }

    companion object {
        fun factory(listingId: Long) = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                ReservationViewModel(app, listingId)
            }
        }
    }
}
