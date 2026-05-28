package com.example.kwagae

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.Listing
import kotlinx.coroutines.flow.*

class MyReservationsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)
    private val dao   = AppDatabase.getDatabase(application).listingDao()

    // Students are identified by their Room user_id stored as a string in reservedByUid
    val currentUserId: String = prefs.getLong("user_id", -1L).toString()

    val reservations: StateFlow<List<Listing>> = dao
        .getReservedByStudent(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
