package com.example.kwagae

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.Listing
import com.example.kwagae.ui.components.*   // ← all shared widgets live here
import com.example.kwagae.ui.theme.GroundedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(navController: NavController) {
    var listings by remember { mutableStateOf<List<Listing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GroundedColors.backgroundGradient)
    ) {
        // Decorative leaf shapes
        LeafShape(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-80).dp, y = (-60).dp)
        )
        LeafShape(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 240.dp, y = 800.dp)
        )

        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── Header stripe ─────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(GroundedColors.topStripeGradient)
                )
            }

            // ── Title section ─────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FIND YOUR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GroundedColors.ClayWarm,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "GROUNDED HOME",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GroundedColors.TextPrimary,
                        letterSpacing = 1.sp
                    )
                }
            }

            // ── Search and Filter card ────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF1E1208).copy(alpha = 0.2f),
                            spotColor = Color(0xFF1E1208).copy(alpha = 0.15f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        GroundedSearchField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search by title or location..."
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { showFilters = !showFilters },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (showFilters) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = GroundedColors.BarkMid
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (showFilters) "Hide Filters" else "Filters",
                                    fontSize = 12.sp,
                                    color = GroundedColors.BarkMid
                                )
                            }

                            TextButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Clear Search",
                                    fontSize = 12.sp,
                                    color = GroundedColors.ClayWarm
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showFilters,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                GroundedDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(
                                    text = "PRICE RANGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GroundedColors.ClayWarm,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "More filters coming soon...",
                                    fontSize = 11.sp,
                                    color = GroundedColors.TextMuted,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── Loading state ─────────────────────────────────────────────────
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = GroundedColors.ClayWarm,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Finding your grounded home...",
                                fontSize = 13.sp,
                                color = GroundedColors.TextMuted,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // ── Empty state ───────────────────────────────────────────────────
            } else if (filteredListings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = GroundedColors.TextMuted
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "No homes found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = GroundedColors.TextSecondary
                            )
                            Text(
                                text = "Try adjusting your search",
                                fontSize = 13.sp,
                                color = GroundedColors.TextMuted
                            )
                        }
                    }
                }

                // ── Listing cards ─────────────────────────────────────────────────
            } else {
                items(
                    items = filteredListings,
                    key = { it.listingId }
                ) { listing ->
                    GroundedListingCard(
                        listing = listing,
                        navController = navController,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ── Listing card ──────────────────────────────────────────────────────────────

@Composable
fun GroundedListingCard(
    listing: Listing,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("listing_detail/${listing.listingId}")
            }
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF1E1208).copy(alpha = 0.15f),
                spotColor = Color(0xFF1E1208).copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GroundedColors.CreamCard)
    ) {
        Column {
            // Top stripe accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(GroundedColors.topStripeGradient)
            )

            Column(modifier = Modifier.padding(16.dp)) {

                // ── Title and price row ───────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = listing.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GroundedColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GroundedColors.ClayWarm.copy(alpha = 0.15f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "BWP ${"%.0f".format(listing.price)}/mo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GroundedColors.ClayWarm,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Location ──────────────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(14.dp),
                        tint = GroundedColors.BarkMid
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = listing.location,
                        fontSize = 13.sp,
                        color = GroundedColors.TextSecondary
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ── Availability + bed/bath row ───────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GroundedColors.AccentMoss.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Available",
                                modifier = Modifier.size(12.dp),
                                tint = GroundedColors.AccentMoss
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Available ${listing.availabilityDate}",
                                fontSize = 11.sp,
                                color = GroundedColors.AccentMoss,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (listing.amenities.isNotBlank()) {
                        val amenitiesList = listing.amenities.split(",").map { it.trim() }
                        val bedInfo = amenitiesList.find { it.contains("bed", ignoreCase = true) }
                        val bathInfo = amenitiesList.find { it.contains("bath", ignoreCase = true) }

                        if (bedInfo != null || bathInfo != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                bedInfo?.let {
                                    Text(text = "🛏️ $it", fontSize = 11.sp, color = GroundedColors.TextMuted)
                                }
                                bathInfo?.let {
                                    Text(text = "🚿 $it", fontSize = 11.sp, color = GroundedColors.TextMuted)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Amenity chips ─────────────────────────────────────────────
                if (listing.amenities.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val amenitiesList = listing.amenities.split(",").map { it.trim() }
                        amenitiesList.take(3).forEach { amenity ->
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = amenity,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier
                                    .height(28.dp)
                                    .weight(1f, fill = false),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = GroundedColors.CreamField,
                                    labelColor = GroundedColors.BarkMid
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        if (amenitiesList.size > 3) {
                            Text(
                                text = "+${amenitiesList.size - 3}",
                                fontSize = 10.sp,
                                color = GroundedColors.TextMuted
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── View details footer ───────────────────────────────────────
                GroundedDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GroundedColors.ClayWarm,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = GroundedColors.ClayWarm
                    )
                }
            }
        }
    }
}

// LeafShape, GroundedDivider, GroundedSearchField →  ui/components/AppComponents.kt