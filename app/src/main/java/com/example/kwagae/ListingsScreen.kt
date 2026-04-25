package com.example.kwagae

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.example.kwagae.R
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.Listing
import kotlinx.coroutines.launch

@Composable
fun ListingsScreen(navController: NavController) {
    var listings by remember { mutableStateOf<List<Listing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        val database = AppDatabase.getDatabase(context)
        database.listingDao().getAvailableListings().collect { listingList ->
            listings = listingList
            isLoading = false
        }
    }
    
    val filteredListings = if (searchQuery.isEmpty()) {
        listings
    } else {
        listings.filter { 
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search houses by title or location...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            }
        )
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredListings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No houses available", style = MaterialTheme.typography.titleLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredListings) { listing ->
                    ListingCard(listing = listing, navController)
                }
            }
        }
    }
}

@Composable
fun ListingCard(listing: Listing, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                navController.navigate("listing_detail/${listing.listingId}")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Placeholder image - replace with actual image loading
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "House",
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = "P${listing.price}/month",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "📍 ${listing.location}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Available: ${listing.availabilityDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listing.amenities.split(",").take(2).forEach { amenity ->
                        AssistChip(
                            onClick = { },
                            label = { Text(amenity.trim()) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        }
    }
}
